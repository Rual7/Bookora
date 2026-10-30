package com.bookora.repository;

import java.math.BigDecimal;

/** One row of the book_rating_view. */
public interface RatingSummary {

	BigDecimal getAverageRating();

	long getReviewCount();

}
