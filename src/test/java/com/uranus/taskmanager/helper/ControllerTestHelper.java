package com.uranus.taskmanager.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uranus.taskmanager.api.global.config.WebMvcConfig;
import com.uranus.taskmanager.api.invitation.domain.repository.InvitationRepository;
import com.uranus.taskmanager.api.invitation.presentation.controller.InvitationController;
import com.uranus.taskmanager.api.invitation.service.command.InvitationCommandService;
import com.uranus.taskmanager.api.invitation.service.query.InvitationQueryService;
import com.uranus.taskmanager.api.issue.domain.repository.IssueRepository;
import com.uranus.taskmanager.api.issue.presentation.controller.IssueController;
import com.uranus.taskmanager.api.issue.service.IssueCommandService;
import com.uranus.taskmanager.api.member.domain.repository.MemberRepository;
import com.uranus.taskmanager.api.member.presentation.controller.MemberController;
import com.uranus.taskmanager.api.member.service.command.MemberCommandService;
import com.uranus.taskmanager.api.member.service.query.MemberQueryService;
import com.uranus.taskmanager.api.position.domain.repository.PositionRepository;
import com.uranus.taskmanager.api.position.presentation.controller.PositionController;
import com.uranus.taskmanager.api.position.service.command.PositionCommandService;
import com.uranus.taskmanager.api.position.service.query.PositionQueryService;
import com.uranus.taskmanager.api.security.authentication.presentation.controller.AuthenticationController;
import com.uranus.taskmanager.api.security.authentication.service.AuthenticationService;
import com.uranus.taskmanager.api.security.session.SessionManager;
import com.uranus.taskmanager.api.security.session.SessionValidator;
import com.uranus.taskmanager.api.workspace.domain.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspace.presentation.controller.WorkspaceController;
import com.uranus.taskmanager.api.workspace.service.command.WorkspaceCommandService;
import com.uranus.taskmanager.api.workspace.service.command.create.CheckCodeDuplicationService;
import com.uranus.taskmanager.api.workspace.service.query.WorkspaceQueryService;
import com.uranus.taskmanager.api.workspacemember.domain.repository.WorkspaceMemberRepository;
import com.uranus.taskmanager.api.workspacemember.presentation.controller.WorkspaceMemberInfoController;
import com.uranus.taskmanager.api.workspacemember.presentation.controller.WorkspaceMembershipController;
import com.uranus.taskmanager.api.workspacemember.presentation.controller.WorkspaceParticipationController;
import com.uranus.taskmanager.api.workspacemember.service.command.WorkspaceMemberCommandService;
import com.uranus.taskmanager.api.workspacemember.service.command.WorkspaceMemberInviteService;
import com.uranus.taskmanager.api.workspacemember.service.command.WorkspaceParticipationCommandService;
import com.uranus.taskmanager.api.workspacemember.service.query.WorkspaceParticipationQueryService;
import com.uranus.taskmanager.config.WebMvcTestConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebMvcTest(
	controllers = {
		AuthenticationController.class,
		InvitationController.class,
		WorkspaceController.class,
		WorkspaceMembershipController.class,
		WorkspaceParticipationController.class,
		WorkspaceMemberInfoController.class,
		MemberController.class,
		PositionController.class,
		IssueController.class
	},
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
			WebMvcConfig.class,
			HandlerMethodArgumentResolver.class,
			HandlerInterceptor.class
		})
	}
)
@Import(value = WebMvcTestConfig.class)
public abstract class ControllerTestHelper {
	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;

	/**
	 * Session
	 */
	@MockBean
	protected SessionManager sessionManager;
	@MockBean
	protected SessionValidator sessionValidator;

	/**
	 * Service
	 */
	@MockBean
	protected MemberCommandService memberCommandService;
	@MockBean
	protected MemberQueryService memberQueryService;
	@MockBean
	protected WorkspaceMemberCommandService workspaceMemberCommandService;
	@MockBean
	protected WorkspaceMemberInviteService workspaceMemberInviteService;
	@MockBean
	protected WorkspaceParticipationQueryService workspaceParticipationQueryService;
	@MockBean
	protected WorkspaceParticipationCommandService workspaceParticipationCommandService;
	@MockBean
	protected CheckCodeDuplicationService workspaceCreateService;
	@MockBean
	protected WorkspaceQueryService workspaceQueryService;
	@MockBean
	protected WorkspaceCommandService workspaceCommandService;
	@MockBean
	protected AuthenticationService authenticationService;
	@MockBean
	protected InvitationCommandService invitationCommandService;
	@MockBean
	protected InvitationQueryService invitationQueryService;
	@MockBean
	protected PositionCommandService positionCommandService;
	@MockBean
	protected PositionQueryService positionQueryService;
	@MockBean
	protected IssueCommandService issueCommandService;

	/**
	 * Repository
	 */
	@MockBean
	protected MemberRepository memberRepository;
	@MockBean
	protected WorkspaceRepository workspaceRepository;
	@MockBean
	protected WorkspaceMemberRepository workspaceMemberRepository;
	@MockBean
	protected InvitationRepository invitationRepository;
	@MockBean
	protected PositionRepository positionRepository;
	@MockBean
	protected IssueRepository issueRepository;

}
