package com.tissue.api.workspacemember.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import com.tissue.api.member.domain.Member;
import com.tissue.api.security.session.SessionAttributes;
import com.tissue.api.workspace.domain.Workspace;
import com.tissue.api.workspacemember.domain.WorkspaceMember;
import com.tissue.api.workspacemember.domain.WorkspaceRole;
import com.tissue.api.workspacemember.exception.NoValidMembersToInviteException;
import com.tissue.api.workspacemember.presentation.dto.request.InviteMembersRequest;
import com.tissue.api.workspacemember.presentation.dto.request.UpdateNicknameRequest;
import com.tissue.api.workspacemember.presentation.dto.request.UpdateRoleRequest;
import com.tissue.api.workspacemember.presentation.dto.response.InviteMembersResponse;
import com.tissue.api.workspacemember.presentation.dto.response.RemoveWorkspaceMemberResponse;
import com.tissue.api.workspacemember.presentation.dto.response.UpdateNicknameResponse;
import com.tissue.api.workspacemember.presentation.dto.response.UpdateRoleResponse;
import com.tissue.fixture.entity.MemberEntityFixture;
import com.tissue.fixture.entity.WorkspaceEntityFixture;
import com.tissue.fixture.entity.WorkspaceMemberEntityFixture;
import com.tissue.helper.ControllerTestHelper;

class WorkspaceMembershipControllerTest extends ControllerTestHelper {

	WorkspaceEntityFixture workspaceEntityFixture;
	MemberEntityFixture memberEntityFixture;
	WorkspaceMemberEntityFixture workspaceMemberEntityFixture;

	@BeforeEach
	public void setup() {
		workspaceEntityFixture = new WorkspaceEntityFixture();
		memberEntityFixture = new MemberEntityFixture();
		workspaceMemberEntityFixture = new WorkspaceMemberEntityFixture();
	}

	@Test
	@DisplayName("DELETE /workspaces/{code}/members/{memberId} - 워크스페이스에서 멤버를 추방하는데 성공하면 200을 응답받는다")
	void test13() throws Exception {
		// Session 모킹
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		// given
		String workspaceCode = "TESTCODE";

		Member member = memberEntityFixture.createMember("member1", "member1@test.com");
		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		WorkspaceMember workspaceMember = workspaceMemberEntityFixture
			.createCollaboratorWorkspaceMember(member, workspace);

		RemoveWorkspaceMemberResponse response = RemoveWorkspaceMemberResponse.from(2L, workspaceMember);

		when(workspaceMemberCommandService.removeWorkspaceMember(eq(workspaceCode), eq(2L), anyLong()))
			.thenReturn(response);

		// when & then
		mockMvc.perform(delete("/api/v1/workspaces/{code}/members/{memberId}", workspaceCode, 2L)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Member was removed from this workspace"))
			.andExpect(jsonPath("$.data.memberId").value(2L))
			.andDo(print());

	}

	@Test
	@DisplayName("PATCH /workspaces/{code}/members/nickname - 별칭을 변경하는데 성공하면 200을 응답받는다")
	void testUpdateNickname_ifSuccess_return200() throws Exception {
		// given
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		String workspaceCode = "TESTCODE";

		WorkspaceMember workspaceMember = workspaceMemberEntityFixture.createManagerWorkspaceMember(
			Member.builder()
				.loginId("tester")
				.email("test@test.com")
				.build(),
			Workspace.builder()
				.code(workspaceCode)
				.build()
		);
		workspaceMember.updateNickname("newNickname");

		UpdateNicknameResponse response = UpdateNicknameResponse.from(workspaceMember);

		when(workspaceMemberCommandService.updateNickname(
			eq(workspaceCode),
			eq(1L),
			any(UpdateNicknameRequest.class))
		)
			.thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/workspaces/{code}/members/nickname", workspaceCode)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new UpdateNicknameRequest("newNickname"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Nickname updated."))
			.andExpect(jsonPath("$.data.nickname").value("newNickname"))
			.andDo(print());
	}

	@Test
	@DisplayName("PATCH /workspaces/{code}/members/{memberId}/role - 워크스페이스 멤버의 권한을 변경하는데 성공하면 200을 응답받는다")
	void test14() throws Exception {
		// Session 모킹
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		// given
		String workspaceCode = "TESTCODE";

		UpdateRoleRequest request = new UpdateRoleRequest(WorkspaceRole.MANAGER);

		WorkspaceMember target = workspaceMemberEntityFixture.createManagerWorkspaceMember(
			Member.builder()
				.loginId("member1")
				.build(),
			Workspace.builder()
				.code("TESTCODE")
				.build()
		);

		UpdateRoleResponse response = UpdateRoleResponse.from(target);

		when(workspaceMemberCommandService.updateWorkspaceMemberRole(
			eq(workspaceCode),
			anyLong(),
			anyLong(),
			any(UpdateRoleRequest.class))
		).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/workspaces/{code}/members/{memberId}/role", workspaceCode, 2L)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Member's role for this workspace was updated"))
			.andDo(print());
	}

	@Test
	@DisplayName("PATCH /workspaces/{code}/members/{memberId}/role - 권한을 변경하는데 성공하면 해당 워크스페이스 멤버의 상세 정보를 응답 데이터로 받는다")
	void test15() throws Exception {
		// Session 모킹
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		// given
		String workspaceCode = "TESTCODE";

		UpdateRoleRequest request = new UpdateRoleRequest(WorkspaceRole.MANAGER);

		WorkspaceMember target = workspaceMemberEntityFixture.createManagerWorkspaceMember(
			Member.builder()
				.loginId("member1")
				.email("member1@test.com")
				.build(),
			Workspace.builder()
				.code("TESTCODE")
				.build()
		);

		UpdateRoleResponse response = UpdateRoleResponse.from(target);

		when(workspaceMemberCommandService.updateWorkspaceMemberRole(
			eq(workspaceCode),
			anyLong(),
			anyLong(),
			any(UpdateRoleRequest.class))
		).thenReturn(response);

		// when & then
		mockMvc.perform(patch("/api/v1/workspaces/{code}/members/{memberId}/role", workspaceCode, 2L)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Member's role for this workspace was updated"))
			.andExpect(jsonPath("$.data.role").value("MANAGER"))
			.andDo(print());
	}

	@Test
	@DisplayName("POST /workspaces/{code}/members/invite - 워크스페이스 멤버 초대 성공")
	void inviteMembers_Success() throws Exception {
		// given
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		String workspaceCode = "TESTCODE";
		Set<String> memberIdentifiers = new HashSet<>(Arrays.asList(
			"john@example.com",
			"jane@example.com"
		));
		InviteMembersRequest request = InviteMembersRequest.of(memberIdentifiers);

		List<InviteMembersResponse.InvitedMember> invitedMembers = Arrays.asList(
			new InviteMembersResponse.InvitedMember(1L, "john@example.com"),
			new InviteMembersResponse.InvitedMember(2L, "jane@example.com")
		);

		InviteMembersResponse response = InviteMembersResponse.of(workspaceCode, invitedMembers);

		// 로그인 멤버 및 권한 설정
		when(workspaceMemberInviteService.inviteMembers(workspaceCode, request)).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/workspaces/{code}/members/invite", workspaceCode)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("200"))
			.andExpect(jsonPath("$.message").value("Members invited"))
			.andExpect(jsonPath("$.data.workspaceCode").value(workspaceCode))
			.andExpect(jsonPath("$.data.invitedMembers[0].id").value(1))
			.andExpect(jsonPath("$.data.invitedMembers[0].email").value("john@example.com"))
			.andExpect(jsonPath("$.data.invitedMembers[1].id").value(2))
			.andExpect(jsonPath("$.data.invitedMembers[1].email").value("jane@example.com"))
			.andDo(print());

		verify(workspaceMemberInviteService).inviteMembers(workspaceCode, request);
	}

	@Test
	@DisplayName("POST /workspaces/{code}/members/invite - 비어있는 멤버 목록으로 초대 요청 시 요청 검증 실패")
	void inviteMembers_Fail_EmptyMemberList() throws Exception {
		// given
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		String workspaceCode = "TESTCODE";
		Set<String> memberIdentifiers = new HashSet<>();
		InviteMembersRequest request = InviteMembersRequest.of(memberIdentifiers);

		// when & then
		mockMvc.perform(post("/api/v1/workspaces/{code}/members/invite", workspaceCode)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("One or more fields have validation errors"));

		verify(workspaceMemberInviteService, never()).inviteMembers(any(), any());
	}

	@Test
	@DisplayName("POST /workspaces/{code}/members/invite - 모든 멤버 식별자가 초대 대상에서 제외되면 예외가 발생한다")
	void inviteMembers_ifAllIdentifiersExcluded_throwsException() throws Exception {
		// given
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		String workspaceCode = "TESTCODE";
		Set<String> memberIdentifiers = Set.of("excludedMember1", "excludedMember2", "excludedMember3");
		InviteMembersRequest request = InviteMembersRequest.of(memberIdentifiers);

		when(workspaceMemberInviteService.inviteMembers(workspaceCode, request)).thenThrow(
			new NoValidMembersToInviteException());

		// when & then
		mockMvc.perform(post("/api/v1/workspaces/{code}/members/invite", workspaceCode)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("No avaliable members were found for invitation."));
	}
}
