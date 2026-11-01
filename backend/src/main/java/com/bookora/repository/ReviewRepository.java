package com.bookora.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.Review;
import com.bookora.model.ReviewStatus;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByBookIdAndStatusOrderByCreatedAtDesc(Long bookId, ReviewStatus status);

	Page<Review> findByStatusOrderByCreatedAtAsc(ReviewStatus status, Pageable pageable);

	boolean existsByUserIdAndBookId(Long userId, Long bookId);

}
