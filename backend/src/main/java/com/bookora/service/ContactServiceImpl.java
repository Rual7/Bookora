package com.bookora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.ContactRequest;
import com.bookora.model.ContactMessage;
import com.bookora.repository.ContactMessageRepository;

@Service
public class ContactServiceImpl implements ContactService {

	private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

	private final ContactMessageRepository messages;

	public ContactServiceImpl(ContactMessageRepository messages) {
		this.messages = messages;
	}

	@Override
	@Transactional
	public void receive(ContactRequest request) {
		ContactMessage saved = messages.save(
				new ContactMessage(request.name().trim(), request.email().trim(), request.message().trim()));
		log.info("Contact message received: id={}", saved.getId());
	}

}
