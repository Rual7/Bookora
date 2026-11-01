package com.bookora.config;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bookora.model.AppUser;
import com.bookora.model.Role;
import com.bookora.repository.UserRepository;

/** Creates the first administrator from the environment when the shop has none yet. */
@Component
public class AdminAccountInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final String email;
	private final String password;

	public AdminAccountInitializer(UserRepository users, PasswordEncoder passwordEncoder,
			@Value("${bookora.admin.email:}") String email, @Value("${bookora.admin.password:}") String password) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.email = email.trim().toLowerCase(Locale.ROOT);
		this.password = password;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (email.isEmpty() || password.isEmpty() || users.existsByRole(Role.ADMIN)) {
			return;
		}
		if (users.existsByEmailIgnoreCase(email)) {
			log.warn("ADMIN_EMAIL belongs to an existing customer account; no administrator was created");
			return;
		}
		users.save(AppUser.admin(email, passwordEncoder.encode(password), "Shop", "Administrator"));
		log.info("First administrator created");
	}

}
