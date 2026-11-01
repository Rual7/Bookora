package com.bookora.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookora.model.AppUser;
import com.bookora.model.Role;
import com.bookora.repository.UserRepository;

class AdminAccountInitializerTest {

	private UserRepository users;
	private PasswordEncoder encoder;

	@BeforeEach
	void setUp() {
		users = mock(UserRepository.class);
		encoder = mock(PasswordEncoder.class);
		when(encoder.encode("long-secret")).thenReturn("hashed");
	}

	@Test
	void createsTheFirstAdministratorWithAHashedPassword() {
		new AdminAccountInitializer(users, encoder, " Admin@Shop.test ", "long-secret").run(null);

		ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
		verify(users).save(saved.capture());
		assertEquals("admin@shop.test", saved.getValue().getEmail());
		assertEquals("hashed", saved.getValue().getPasswordHash());
		assertEquals(Role.ADMIN, saved.getValue().getRole());
	}

	@Test
	void doesNothingWhenAnAdministratorExists() {
		when(users.existsByRole(Role.ADMIN)).thenReturn(true);

		new AdminAccountInitializer(users, encoder, "admin@shop.test", "long-secret").run(null);

		verify(users, never()).save(any());
	}

	@Test
	void doesNothingWithoutCredentials() {
		new AdminAccountInitializer(users, encoder, "", "").run(null);

		verify(users, never()).save(any());
	}

	@Test
	void leavesAnExistingCustomerAlone() {
		when(users.existsByEmailIgnoreCase("ana@shop.test")).thenReturn(true);

		new AdminAccountInitializer(users, encoder, "ana@shop.test", "long-secret").run(null);

		verify(users, never()).save(any());
	}

}
