package com.bookora.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.LoginRequest;
import com.bookora.dto.RegisterRequest;
import com.bookora.dto.UserDto;
import com.bookora.service.CaptchaService;
import com.bookora.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	public static final String CAPTCHA_HEADER = "X-Captcha-Token";

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final SecurityContextHolderStrategy contextStrategy = SecurityContextHolder.getContextHolderStrategy();
	private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final CaptchaService captcha;

	public AuthController(AuthenticationManager authenticationManager, UserService userService, CaptchaService captcha) {
		this.authenticationManager = authenticationManager;
		this.userService = userService;
		this.captcha = captcha;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, @RequestHeader(value = CAPTCHA_HEADER, required = false) String captchaToken) {
		captcha.verify(captchaToken, httpRequest.getRemoteAddr());
		userService.register(request);
		return signIn(request.email(), request.password(), httpRequest, httpResponse);
	}

	@PostMapping("/login")
	public UserDto login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, @RequestHeader(value = CAPTCHA_HEADER, required = false) String captchaToken) {
		captcha.verify(captchaToken, httpRequest.getRemoteAddr());
		return signIn(request.email(), request.password(), httpRequest, httpResponse);
	}

	/** A CAPTCHA token works once, so registering also signs the new customer in instead of asking for a second token. */
	private UserDto signIn(String email, String password, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(email.trim(), password));

		if (httpRequest.getSession(false) != null) {
			httpRequest.changeSessionId();
		}
		SecurityContext context = contextStrategy.createEmptyContext();
		context.setAuthentication(authentication);
		contextStrategy.setContext(context);
		contextRepository.saveContext(context, httpRequest, httpResponse);

		UserDto user = userService.getByEmail(authentication.getName());
		log.info("User signed in: userId={}", user.id());
		return user;
	}

	/** Does nothing itself; the response carries the CSRF cookie the browser needs before its next POST. */
	@GetMapping("/csrf")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void csrf() {
	}

	@GetMapping("/me")
	public ResponseEntity<UserDto> me(Authentication authentication) {
		if (authentication == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(userService.getByEmail(authentication.getName()));
	}

}
