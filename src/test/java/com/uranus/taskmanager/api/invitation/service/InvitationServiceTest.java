package com.uranus.taskmanager.api.invitation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uranus.taskmanager.api.invitation.InvitationStatus;
import com.uranus.taskmanager.api.invitation.domain.Invitation;
import com.uranus.taskmanager.api.invitation.dto.response.InvitationAcceptResponse;
import com.uranus.taskmanager.api.invitation.exception.InvalidInvitationStatusException;
import com.uranus.taskmanager.api.invitation.exception.InvitationNotFoundException;
import com.uranus.taskmanager.api.invitation.repository.InvitationRepository;
import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.member.repository.MemberRepository;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspace.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;
import com.uranus.taskmanager.api.workspacemember.repository.WorkspaceMemberRepository;
import com.uranus.taskmanager.fixture.dto.LoginMemberDtoFixture;
import com.uranus.taskmanager.fixture.entity.InvitationEntityFixture;
import com.uranus.taskmanager.fixture.entity.MemberEntityFixture;
import com.uranus.taskmanager.fixture.entity.WorkspaceEntityFixture;
import com.uranus.taskmanager.fixture.entity.WorkspaceMemberEntityFixture;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

	@InjectMocks
	private InvitationService invitationService;

	@Mock
	private WorkspaceRepository workspaceRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private WorkspaceMemberRepository workspaceMemberRepository;
	@Mock
	private InvitationRepository invitationRepository;

	WorkspaceEntityFixture workspaceEntityFixture;
	MemberEntityFixture memberEntityFixture;
	WorkspaceMemberEntityFixture workspaceMemberEntityFixture;
	InvitationEntityFixture invitationEntityFixture;
	LoginMemberDtoFixture loginMemberDtoFixture;

	@BeforeEach
	public void setup() {
		workspaceEntityFixture = new WorkspaceEntityFixture();
		memberEntityFixture = new MemberEntityFixture();
		workspaceMemberEntityFixture = new WorkspaceMemberEntityFixture();
		invitationEntityFixture = new InvitationEntityFixture();
		loginMemberDtoFixture = new LoginMemberDtoFixture();
	}

	@Test
	@DisplayName("유효한 로그인 정보와 코드를 사용해서 초대를 수락하면 초대가 저장된다")
	void test1() {
		// given
		String workspaceCode = "testcode";
		String loginId = "user123";
		String email = "user123@test.com";
		Long memberId = 1L;

		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		Member member = memberEntityFixture.createMember(loginId, email);
		WorkspaceMember workspaceMember = workspaceMemberEntityFixture.createCollaboratorWorkspaceMember(member,
			workspace);
		Invitation invitation = invitationEntityFixture.createPendingInvitation(workspace, member);

		when(invitationRepository.findByWorkspaceCodeAndMemberId(workspaceCode, memberId)).thenReturn(
			Optional.of(invitation));
		when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(workspaceMember);

		// when
		InvitationAcceptResponse acceptResponse = invitationService.acceptInvitation(memberId, workspaceCode);

		// then
		assertThat(acceptResponse.getWorkspaceDetail().getCode()).isEqualTo(workspaceCode);

	}

	@Test
	@DisplayName("초대가 성공하면 초대의 상태가 ACCEPTED로 변경된다")
	void test2() {
		// given
		String workspaceCode = "testcode";
		Long memberId = 1L;
		String loginId = "user123";
		String email = "user123@test.com";

		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		Member member = memberEntityFixture.createMember(loginId, email);
		WorkspaceMember workspaceMember = workspaceMemberEntityFixture.createCollaboratorWorkspaceMember(member,
			workspace);
		Invitation invitation = invitationEntityFixture.createPendingInvitation(workspace, member);

		when(invitationRepository.findByWorkspaceCodeAndMemberId(workspaceCode, memberId))
			.thenReturn(Optional.of(invitation));
		when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(workspaceMember);

		// when
		invitationService.acceptInvitation(memberId, workspaceCode);
		Optional<Invitation> acceptedInvitation = invitationRepository.findByWorkspaceCodeAndMemberId(
			workspaceCode, memberId);

		// then
		assertThat(acceptedInvitation).isPresent();
		assertThat(acceptedInvitation.get().getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
	}

	@Test
	@DisplayName("초대의 수락이 성공하면 해당 워크스페이스에 참여 된다")
	void test3() {
		// given
		String workspaceCode = "testcode";
		String loginId = "user123";
		String email = "user123@test.com";
		Long memberId = 1L;

		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		Member member = memberEntityFixture.createMember(loginId, email);
		WorkspaceMember workspaceMember = workspaceMemberEntityFixture.createCollaboratorWorkspaceMember(member,
			workspace);
		Invitation invitation = invitationEntityFixture.createPendingInvitation(workspace, member);

		when(invitationRepository.findByWorkspaceCodeAndMemberId(workspaceCode, memberId)).thenReturn(
			Optional.of(invitation));
		when(workspaceMemberRepository.save(any(WorkspaceMember.class))).thenReturn(workspaceMember);

		// when
		InvitationAcceptResponse acceptResponse = invitationService.acceptInvitation(memberId, workspaceCode);

		// then
		assertThat(acceptResponse.getWorkspaceDetail().getCode()).isEqualTo(workspaceCode);
		verify(workspaceMemberRepository, times(1)).save(any(WorkspaceMember.class));
	}

	@Test
	@DisplayName("유효하지 않은 코드를 사용해서 초대를 수락하면 예외가 발생한다")
	void test4() {
		// given
		String workspaceCode = "invalidcode";
		Long memberId = 1L;

		when(invitationRepository.findByWorkspaceCodeAndMemberId(workspaceCode, memberId)).thenReturn(
			Optional.empty());

		// when & then
		assertThatThrownBy(() -> invitationService.acceptInvitation(memberId, workspaceCode)).isInstanceOf(
			InvitationNotFoundException.class);

	}

	@Test
	@DisplayName("존재하는 초대의 상태가 PENDING이 아니면 예외가 발생한다")
	void test5() {
		// given
		String workspaceCode = "invalidcode";
		String loginId = "user123";
		String email = "user123@test.com";
		Long memberId = 1L;

		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		Member member = memberEntityFixture.createMember(loginId, email);
		Invitation invitation = invitationEntityFixture.createAcceptedInvitation(workspace, member);

		when(invitationRepository.findByWorkspaceCodeAndMemberId(workspaceCode, memberId))
			.thenReturn(Optional.of(invitation));

		// when & then
		assertThatThrownBy(() -> invitationService.acceptInvitation(memberId, workspaceCode)).isInstanceOf(
			InvalidInvitationStatusException.class);

	}

	@Test
	@DisplayName("초대의 거절이 성공하면 초대 상태가 REJECTED로 변경된다")
	void test6() {
		// given
		String workspaceCode = "testcode";
		Long memberId = 1L;
		String loginId = "user123";
		String email = "user123@test.com";

		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		Member member = memberEntityFixture.createMember(loginId, email);
		Invitation invitation = invitationEntityFixture.createPendingInvitation(workspace, member);

		when(invitationRepository.findByWorkspaceCodeAndMemberId(workspaceCode, memberId)).thenReturn(
			Optional.of(invitation));

		// when
		invitationService.rejectInvitation(memberId, workspaceCode);
		Optional<Invitation> rejectedInvitation = invitationRepository.findByWorkspaceCodeAndMemberId(
			workspaceCode, memberId);

		// then
		assertThat(rejectedInvitation).isPresent();
		assertThat(rejectedInvitation.get().getStatus()).isEqualTo(InvitationStatus.REJECTED);
	}

}
