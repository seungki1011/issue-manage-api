package com.uranus.taskmanager.api.workspacemember.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uranus.taskmanager.api.common.exception.CommonException;
import com.uranus.taskmanager.api.invitation.InvitationStatus;
import com.uranus.taskmanager.api.invitation.domain.Invitation;
import com.uranus.taskmanager.api.invitation.domain.repository.InvitationRepository;
import com.uranus.taskmanager.api.invitation.exception.InvitationAlreadyExistsException;
import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.member.domain.repository.MemberRepository;
import com.uranus.taskmanager.api.member.exception.MemberNotFoundException;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspace.domain.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspace.exception.WorkspaceNotFoundException;
import com.uranus.taskmanager.api.workspace.validator.WorkspaceValidator;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;
import com.uranus.taskmanager.api.workspacemember.domain.repository.WorkspaceMemberRepository;
import com.uranus.taskmanager.api.workspacemember.exception.AlreadyJoinedWorkspaceException;
import com.uranus.taskmanager.api.workspacemember.exception.MemberNotInWorkspaceException;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.InviteMemberRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.InviteMembersRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.KickWorkspaceMemberRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.TransferWorkspaceOwnershipRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.WorkspaceJoinRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.FailedInvitedMember;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.InviteMemberResponse;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.InviteMembersResponse;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.InvitedMember;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.KickWorkspaceMemberResponse;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.TransferWorkspaceOwnershipResponse;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.UpdateWorkspaceMemberRoleResponse;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.WorkspaceJoinResponse;
import com.uranus.taskmanager.api.workspacemember.validator.WorkspaceMemberValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

	private final WorkspaceRepository workspaceRepository;
	private final MemberRepository memberRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final InvitationRepository invitationRepository;

	private final WorkspaceMemberValidator workspaceMemberValidator;
	private final WorkspaceValidator workspaceValidator;

	@Transactional
	public InviteMemberResponse inviteMember(String code, InviteMemberRequest request) {

		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		Member invitedMember = memberRepository.findByMemberIdentifier(request.getMemberIdentifier())
			.orElseThrow(MemberNotFoundException::new);

		workspaceMemberValidator.validateIfAlreadyJoined(invitedMember.getId(), code);
		checkIfPendingInvitationExists(workspace, invitedMember);

		Invitation invitation = savePendingInvitation(workspace, invitedMember);

		return InviteMemberResponse.from(invitation);
	}

	/**
	 * Todo
	 *  - InvitedMember, FailedInvitedMember가 일치하지 않는 문제 때문에 equals & hashCode를 구현했다
	 *  - 추후에 중복 멤버 또는 identifier를 거르기 위해서 Set을 사용할 예정
	 *  - InvitedMember, FailedInvitedMember로 분리하지 않고 하나의 클래스를 만들어서 통합하기
	 */
	@Transactional
	public InviteMembersResponse inviteMembers(String code, InviteMembersRequest request) {
		// Todo: 일급 컬렉션으로 리팩토링하는 것을 고려. 관련 처리 로직을 해당 일급 컬렉션 클래스에서 정의
		List<InvitedMember> invitedMembers = new ArrayList<>();
		List<FailedInvitedMember> failedInvitedMembers = new ArrayList<>();

		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		for (String identifier : request.getMemberIdentifiers()) {
			try {
				Member invitedMember = memberRepository.findByMemberIdentifier(identifier)
					.orElseThrow(MemberNotFoundException::new);

				workspaceMemberValidator.validateIfAlreadyJoined(invitedMember.getId(), code);
				checkIfPendingInvitationExists(workspace, invitedMember);

				savePendingInvitation(workspace, invitedMember);
				addInvitedMember(invitedMembers, invitedMember);
			} catch (AlreadyJoinedWorkspaceException | InvitationAlreadyExistsException e) {
				String errorMessage = getErrorMessageFromException(e);
				addFailedInvitedMember(identifier, failedInvitedMembers, errorMessage);
			}
		}

		return new InviteMembersResponse(invitedMembers, failedInvitedMembers);
	}

	/**
	 * 참여할 워크스페이스의 코드와 참여 요청의 패스워드(null 허용)를 사용해서
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

	@Transactional
	public KickWorkspaceMemberResponse kickWorkspaceMember(String code, KickWorkspaceMemberRequest request) {

		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		String identifier = request.getMemberIdentifier();
		Member member = memberRepository.findByMemberIdentifier(identifier)
			.orElseThrow(MemberNotFoundException::new);

		WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberIdAndWorkspaceCode(member.getId(), code)
			.orElseThrow(MemberNotInWorkspaceException::new);

		workspaceMemberRepository.delete(workspaceMember);
		workspace.decreaseMemberCount();

		return KickWorkspaceMemberResponse.from(identifier, workspaceMember);
	}

	@Transactional
	public UpdateWorkspaceMemberRoleResponse updateWorkspaceMemberRole(String code,
		UpdateWorkspaceMemberRoleRequest request, Long requesterId) {

		WorkspaceMember requester = workspaceMemberRepository
			.findByMemberIdAndWorkspaceCode(requesterId, code)
			.orElseThrow(MemberNotInWorkspaceException::new);

		WorkspaceMember target = workspaceMemberRepository
			.findByMemberIdentifierAndWorkspaceCode(request.getMemberIdentifier(), code)
			.orElseThrow(MemberNotInWorkspaceException::new);

		workspaceMemberValidator.validateRoleUpdate(requester, target);

		target.updateRole(request.getUpdateWorkspaceRole());

		return UpdateWorkspaceMemberRoleResponse.from(target);
	}

	@Transactional
	public TransferWorkspaceOwnershipResponse transferWorkspaceOwnership(String code,
		TransferWorkspaceOwnershipRequest request, Long requesterId) {

		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		WorkspaceMember requester = workspaceMemberRepository
			.findByMemberIdAndWorkspaceId(requesterId, workspace.getId())
			.orElseThrow(MemberNotInWorkspaceException::new);

		WorkspaceMember target = workspaceMemberRepository.findByMemberIdentifierAndWorkspaceCode(
				request.getMemberIdentifier(), code)
			.orElseThrow(MemberNotInWorkspaceException::new);

		requester.updateRoleFromOwnerToManager();
		target.updateRoleToOwner();

		return TransferWorkspaceOwnershipResponse.from(target);
	}

	private void checkIfPendingInvitationExists(Workspace workspace, Member invitedMember) {
		invitationRepository.findByWorkspaceAndMember(workspace, invitedMember)
			.filter(invitation -> invitation.getStatus() == InvitationStatus.PENDING)
			.ifPresent(invitation -> {
				throw new InvitationAlreadyExistsException();
			});
	}

	private Invitation savePendingInvitation(Workspace workspace, Member invitedMember) {
		Invitation invitation = Invitation.builder()
			.workspace(workspace)
			.member(invitedMember)
			.status(InvitationStatus.PENDING)
			.build();
		invitationRepository.save(invitation);
		return invitation;
	}

	private static void addInvitedMember(List<InvitedMember> invitedMembers, Member invitedMember) {
		invitedMembers.add(new InvitedMember(invitedMember.getLoginId(),
			invitedMember.getEmail()));
	}

	private static void addFailedInvitedMember(String identifier, List<FailedInvitedMember> failedInvitedMembers,
		String errorMessage) {
		failedInvitedMembers.add(new FailedInvitedMember(identifier, errorMessage));
	}

	private static String getErrorMessageFromException(Exception exception) {
		return exception instanceof CommonException ? exception.getMessage() : "Invitation failed";
	}
}