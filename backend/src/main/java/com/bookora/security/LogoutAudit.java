package com.bookora.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.bookora.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Writes a log line when a signed-in user signs out, with the user's id and never the email. */
@Component
public class LogoutAudit implements LogoutHandler {

	private static final Logger log = LoggerFactory.getLogger(LogoutAudit.class);

	private final UserRepository users;

	public LogoutAudit(UserRepository users) {
		this.users = users;
	}

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (authentication == null) {
			return;
		}
		users.findByEmailIgnoreCase(authentication.getName())
				.ifPresent(user -> log.info("User signed out: userId={}", user.getId()));
	}

}
