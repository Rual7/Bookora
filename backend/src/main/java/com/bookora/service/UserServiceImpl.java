package com.bookora.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.RegisterRequest;
import com.bookora.dto.UserDto;
import com.bookora.exception.ConflictException;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository users, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public UserDto register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("An account with this email already exists");
		}
		AppUser saved = users.save(new AppUser(email, passwordEncoder.encode(request.password()),
				request.firstName().trim(), request.lastName().trim()));
		log.info("User registered: id={}", saved.getId());
		return toDto(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto getByEmail(String email) {
		return users.findByEmailIgnoreCase(email)
				.map(UserServiceImpl::toDto)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private static UserDto toDto(AppUser user) {
		return new UserDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
	}

}
