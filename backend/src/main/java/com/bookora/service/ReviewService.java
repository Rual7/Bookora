package com.bookora.service;

import com.bookora.dto.ReviewRequest;

public interface ReviewService {

	/** Saves a review as PENDING; it is shown on the book page only after an administrator approves it. */
	void submit(String email, String bookSlug, ReviewRequest request);

}
