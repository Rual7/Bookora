package com.bookora.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookora.dto.AdminReviewDto;
import com.bookora.dto.PageDto;
import com.bookora.exception.ResourceNotFoundException;
import com.bookora.model.AppUser;
import com.bookora.model.Review;
import com.bookora.model.ReviewStatus;
import com.bookora.repository.OrderRepository;
import com.bookora.repository.ReviewRepository;

@Service
public class AdminReviewServiceImpl implements AdminReviewService {

	private final ReviewRepository reviews;
	private final OrderRepository orders;

	public AdminReviewServiceImpl(ReviewRepository reviews, OrderRepository orders) {
		this.reviews = reviews;
		this.orders = orders;
	}

	@Override
	@Transactional(readOnly = true)
	public PageDto<AdminReviewDto> list(ReviewStatus status, int page, int size) {
		return PageDto.of(reviews.findByStatusOrderByCreatedAtAsc(status, PageRequest.of(page, size)).map(this::toDto));
	}

	@Override
	@Transactional
	public AdminReviewDto moderate(Long id, ReviewStatus status) {
		Review review = reviews.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
		review.moderate(status);
		return toDto(review);
	}

	private AdminReviewDto toDto(Review review) {
		AppUser author = review.getUser();
		boolean bought = orders.hasBought(author.getId(), review.getBook().getId());
		return new AdminReviewDto(review.getId(), review.getBook().getTitle(), review.getBook().getSlug(),
				author.getFullName(), author.getEmail(), review.getRating(),
				review.getTitle(), review.getComment(), review.getStatus(), bought, review.getCreatedAt());
	}

}
