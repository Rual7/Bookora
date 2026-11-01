package com.bookora.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.AdminMessageDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.ContactMessage;
import com.bookora.repository.ContactMessageRepository;

@Service
public class AdminMessageServiceImpl implements AdminMessageService {

	private final ContactMessageRepository messages;

	public AdminMessageServiceImpl(ContactMessageRepository messages) {
		this.messages = messages;
	}

	@Override
	@Transactional(readOnly = true)
	public PageDto<AdminMessageDto> list(int page, int size) {
		return PageDto.of(messages.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
				.map(AdminMessageServiceImpl::toDto));
	}

	@Override
	@Transactional
	public AdminMessageDto setHandled(Long id, boolean handled) {
		ContactMessage message = messages.findById(id).orElseThrow(() -> new ResourceNotFoundException("Message not found"));
		message.setHandled(handled);
		return toDto(message);
	}

	private static AdminMessageDto toDto(ContactMessage message) {
		return new AdminMessageDto(message.getId(), message.getName(), message.getEmail(), message.getMessage(),
				message.isHandled(), message.getCreatedAt());
	}

}
