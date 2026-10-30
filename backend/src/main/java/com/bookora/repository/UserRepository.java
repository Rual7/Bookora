package com.bookora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.bookora.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByEmailIgnoreCase(String email);

	/** Locks the user row until the transaction ends, so two requests of one user run one after the other. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from AppUser u where lower(u.email) = lower(:email)")
	Optional<AppUser> findAndLockByEmail(@Param("email") String email);

	boolean existsByEmailIgnoreCase(String email);

}
