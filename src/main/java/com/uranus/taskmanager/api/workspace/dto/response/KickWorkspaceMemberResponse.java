package com.uranus.taskmanager.api.workspace.dto.response;

import com.uranus.taskmanager.api.workspacemember.WorkspaceRole;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class KickWorkspaceMemberResponse {

	/*
	 * Todo
	 *  - WorkspaceMemberDetail을 만들어서 사용하기
	 *  - 해당 DTO에 다음 필드 넣기
	 *  - id
	 *  - nickname
	 *  - role
	 */
	private String memberIdentifier;
	private String nickname;
	private WorkspaceRole role;

	@Builder
	public KickWorkspaceMemberResponse(String memberIdentifier, String nickname, WorkspaceRole role) {
		this.memberIdentifier = memberIdentifier;
		this.nickname = nickname;
		this.role = role;
	}

	public static KickWorkspaceMemberResponse from(String memberIdentifier, WorkspaceMember workspaceMember) {
		return KickWorkspaceMemberResponse.builder()
			.memberIdentifier(memberIdentifier)
			.nickname(workspaceMember.getNickname())
			.role(workspaceMember.getRole())
			.build();
	}
}
