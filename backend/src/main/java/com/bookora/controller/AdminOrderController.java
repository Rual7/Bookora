package com.bookora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.AdminOrderDto;
import com.bookora.dto.AdminOrderRowDto;
import com.bookora.dto.OrderStatusRequest;
import com.bookora.dto.PageDto;
import com.bookora.model.OrderStatus;
import com.bookora.service.AdminAudit;
import com.bookora.service.AdminOrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final AdminOrderService orderService;
	private final AdminAudit audit;

	public AdminOrderController(AdminOrderService orderService, AdminAudit audit) {
		this.orderService = orderService;
		this.audit = audit;
	}

	@GetMapping
	public PageDto<AdminOrderRowDto> list(@RequestParam(required = false) OrderStatus status,
			@RequestParam(defaultValue = "0") int page) {
		return orderService.list(status, AdminPaging.page(page), AdminPaging.PAGE_SIZE);
	}

	@GetMapping("/{orderNumber}")
	public AdminOrderDto get(@PathVariable String orderNumber) {
		return orderService.get(orderNumber);
	}

	@PutMapping("/{orderNumber}/status")
	public AdminOrderDto changeStatus(@PathVariable String orderNumber, @Valid @RequestBody OrderStatusRequest request,
			Authentication admin) {
		AdminOrderDto changed = orderService.changeStatus(orderNumber, request.status());
		audit.record(admin, "order.status." + request.status(), orderNumber);
		return changed;
	}

}
