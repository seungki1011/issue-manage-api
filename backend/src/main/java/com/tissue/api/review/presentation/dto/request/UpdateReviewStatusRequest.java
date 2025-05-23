package com.tissue.api.review.presentation.dto.request;

import com.tissue.api.review.domain.model.enums.ReviewStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateReviewStatusRequest(

	@NotNull(message = "{valid.notnull}")
	ReviewStatus status
) {
}
