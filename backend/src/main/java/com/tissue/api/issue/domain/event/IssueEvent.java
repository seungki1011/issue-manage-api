package com.tissue.api.issue.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tissue.api.common.event.DomainEvent;
import com.tissue.api.issue.domain.model.enums.IssueType;
import com.tissue.api.notification.domain.enums.NotificationType;
import com.tissue.api.notification.domain.enums.ResourceType;
import com.tissue.api.notification.domain.model.vo.EntityReference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Todo
 *  - Issue를 전달하는게 아니라 DTO를 만들어서 필요한 데이터만 전달하도록 리팩토링
 *  - 또는 issue id만 전달하고, 핸들러에서 id로 다시 조회하는 방식
 */
@Getter
@RequiredArgsConstructor
public abstract class IssueEvent implements DomainEvent {

	private final UUID eventId = UUID.randomUUID();
	private final LocalDateTime occurredAt = LocalDateTime.now();

	private final NotificationType notificationType;
	private final ResourceType resourceType;

	private final String workspaceCode;
	private final Long issueId;
	private final String issueKey;
	private final IssueType issueType;
	private final Long actorMemberId;

	@Override
	public String getEntityKey() {
		return issueKey;
	}

	@Override
	public EntityReference createEntityReference() {
		return EntityReference.forIssue(getWorkspaceCode(), getIssueKey());
	}
}
