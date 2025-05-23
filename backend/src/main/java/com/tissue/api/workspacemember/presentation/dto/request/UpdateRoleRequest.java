package com.tissue.api.workspacemember.presentation.dto.request;

import com.tissue.api.workspacemember.domain.model.enums.WorkspaceRole;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(

	@NotNull(message = "{valid.notnull}")
	WorkspaceRole updateWorkspaceRole
) {
}
