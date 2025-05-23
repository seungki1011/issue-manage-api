package com.tissue.api.notification.domain.service.message;

import org.springframework.stereotype.Component;

import com.tissue.api.comment.domain.event.ReviewCommentAddedEvent;
import com.tissue.api.common.event.DomainEvent;
import com.tissue.api.issue.domain.event.IssueParentAssignedEvent;
import com.tissue.api.issue.domain.event.IssueParentRemovedEvent;
import com.tissue.api.issue.domain.event.IssueReviewerAddedEvent;
import com.tissue.api.issue.domain.event.IssueStatusChangedEvent;
import com.tissue.api.review.domain.event.ReviewSubmittedEvent;
import com.tissue.api.sprint.domain.event.SprintCompletedEvent;
import com.tissue.api.workspace.domain.event.MemberJoinedWorkspaceEvent;
import com.tissue.api.workspacemember.domain.event.WorkspaceMemberRoleChangedEvent;

/**
 * Helper class for creating notification message content arguments.
 */
@Component
public class NotificationContentArgumentsFormatter {

	/**
	 * Create default arguments.
	 */
	public Object[] createStandardArgs(String actorNickname, String entityKey) {
		return new Object[] {actorNickname, entityKey};
	}

	public Object[] createIssueStatusChangeArgs(DomainEvent event, String actorNickname) {
		IssueStatusChangedEvent statusChangedEvent = (IssueStatusChangedEvent)event;
		return new Object[] {
			actorNickname,
			event.getEntityKey(),
			statusChangedEvent.getOldStatus().toString(),
			statusChangedEvent.getNewStatus().toString()
		};
	}

	public Object[] createIssueParentAssignedArgs(DomainEvent event, String actorNickname) {
		IssueParentAssignedEvent parentAssignedEvent = (IssueParentAssignedEvent)event;
		return new Object[] {
			actorNickname,
			event.getEntityKey(),
			parentAssignedEvent.getParentIssueKey()
		};
	}

	public Object[] createIssueParentRemovedArgs(DomainEvent event, String actorNickname) {
		IssueParentRemovedEvent parentRemovedEvent = (IssueParentRemovedEvent)event;
		return new Object[] {
			actorNickname,
			event.getEntityKey(),
			parentRemovedEvent.getRemovedParentIssueKey()
		};
	}

	public Object[] createReviewerAddedArgs(DomainEvent event, String actorNickname) {
		IssueReviewerAddedEvent issueReviewerAddedEvent = (IssueReviewerAddedEvent)event;
		return new Object[] {
			actorNickname,
			event.getEntityKey(),
			issueReviewerAddedEvent.getReviewerNickname()
		};
	}

	public Object[] createReviewSubmittedArgs(DomainEvent event, String actorNickname) {
		ReviewSubmittedEvent reviewSubmittedEvent = (ReviewSubmittedEvent)event;
		return new Object[] {
			actorNickname,
			event.getEntityKey(),
			reviewSubmittedEvent.getReviewStatus()
		};
	}

	public Object[] createReviewCommentAddedArgs(DomainEvent event, String actorNickname) {
		ReviewCommentAddedEvent commentEvent = (ReviewCommentAddedEvent)event;
		return new Object[] {
			actorNickname,
			event.getEntityKey(),
			commentEvent.getReviewId().toString()
		};
	}

	public Object[] createSprintStartedArgs(String entityKey) {
		return new Object[] {entityKey};
	}

	public Object[] createSprintCompletedArgs(DomainEvent event) {
		SprintCompletedEvent sprintCompletedEvent = (SprintCompletedEvent)event;
		return new Object[] {
			event.getEntityKey(),
			sprintCompletedEvent.getSprintStartedAt().toString(),
			sprintCompletedEvent.getSprintCompletedAt().toString()
		};
	}

	public Object[] createMemberJoinedWorkspaceArgs(DomainEvent event) {
		MemberJoinedWorkspaceEvent joinedWorkspaceEvent = (MemberJoinedWorkspaceEvent)event;
		return new Object[] {
			joinedWorkspaceEvent.getNickname(),
			joinedWorkspaceEvent.getWorkspaceRole().toString()
		};
	}

	public Object[] createWorkspaceRoleChangedArgs(DomainEvent event, String actorNickname) {
		WorkspaceMemberRoleChangedEvent roleChangedEvent = (WorkspaceMemberRoleChangedEvent)event;
		return new Object[] {
			actorNickname,
			roleChangedEvent.getTargetNickname(),
			roleChangedEvent.getOldRole().toString(),
			roleChangedEvent.getNewRole().toString()
		};
	}
}
