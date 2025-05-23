package com.tissue.api.issue.presentation.controller.dto.request;

import com.tissue.api.issue.application.dto.AddReviewerCommand;

public record AddReviewerRequest(
	Long memberId
) {
	public AddReviewerCommand toCommand() {
		return new AddReviewerCommand(memberId);
	}
}
