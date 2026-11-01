package com.bookora.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

	Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
