package com.tissue.api.issue.domain.event;

import com.tissue.api.issue.domain.model.Issue;
import com.tissue.api.issue.domain.model.enums.IssueStatus;
import com.tissue.api.issue.domain.model.enums.IssueType;
import com.tissue.api.notification.domain.enums.NotificationType;
import com.tissue.api.notification.domain.enums.ResourceType;

import lombok.Getter;

/**
 * Todo
 *  - IssueStatusUpdated -> IssueStatusChanged
 */
@Getter
public class IssueStatusChangedEvent extends IssueEvent {

	// 이슈 상태 변경 정보
	private final IssueStatus oldStatus;
	private final IssueStatus newStatus;

	// 부모 이슈 정보 (있는 경우)
	private final Long parentIssueId;
	private final String parentIssueKey;
	private final IssueType parentIssueType;

	private final Integer storyPoint;

	private IssueStatusChangedEvent(
		Long issueId,
		String issueKey,
		String workspaceCode,
		IssueType issueType,
		Long actorMemberId,
		IssueStatus oldStatus,
		IssueStatus newStatus,
		Long parentIssueId,
		String parentIssueKey,
		IssueType parentIssueType,
		Integer storyPoint
	) {
		super(
			NotificationType.ISSUE_STATUS_CHANGED,
			ResourceType.ISSUE,
			workspaceCode,
			issueId,
			issueKey,
			issueType,
			actorMemberId
		);
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.parentIssueId = parentIssueId;
		this.parentIssueKey = parentIssueKey;
		this.parentIssueType = parentIssueType;
		this.storyPoint = storyPoint;
	}

	public static IssueStatusChangedEvent createEvent(
		Issue issue,
		IssueStatus oldStatus,
		Long actorMemberId
	) {
		Issue parentIssue = issue.hasParent() ? issue.getParentIssue() : null;

		return new IssueStatusChangedEvent(
			issue.getId(),
			issue.getIssueKey(),
			issue.getWorkspaceCode(),
			issue.getType(),
			actorMemberId,
			oldStatus,
			issue.getStatus(),
			parentIssue != null ? parentIssue.getId() : null,
			parentIssue != null ? parentIssue.getIssueKey() : null,
			parentIssue != null ? parentIssue.getType() : null,
			issue.getStoryPoint()
		);
	}

	/**
	 * 이슈가 CLOSED 상태로 변경되었는지 확인한다
	 */
	public boolean isClosedNow() {
		return newStatus == IssueStatus.CLOSED;
	}
}
