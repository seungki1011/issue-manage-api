package com.tissue.api.issue.presentation.controller.dto.response;

import com.tissue.api.issue.domain.model.Issue;

public record IssueResponse(
	String workspaceCode,
	String issueKey
) {
	public static IssueResponse from(Issue issue) {
		return new IssueResponse(issue.getWorkspaceCode(), issue.getIssueKey());
	}
}
