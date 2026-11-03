package com.bookora.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.bookora.exception.BadRequestException;
import com.bookora.exception.ServiceUnavailableException;

/** Checks the token of the Cloudflare Turnstile widget with Cloudflare, so a script cannot just skip the widget. */
@Service
public class TurnstileCaptchaService implements CaptchaService {

	private static final Logger log = LoggerFactory.getLogger(TurnstileCaptchaService.class);

	private final String secret;
	private final String verifyUrl;
	private final RestClient client;

	public TurnstileCaptchaService(@Value("${bookora.turnstile.secret}") String secret,
			@Value("${bookora.turnstile.verify-url}") String verifyUrl) {
		this.secret = secret;
		this.verifyUrl = verifyUrl;
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000);
		factory.setReadTimeout(5000);
		this.client = RestClient.builder().requestFactory(factory).build();
		if (secret.isBlank()) {
			log.warn("TURNSTILE_SECRET is not set: the CAPTCHA is not checked");
		}
	}

	@Override
	public void verify(String token, String clientIp) {
		if (secret.isBlank()) {
			return;
		}
		if (token == null || token.isBlank()) {
			throw new BadRequestException("Please complete the verification");
		}
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("secret", secret);
		form.add("response", token);
		form.add("remoteip", clientIp);
		Map<String, Object> answer;
		try {
			answer = client.post().uri(verifyUrl).contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form)
					.retrieve().body(new ParameterizedTypeReference<>() { });
		} catch (RuntimeException e) {
			log.warn("Turnstile could not be reached: {}", e.getMessage());
			throw new ServiceUnavailableException("The verification service is not available, please try again");
		}
		if (answer == null || !Boolean.TRUE.equals(answer.get("success"))) {
			log.info("Turnstile refused a token: {}", answer == null ? null : answer.get("error-codes"));
			throw new BadRequestException("The verification failed, please try again");
		}
	}

}
