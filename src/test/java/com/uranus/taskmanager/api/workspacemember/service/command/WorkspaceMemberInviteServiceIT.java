package com.uranus.taskmanager.api.workspacemember.service.command;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.uranus.taskmanager.api.invitation.domain.Invitation;
import com.uranus.taskmanager.api.invitation.domain.InvitationStatus;
import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceRole;
import com.uranus.taskmanager.api.workspacemember.exception.NoValidMembersToInviteException;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.InviteMembersRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.InviteMembersResponse;
import com.uranus.taskmanager.helper.ServiceIntegrationTestHelper;

class WorkspaceMemberInviteServiceIT extends ServiceIntegrationTestHelper {

	@AfterEach
	void tearDown() {
		databaseCleaner.execute();
	}

	@Test
	@DisplayName("초대가 성공하면 초대가 PENDING 상태로 저장된다")
	void testInviteMembers_ifSuccess_invitationStatusIsPending() {
		// given
		workspaceRepositoryFixture.createWorkspace(
			"Test Workspace",
			"Test Description",
			"TESTCODE",
			null
		);

		Member member = memberRepositoryFixture.createMember(
			"member1",
			"member1@test.com",
			"password1234!"
		);

		InviteMembersRequest request = InviteMembersRequest.of(Set.of("member1"));

		// when
		workspaceMemberInviteService.inviteMembers("TESTCODE", request);

		// then
		Invitation invitation = invitationRepository.findByWorkspaceCodeAndMemberId("TESTCODE", member.getId()).get();
		assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
	}

	@Test
	@DisplayName("존재하지 않는 멤버에 대한 초대는 초대 대상에서 제외된다")
	void testInviteMembers_ifMemberNotExist_excludedFromInvite() {
		// given
		workspaceRepositoryFixture.createWorkspace(
			"Test Workspace",
			"Test Description",
			"TESTCODE",
			null
		);

		Member member = memberRepositoryFixture.createMember(
			"member1",
			"member1@test.com",
			"password1234!"
		);

		InviteMembersRequest request = InviteMembersRequest.of(
			Set.of("nonExistentMember1", "nonExistentMember2", "member1")
		);

		// when
		InviteMembersResponse response = workspaceMemberInviteService.inviteMembers("TESTCODE", request);

		// then
		assertThat(response.getTotalInvitedMembers()).isEqualTo(1L);
		assertThat(response.getInvitedMembers().get(0)).isEqualTo(InviteMembersResponse.InvitedMember.from(member));
	}

	@Test
	@DisplayName("다수의 멤버를 초대할 때 존재하지 않는 멤버는 대상에서 제외되고, 존재하는 멤버는 초대된다")
	void testInviteMembers_memberNotExistExcluded_memberExistInvited() {
		// given
		workspaceRepositoryFixture.createWorkspace(
			"Test Workspace",
			"Test Description",
			"TESTCODE",
			null
		);

		Member member2 = memberRepositoryFixture.createMember(
			"member2",
			"member2@test.com",
			"password1234!"
		);

		Member member3 = memberRepositoryFixture.createMember(
			"member3",
			"member3@test.com",
			"password1234!"
		);

		InviteMembersRequest request = InviteMembersRequest.of(Set.of("nonExistingMember", "member2", "member3"));

		// when
		InviteMembersResponse response = workspaceMemberInviteService.inviteMembers("TESTCODE", request);

		// then
		assertThat(response.getTotalInvitedMembers()).isEqualTo(2L);

		assertThat(response.getInvitedMembers()).contains(
			InviteMembersResponse.InvitedMember.from(member2),
			InviteMembersResponse.InvitedMember.from(member3)
		);
	}

	@Test
	@DisplayName("해당 워크스페이스에 이미 참여하고 있는 멤버는 초대 대상에서 제외된다")
	void testInviteMembers_AlreadyJoinedMemberExcluded() {
		// given
		Workspace workspace = workspaceRepositoryFixture.createWorkspace(
			"Test Workspace",
			"Test Description",
			"TESTCODE",
			null
		);

		Member member2 = memberRepositoryFixture.createMember(
			"member2",
			"member2@test.com",
			"password1234!"
		);

		Member member = memberRepositoryFixture.createMember(
			"member1",
			"member1@test.com",
			"password1234!"
		);
		workspaceRepositoryFixture.addMemberToWorkspace(member, workspace, WorkspaceRole.COLLABORATOR);

		InviteMembersRequest request = InviteMembersRequest.of(
			Set.of("member2", "member1")
		);

		// when
		InviteMembersResponse response = workspaceMemberInviteService.inviteMembers("TESTCODE", request);

		// then
		assertThat(response.getTotalInvitedMembers()).isEqualTo(1L);
		assertThat(response.getInvitedMembers().get(0)).isEqualTo(InviteMembersResponse.InvitedMember.from(member2));
		assertThat(response.getWorkspaceCode()).isEqualTo("TESTCODE");
	}

	@Test
	@DisplayName("멤버 식별자들을 필터링 후 초대 대상에 대한 리스트가 비어 있으면 예외가 발생한다")
	void testInviteMembers_ifListOfInvitedMembersEmpty_throwException() {
		// given
		Workspace workspace = workspaceRepositoryFixture.createWorkspace(
			"Test Workspace",
			"Test Description",
			"TESTCODE",
			null
		);

		Member member = memberRepositoryFixture.createMember(
			"member1",
			"member1@test.com",
			"password1234!"
		);
		workspaceRepositoryFixture.addMemberToWorkspace(member, workspace, WorkspaceRole.COLLABORATOR);

		InviteMembersRequest request = InviteMembersRequest.of(Set.of("member1"));

		// when & then
		assertThatThrownBy(() -> workspaceMemberInviteService.inviteMembers("TESTCODE", request)).isInstanceOf(
			NoValidMembersToInviteException.class);
	}
}