package com.bookora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.Role;

public interface UserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByEmailIgnoreCase(String email);

	/** Locks the user row until the transaction ends, so two requests of one user run one after the other. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from AppUser u where lower(u.email) = lower(:email)")
	Optional<AppUser> findAndLockByEmail(@Param("email") String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByRole(Role role);

	/** The signed-in user; services receive the email from the session, so a missing account is a 404. */
	default AppUser requireByEmail(String email) {
		return findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	default AppUser requireAndLockByEmail(String email) {
		return findAndLockByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

}
