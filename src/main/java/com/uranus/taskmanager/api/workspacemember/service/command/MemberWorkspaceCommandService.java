package com.uranus.taskmanager.api.workspacemember.service.command;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.member.domain.repository.MemberRepository;
import com.uranus.taskmanager.api.member.exception.MemberNotFoundException;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspace.domain.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspace.exception.WorkspaceNotFoundException;
import com.uranus.taskmanager.api.workspace.validator.WorkspaceValidator;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;
import com.uranus.taskmanager.api.workspacemember.domain.repository.WorkspaceMemberRepository;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.WorkspaceJoinRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.WorkspaceJoinResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberWorkspaceCommandService {
	/*
	 * Todo
	 *  - leaveWorkspace: 워크스페이스 떠나기(현재 OWNER 상태면 불가능)
	 */
	private final WorkspaceRepository workspaceRepository;
	private final MemberRepository memberRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final WorkspaceValidator workspaceValidator;

	/**
	 * 참여할 워크스페이스의 코드와 참여 요청을 통해
	 * 참여를 요청한 로그인 멤버를 해당 워크스페이스에 참여시킨다.
	 *
	 * @param code     - 워크스페이스의 고유 코드
	 * @param request  - 워크스페이스 참여 요청 객체
	 * @param memberId - 세션에서 꺼낸 멤버 id(PK)
	 * @return - 워크스페이스 참여 응답을 위한 DTO
	 */
	@Transactional
	public WorkspaceJoinResponse joinWorkspace(String code, WorkspaceJoinRequest request, Long memberId) {

		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		Member member = memberRepository.findById(memberId)
			.orElseThrow(MemberNotFoundException::new);

		Optional<WorkspaceMember> optionalWorkspaceMember = workspaceMemberRepository.findByMemberIdAndWorkspaceCode(
			memberId, code);
		if (optionalWorkspaceMember.isPresent()) {
			return WorkspaceJoinResponse.from(workspace, optionalWorkspaceMember.get(), true);
		}

		workspaceValidator.validatePasswordIfExists(workspace.getPassword(), request.getPassword());

		WorkspaceMember workspaceMember = WorkspaceMember.addCollaboratorWorkspaceMember(member, workspace);

		workspaceMemberRepository.save(workspaceMember);

		return WorkspaceJoinResponse.from(workspace, workspaceMember, false);
	}
}