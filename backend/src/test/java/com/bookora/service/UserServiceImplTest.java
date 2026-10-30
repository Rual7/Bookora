package com.bookora.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bookora.dto.RegisterRequest;
import com.bookora.dto.UserDto;
import com.bookora.exception.ConflictException;
import com.bookora.model.AppUser;
import com.bookora.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository users;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UserServiceImpl service() {
		return new UserServiceImpl(users, passwordEncoder);
	}

	@Test
	void registerStoresLowercaseEmailAndHashedPassword() {
		when(users.existsByEmailIgnoreCase("ana@example.com")).thenReturn(false);
		when(passwordEncoder.encode("secret-pass")).thenReturn("hashed");
		when(users.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));

		UserDto created = service().register(new RegisterRequest(" Ana@Example.com ", "secret-pass", "Ana", "Pop"));

		assertThat(created.email()).isEqualTo("ana@example.com");
		verify(passwordEncoder).encode("secret-pass");
	}

	@Test
	void registerRejectsAnEmailThatIsAlreadyUsed() {
		when(users.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

		assertThatThrownBy(() -> service().register(new RegisterRequest("ana@example.com", "secret-pass", "Ana", "Pop")))
				.isInstanceOf(ConflictException.class);
		verify(users, never()).save(any());
	}

}
