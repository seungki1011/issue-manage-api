package com.uranus.taskmanager.api.workspacemember.presentation.dto.response;

import java.time.LocalDateTime;

import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;

public record AssignPositionResponse(
	Long workspaceMemberId,
	String assignedPosition,
	LocalDateTime assignedAt
) {
	public static AssignPositionResponse from(WorkspaceMember workspaceMember) {
		return new AssignPositionResponse(
			workspaceMember.getId(),
			workspaceMember.getPosition().getName(),
			workspaceMember.getLastModifiedDate()
		);
	}
}