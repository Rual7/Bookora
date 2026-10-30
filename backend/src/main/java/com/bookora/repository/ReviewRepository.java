package com.bookora.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.Review;
import com.bookora.model.ReviewStatus;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByBookIdAndStatusOrderByCreatedAtDesc(Long bookId, ReviewStatus status);

}
