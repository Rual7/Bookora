package com.bookora.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.AdminReviewDto;
import com.bookora.dto.PageDto;
import com.bookora.dto.ReviewStatusRequest;
import com.bookora.model.ReviewStatus;
import com.bookora.service.AdminAudit;
import com.bookora.service.AdminReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

	private final AdminReviewService reviewService;
	private final AdminAudit audit;

	public AdminReviewController(AdminReviewService reviewService, AdminAudit audit) {
		this.reviewService = reviewService;
		this.audit = audit;
	}

	@GetMapping
	public PageDto<AdminReviewDto> list(@RequestParam(defaultValue = "PENDING") ReviewStatus status,
			@RequestParam(defaultValue = "0") int page) {
		return reviewService.list(status, AdminPaging.page(page), AdminPaging.PAGE_SIZE);
	}

	@PutMapping("/{id}/status")
	public AdminReviewDto moderate(@PathVariable Long id, @Valid @RequestBody ReviewStatusRequest request,
			Authentication admin) {
		AdminReviewDto moderated = reviewService.moderate(id, request.status());
		audit.record(admin, "review." + request.status(), id);
		return moderated;
	}

}
