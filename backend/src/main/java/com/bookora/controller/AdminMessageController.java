package com.bookora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.AdminMessageDto;
import com.bookora.dto.HandledRequest;
import com.bookora.dto.PageDto;
import com.bookora.service.AdminAudit;
import com.bookora.service.AdminMessageService;

@RestController
@RequestMapping("/api/admin/messages")
public class AdminMessageController {

	private static final int PAGE_SIZE = 20;

	private final AdminMessageService messageService;
	private final AdminAudit audit;

	public AdminMessageController(AdminMessageService messageService, AdminAudit audit) {
		this.messageService = messageService;
		this.audit = audit;
	}

	@GetMapping
	public PageDto<AdminMessageDto> list(@RequestParam(defaultValue = "0") int page) {
		return messageService.list(Math.max(page, 0), PAGE_SIZE);
	}

	@PutMapping("/{id}/handled")
	public AdminMessageDto setHandled(@PathVariable Long id, @RequestBody HandledRequest request, Authentication admin) {
		AdminMessageDto changed = messageService.setHandled(id, request.handled());
		audit.record(admin, "message.handled." + request.handled(), id);
		return changed;
	}

}
