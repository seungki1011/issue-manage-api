package com.uranus.taskmanager.api.workspacemember.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.security.session.SessionAttributes;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspace.exception.InvalidWorkspacePasswordException;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.request.JoinWorkspaceRequest;
import com.uranus.taskmanager.api.workspacemember.presentation.dto.response.JoinWorkspaceResponse;
import com.uranus.taskmanager.fixture.entity.InvitationEntityFixture;
import com.uranus.taskmanager.fixture.entity.MemberEntityFixture;
import com.uranus.taskmanager.fixture.entity.WorkspaceEntityFixture;
import com.uranus.taskmanager.fixture.entity.WorkspaceMemberEntityFixture;
import com.uranus.taskmanager.helper.ControllerTestHelper;

class MemberWorkspaceControllerTest extends ControllerTestHelper {

	WorkspaceEntityFixture workspaceEntityFixture;
	MemberEntityFixture memberEntityFixture;
	WorkspaceMemberEntityFixture workspaceMemberEntityFixture;
	InvitationEntityFixture invitationEntityFixture;

	@BeforeEach
	public void setup() {
		workspaceEntityFixture = new WorkspaceEntityFixture();
		memberEntityFixture = new MemberEntityFixture();
		workspaceMemberEntityFixture = new WorkspaceMemberEntityFixture();
		invitationEntityFixture = new InvitationEntityFixture();
	}

	@Test
	@DisplayName("POST /members/workspaces/{code} - 워크스페이스 참여 요청을 성공하는 경우 200을 응답 받는다")
	void test11() throws Exception {
		// Session 모킹
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		// given
		String workspaceCode = "TESTCODE";
		String loginId = "member1";
		String email = "member1@test.com";

		Workspace workspace = workspaceEntityFixture.createWorkspace(workspaceCode);
		Member member = memberEntityFixture.createMember(loginId, email);
		WorkspaceMember workspaceMember = workspaceMemberEntityFixture.createCollaboratorWorkspaceMember(member,
			workspace);
		JoinWorkspaceRequest request = new JoinWorkspaceRequest();

		JoinWorkspaceResponse response = JoinWorkspaceResponse.from(workspaceMember);

		when(memberWorkspaceCommandService.joinWorkspace(eq(workspaceCode),
			any(JoinWorkspaceRequest.class),
			anyLong())).thenReturn(response);

		// when & then
		mockMvc.perform(post("/api/v1/members/workspaces/{code}", workspaceCode)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("Joined workspace"))
			.andDo(print());
	}

	@Test
	@DisplayName("POST /members/workspaces/{code} - 워크스페이스 참여 요청 시 비밀번호가 불일치하는 경우 401을 응답 받는다")
	void test12() throws Exception {
		// Session 모킹
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		// given
		String workspaceCode = "TESTCODE";
		String invalidPassword = "invalid1234!";

		JoinWorkspaceRequest request = new JoinWorkspaceRequest(invalidPassword);

		when(memberWorkspaceCommandService.joinWorkspace(eq("TESTCODE"), any(JoinWorkspaceRequest.class), anyLong()))
			.thenThrow(new InvalidWorkspacePasswordException());

		// when & then
		mockMvc.perform(post("/api/v1/members/workspaces/{code}", workspaceCode)
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("The given workspace password is invalid"))
			.andDo(print());
	}
}