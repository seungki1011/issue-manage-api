package com.tissue.api.workspace.presentation.dto.response;

import java.time.LocalDateTime;

import com.tissue.api.workspace.domain.Workspace;

public record UpdateWorkspaceInfoResponse(
	Long id,
	String code,
	LocalDateTime updatedAt
) {
	public static UpdateWorkspaceInfoResponse from(Workspace workspace) {
		return new UpdateWorkspaceInfoResponse(
			workspace.getId(),
			workspace.getCode(),
			workspace.getLastModifiedDate()
		);
	}
}
