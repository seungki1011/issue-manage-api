package com.uranus.taskmanager.basetest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.uranus.taskmanager.api.authentication.service.AuthenticationService;
import com.uranus.taskmanager.api.invitation.repository.InvitationRepository;
import com.uranus.taskmanager.api.invitation.service.InvitationService;
import com.uranus.taskmanager.api.member.repository.MemberRepository;
import com.uranus.taskmanager.api.member.service.MemberService;
import com.uranus.taskmanager.api.workspace.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspace.service.CheckCodeDuplicationService;
import com.uranus.taskmanager.api.workspace.service.WorkspaceService;
import com.uranus.taskmanager.api.workspacemember.repository.WorkspaceMemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseApiIntegrationTest {
	@LocalServerPort
	protected int port;

	@Autowired
	protected WorkspaceService workspaceService;
	@Autowired
	protected CheckCodeDuplicationService workspaceCreateService;
	@Autowired
	protected AuthenticationService authenticationService;
	@Autowired
	protected MemberService memberService;
	@Autowired
	protected InvitationService invitationService;

	@Autowired
	protected WorkspaceRepository workspaceRepository;
	@Autowired
	protected MemberRepository memberRepository;
	@Autowired
	protected WorkspaceMemberRepository workspaceMemberRepository;
	@Autowired
	protected InvitationRepository invitationRepository;

}