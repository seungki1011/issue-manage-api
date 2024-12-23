package com.tissue.api.workspace.service.command.create;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.member.domain.Member;
import com.tissue.api.member.domain.repository.MemberRepository;
import com.tissue.api.security.PasswordEncoder;
import com.tissue.api.util.WorkspaceCodeGenerator;
import com.tissue.api.workspace.domain.Workspace;
import com.tissue.api.workspace.domain.repository.WorkspaceRepository;
import com.tissue.api.workspace.presentation.dto.request.CreateWorkspaceRequest;
import com.tissue.api.workspace.presentation.dto.response.CreateWorkspaceResponse;
import com.tissue.api.workspace.validator.WorkspaceValidator;
import com.tissue.api.workspacemember.domain.WorkspaceMember;
import com.tissue.api.workspacemember.domain.repository.WorkspaceMemberRepository;
import com.tissue.api.member.exception.MemberNotFoundException;
import com.tissue.api.workspace.exception.WorkspaceCodeCollisionHandleException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WorkspaceCode의 중복 검사를 진행해서, 중복인 경우 재생성한다
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckCodeDuplicationService implements WorkspaceCreateService {

	private static final int MAX_RETRIES = 5;

	private final WorkspaceRepository workspaceRepository;
	private final MemberRepository memberRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	private final WorkspaceCodeGenerator workspaceCodeGenerator;
	private final PasswordEncoder passwordEncoder;
	private final WorkspaceValidator workspaceValidator;

	/**
	 * 로그인 정보를 사용해서 멤버의 존재 유무를 검증한다
	 * 워크스페이스 생성 요청에 유일한 코드를 생성하고 설정한다
	 * 만약 코드가 중복되면 최대 5회 재성성 시도를 한다
	 * 워크스페이스 생성 요청을 사용해서 워크스페이스를 생성하고 저장한다
	 * 로그인 정보를 통해 찾은 멤버를 해당 워크스페이스에 참여시킨다
	 *
	 * @param request  - 컨트롤러의 워크스페이스 생성 요청 객체
	 * @param memberId - 컨트롤러에서의 로그인 정보에서 꺼내온 멤버 id(PK)
	 * @return WorkspaceCreateResponse - 워크스페이스 생성 응답 DTO
	 */
	@Override
	@Transactional
	public CreateWorkspaceResponse createWorkspace(CreateWorkspaceRequest request,
		Long memberId) {

		Member member = memberRepository.findById(memberId)
			.orElseThrow(MemberNotFoundException::new);

		setUniqueWorkspaceCode(request);
		setEncodedPasswordIfPresent(request);

		Workspace workspace = workspaceRepository.save(CreateWorkspaceRequest.to(request));
		addOwnerMemberToWorkspace(member, workspace);

		return CreateWorkspaceResponse.from(workspace);
	}

	private Optional<String> generateUniqueWorkspaceCode() {
		for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
			String code = workspaceCodeGenerator.generateWorkspaceCode();

			if (workspaceValidator.validateWorkspaceCodeIsUnique(code)) {
				return Optional.of(code);
			}
			log.info("Workspace code collision occured. Retry attempt: #{}", attempt);
		}
		return Optional.empty();
	}

	private void setUniqueWorkspaceCode(CreateWorkspaceRequest createWorkspaceRequest) {
		String code = generateUniqueWorkspaceCode()
			.orElseThrow(WorkspaceCodeCollisionHandleException::new);
		createWorkspaceRequest.setCode(code);
	}

	private void setEncodedPasswordIfPresent(CreateWorkspaceRequest request) {
		String encodedPassword = Optional.ofNullable(request.getPassword())
			.map(passwordEncoder::encode)
			.orElse(null);
		request.setPassword(encodedPassword);
	}

	private void addOwnerMemberToWorkspace(Member member, Workspace workspace) {
		WorkspaceMember workspaceMember = WorkspaceMember.addOwnerWorkspaceMember(member, workspace);

		workspaceMemberRepository.save(workspaceMember);
	}
}
