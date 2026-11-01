package com.bookora.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookora.dto.ReviewRequest;
import com.bookora.service.ReviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books/{slug}/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void submit(@PathVariable String slug, @Valid @RequestBody ReviewRequest request,
			Authentication authentication) {
		reviewService.submit(authentication.getName(), slug, request);
	}

}
