package com.tissue.api.assignee.domain.event;

import com.tissue.api.issue.domain.Issue;
import com.tissue.api.issue.domain.enums.IssueType;
import com.tissue.api.issue.domain.event.IssueEvent;
import com.tissue.api.notification.domain.enums.NotificationType;
import com.tissue.api.notification.domain.enums.ResourceType;

import lombok.Getter;

@Getter
public class IssueAssignedEvent extends IssueEvent {

	private final Long assignedWorkspaceMemberId;

	public IssueAssignedEvent(
		Long issueId,
		String issueKey,
		String workspaceCode,
		IssueType issueType,
		Long triggeredByWorkspaceMemberId,
		Long assignedWorkspaceMemberId
	) {
		super(
			NotificationType.ISSUE_ASSIGNED,
			ResourceType.ISSUE,
			workspaceCode,
			issueId,
			issueKey,
			issueType,
			triggeredByWorkspaceMemberId
		);
		this.assignedWorkspaceMemberId = assignedWorkspaceMemberId;
	}

	public static IssueAssignedEvent createEvent(
		Issue issue,
		Long assignedWorkspaceMemberId,
		Long triggeredByWorkspaceMemberId
	) {
		return new IssueAssignedEvent(
			issue.getId(),
			issue.getIssueKey(),
			issue.getWorkspaceCode(),
			issue.getType(),
			triggeredByWorkspaceMemberId,
			assignedWorkspaceMemberId
		);
	}
}
