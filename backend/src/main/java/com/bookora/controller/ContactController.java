package com.bookora.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.ContactRequest;
import com.bookora.service.CaptchaService;
import com.bookora.service.ContactService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

	private final ContactService contactService;
	private final CaptchaService captcha;

	public ContactController(ContactService contactService, CaptchaService captcha) {
		this.contactService = contactService;
		this.captcha = captcha;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void send(@Valid @RequestBody ContactRequest request, HttpServletRequest httpRequest,
			@RequestHeader(value = AuthController.CAPTCHA_HEADER, required = false) String captchaToken) {
		captcha.verify(captchaToken, httpRequest.getRemoteAddr());
		contactService.receive(request);
	}

}
