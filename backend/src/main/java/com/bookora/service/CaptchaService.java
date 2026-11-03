package com.bookora.service;

public interface CaptchaService {

	/** Throws when the visitor's token is missing or refused. Does nothing when no secret is configured (development). */
	void verify(String token, String clientIp);

}
