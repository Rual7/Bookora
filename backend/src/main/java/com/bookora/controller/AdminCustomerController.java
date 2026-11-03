package com.bookora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.AdminCustomerDto;
import com.bookora.dto.EnabledRequest;
import com.bookora.dto.PageDto;
import com.bookora.service.AdminAudit;
import com.bookora.service.AdminCustomerService;

@RestController
@RequestMapping("/api/admin/customers")
public class AdminCustomerController {

	private final AdminCustomerService customerService;
	private final AdminAudit audit;

	public AdminCustomerController(AdminCustomerService customerService, AdminAudit audit) {
		this.customerService = customerService;
		this.audit = audit;
	}

	@GetMapping
	public PageDto<AdminCustomerDto> list(@RequestParam(defaultValue = "0") int page) {
		return customerService.list(AdminPaging.page(page), AdminPaging.PAGE_SIZE);
	}

	@PutMapping("/{id}/enabled")
	public AdminCustomerDto setEnabled(@PathVariable Long id, @RequestBody EnabledRequest request, Authentication admin) {
		AdminCustomerDto changed = customerService.setEnabled(id, request.enabled());
		audit.record(admin, "customer.enabled." + request.enabled(), id);
		return changed;
	}

}
