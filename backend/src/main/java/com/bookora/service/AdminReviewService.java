package com.bookora.service;

import com.bookora.dto.AdminReviewDto;
import com.bookora.dto.PageDto;
import com.bookora.model.ReviewStatus;

public interface AdminReviewService {

	/** Reviews with the given status, oldest first, so the longest-waiting one is on top. */
	PageDto<AdminReviewDto> list(ReviewStatus status, int page, int size);

	AdminReviewDto moderate(Long id, ReviewStatus status);

}
