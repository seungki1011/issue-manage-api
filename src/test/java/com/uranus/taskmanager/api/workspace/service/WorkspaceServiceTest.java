package com.uranus.taskmanager.api.workspace.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uranus.taskmanager.api.auth.dto.request.LoginMemberDto;
import com.uranus.taskmanager.api.invitation.InvitationStatus;
import com.uranus.taskmanager.api.invitation.domain.Invitation;
import com.uranus.taskmanager.api.invitation.exception.InvitationAlreadyExistsException;
import com.uranus.taskmanager.api.invitation.repository.InvitationRepository;
import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.member.exception.MemberNotFoundException;
import com.uranus.taskmanager.api.member.repository.MemberRepository;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspace.dto.request.InviteMemberRequest;
import com.uranus.taskmanager.api.workspace.dto.request.InviteMembersRequest;
import com.uranus.taskmanager.api.workspace.dto.response.InviteMemberResponse;
import com.uranus.taskmanager.api.workspace.dto.response.InviteMembersResponse;
import com.uranus.taskmanager.api.workspace.dto.response.WorkspaceResponse;
import com.uranus.taskmanager.api.workspace.exception.WorkspaceNotFoundException;
import com.uranus.taskmanager.api.workspace.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;
import com.uranus.taskmanager.api.workspacemember.exception.MemberAlreadyParticipatingException;
import com.uranus.taskmanager.api.workspacemember.repository.WorkspaceMemberRepository;
import com.uranus.taskmanager.fixture.TestFixture;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

	@InjectMocks
	private WorkspaceService workspaceService;

	@Mock
	private WorkspaceRepository workspaceRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private WorkspaceMemberRepository workspaceMemberRepository;
	@Mock
	private InvitationRepository invitationRepository;

	TestFixture testFixture;

	@BeforeEach
	public void setup() {
		testFixture = new TestFixture();
	}

	@Test
	@DisplayName("유효한 워크스페이스 코드로 워크스페이스를 조회하면, 워크스페이스를 반환한다")
	void test2() {
		String workspaceCode = "testcode";
		Workspace workspace = Workspace.builder()
			.code(workspaceCode)
			.name("Test Workspace")
			.description("Test Description")
			.build();

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.of(workspace));

		WorkspaceResponse response = workspaceService.get(workspaceCode);

		assertThat(response).isNotNull();
		assertThat(response.getCode()).isEqualTo(workspaceCode);
		verify(workspaceRepository, times(1)).findByCode(workspaceCode);
	}

	@Test
	@DisplayName("유효하지 워크스페이스 코드로 워크스페이스를 조회하면, WorkspaceNotFoundException 발생")
	void test3() {
		String workspaceCode = "INVALIDCODE";

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> workspaceService.get(workspaceCode))
			.isInstanceOf(WorkspaceNotFoundException.class);

		verify(workspaceRepository, times(1)).findByCode(workspaceCode);
	}

	@Test
	@DisplayName("초대가 성공하면 초대가 PENDING 상태로 저장된다")
	void test8() {
		// given
		String workspaceCode = "TESTCODE";
		String loginId = "user123";
		String email = "user123@test.com";

		String invitedLoginId = "inviteduser123";
		String invitedEmail = "inviteduser123@test.com";

		Workspace workspace = testFixture.createWorkspace(workspaceCode);
		LoginMemberDto loginMember = testFixture.createLoginMemberDto(loginId, email);

		Member invitedMember = testFixture.createMember(invitedLoginId, invitedEmail);

		InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(invitedLoginId);
		String identifier = inviteMemberRequest.getMemberIdentifier();

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.of(workspace));

		when(memberRepository.findByLoginIdOrEmail(identifier, identifier))
			.thenReturn(Optional.of(invitedMember));

		// when
		InviteMemberResponse inviteMemberResponse = workspaceService.inviteMember(workspaceCode, inviteMemberRequest,
			loginMember);

		// then
		assertThat(inviteMemberResponse.getStatus()).isEqualTo(InvitationStatus.PENDING);
		verify(invitationRepository, times(1)).save(any(Invitation.class));
	}

	@Test
	@DisplayName("유효하지 않은 워크스페이스 코드로 멤버를 초대하면 WorkspaceNotFoundException이 발생한다")
	void test4() {
		// given
		String workspaceCode = "INVALIDCODE";
		String loginId = "user123";
		String email = "user123@test.com";

		String invitedLoginId = "inviteduser123";

		LoginMemberDto loginMember = testFixture.createLoginMemberDto(loginId, email);

		InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(invitedLoginId);

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(
			() -> workspaceService.inviteMember(workspaceCode, inviteMemberRequest, loginMember)).isInstanceOf(
			WorkspaceNotFoundException.class);

	}

	@Test
	@DisplayName("유효하지 않은 멤버 식별자를 사용해서 멤버를 초대하면 MemberNotFoundException이 발생한다")
	void test5() {
		// given
		String workspaceCode = "TESTCODE";
		String loginId = "user123";
		String email = "user123@test.com";

		String invitedLoginId = "inviteduser123";

		Workspace workspace = testFixture.createWorkspace(workspaceCode);
		LoginMemberDto loginMember = testFixture.createLoginMemberDto(loginId, email);

		InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(invitedLoginId);
		String identifier = inviteMemberRequest.getMemberIdentifier();

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.of(workspace));

		when(memberRepository.findByLoginIdOrEmail(identifier, identifier))
			.thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(
			() -> workspaceService.inviteMember(workspaceCode, inviteMemberRequest, loginMember)).isInstanceOf(
			MemberNotFoundException.class);
	}

	@Test
	@DisplayName("멤버를 다시 초대할 때 초대가 존재하고 초대 상태가 PENDING이면 InvitationAlreadyExistsException이 발생한다")
	void test6() {
		// given
		String workspaceCode = "TESTCODE";
		String loginId = "user123";
		String email = "user123@test.com";

		String invitedLoginId = "inviteduser123";
		String invitedEmail = "inviteduser123@test.com";

		Workspace workspace = testFixture.createWorkspace(workspaceCode);
		LoginMemberDto loginMember = testFixture.createLoginMemberDto(loginId, email);

		Member invitedMember = testFixture.createMember(invitedLoginId, invitedEmail);
		Invitation invitation = testFixture.createPendingInvitation(workspace, invitedMember);

		InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(invitedLoginId);
		String identifier = inviteMemberRequest.getMemberIdentifier();

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.of(workspace));

		when(memberRepository.findByLoginIdOrEmail(identifier, identifier))
			.thenReturn(Optional.of(invitedMember));

		when(invitationRepository.findByWorkspaceAndMember(workspace, invitedMember)).thenReturn(
			Optional.of(invitation));

		// when & then
		assertThatThrownBy(
			() -> workspaceService.inviteMember(workspaceCode, inviteMemberRequest, loginMember)).isInstanceOf(
			InvitationAlreadyExistsException.class);
	}

	@Test
	@DisplayName("해당 워크스페이스에 이미 참여하고 있는 멤버를 다시 초대하면 MemberAlreadyParticipatingException이 발생한다")
	void test7() {
		// given
		String workspaceCode = "TESTCODE";
		String loginId = "user123";
		String email = "user123@test.com";

		String invitedLoginId = "inviteduser123";
		String invitedEmail = "inviteduser123@test.com";

		Workspace workspace = testFixture.createWorkspace(workspaceCode);
		LoginMemberDto loginMember = testFixture.createLoginMemberDto(loginId, email);

		Member invitedMember = testFixture.createMember(invitedLoginId, invitedEmail);
		WorkspaceMember workspaceMember = testFixture.createUserWorkspaceMember(invitedMember, workspace);

		InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(invitedLoginId);
		String identifier = inviteMemberRequest.getMemberIdentifier();

		when(workspaceRepository.findByCode(workspaceCode))
			.thenReturn(Optional.of(workspace));

		when(memberRepository.findByLoginIdOrEmail(identifier, identifier))
			.thenReturn(Optional.of(invitedMember));

		when(workspaceMemberRepository.findByMemberLoginIdAndWorkspaceCode(invitedLoginId, workspaceCode))
			.thenReturn(Optional.of(workspaceMember));

		// when & then
		assertThatThrownBy(
			() -> workspaceService.inviteMember(workspaceCode, inviteMemberRequest, loginMember)).isInstanceOf(
			MemberAlreadyParticipatingException.class);
	}

	@Test
	@DisplayName("다수의 멤버를 초대 시 모든 멤버의 초대를 성공하면 실패한 멤버의 리스트는 비어있다")
	void test9() {
		// given
		String workspaceCode = "TESTCODE";
		String member1Id = "member1";
		String member2Id = "member2";
		String member1Email = "member1@test.com";
		String member2Email = "member2@test.com";

		List<String> memberIdentifiers = List.of(member1Id, member2Id);
		InviteMembersRequest inviteMembersRequest = new InviteMembersRequest(memberIdentifiers);

		// Mock LoginMemberDto
		LoginMemberDto loginMember = testFixture.createLoginMemberDto("inviter", "inviter@test.com");

		// Mock Workspace
		Workspace workspace = testFixture.createWorkspace(workspaceCode);
		when(workspaceRepository.findByCode(workspaceCode)).thenReturn(Optional.of(workspace));

		// Mock Member
		Member member1 = testFixture.createMember(member1Id, member1Email);
		Member member2 = testFixture.createMember(member2Id, member2Email);
		when(memberRepository.findByLoginIdOrEmail(member1Id, member1Id))
			.thenReturn(Optional.of(member1));
		when(memberRepository.findByLoginIdOrEmail(member2Id, member2Id))
			.thenReturn(Optional.of(member2));

		// Mock Invitation
		Invitation invitation1 = testFixture.createPendingInvitation(workspace, member1);
		Invitation invitation2 = testFixture.createPendingInvitation(workspace, member2);
		when(invitationRepository.save(any(Invitation.class)))
			.thenReturn(invitation1).thenReturn(invitation2);

		// then
		InviteMembersResponse response = workspaceService.inviteMembers(workspaceCode, inviteMembersRequest,
			loginMember);

		log.info("response = {}", response);

		assertThat(response.getInvitedMembers().size()).isEqualTo(2);
		assertThat(response.getFailedInvitedMembers()).isEmpty();

	}

}
