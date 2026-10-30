package com.bookora.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.repository.UserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

	private final UserRepository users;

	public AppUserDetailsService(UserRepository users) {
		this.users = users;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return users.findByEmailIgnoreCase(email)
				.map(u -> User.withUsername(u.getEmail())
						.password(u.getPasswordHash())
						.roles(u.getRole().name())
						.disabled(!u.isEnabled())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
	}

}
