package com.bookora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.bookora.model.AppUser;
import com.bookora.repository.UserRepository;

/** Writes one log line for every change an administrator makes, with the administrator's id and never the email. */
@Component
public class AdminAudit {

	private static final Logger log = LoggerFactory.getLogger(AdminAudit.class);

	private final UserRepository users;

	public AdminAudit(UserRepository users) {
		this.users = users;
	}

	public void record(Authentication admin, String action, Object target) {
		Long adminId = users.findByEmailIgnoreCase(admin.getName()).map(AppUser::getId).orElse(null);
		log.info("Admin action: adminId={} action={} target={}", adminId, action, target);
	}

}
