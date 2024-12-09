package com.uranus.taskmanager.api.workspacemember;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WorkspaceRole {
	OWNER(4),
	MANAGER(3),
	COLLABORATOR(2),
	VIEWER(1);

	private final int level;
}
