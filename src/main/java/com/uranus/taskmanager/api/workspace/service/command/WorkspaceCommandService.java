package com.uranus.taskmanager.api.workspace.service.command;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uranus.taskmanager.api.member.domain.Member;
import com.uranus.taskmanager.api.member.domain.repository.MemberRepository;
import com.uranus.taskmanager.api.security.PasswordEncoder;
import com.uranus.taskmanager.api.workspace.domain.Workspace;
import com.uranus.taskmanager.api.workspace.domain.repository.WorkspaceRepository;
import com.uranus.taskmanager.api.workspace.exception.WorkspaceNotFoundException;
import com.uranus.taskmanager.api.workspace.presentation.dto.request.DeleteWorkspaceRequest;
import com.uranus.taskmanager.api.workspace.presentation.dto.request.UpdateWorkspacePasswordRequest;
import com.uranus.taskmanager.api.workspace.presentation.dto.request.UpdateWorkspaceRequest;
import com.uranus.taskmanager.api.workspace.presentation.dto.response.UpdateWorkspaceResponse;
import com.uranus.taskmanager.api.workspace.validator.WorkspaceValidator;
import com.uranus.taskmanager.api.workspacemember.exception.MemberNotInWorkspaceException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WorkspaceCommandService {

	private final WorkspaceRepository workspaceRepository;
	private final MemberRepository memberRepository;

	private final PasswordEncoder passwordEncoder;
	private final WorkspaceValidator workspaceValidator;

	@Transactional
	public UpdateWorkspaceResponse updateWorkspaceContent(UpdateWorkspaceRequest request, String code) {
		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		if (request.hasName()) {
			workspace.updateName(request.getName());
		}
		if (request.hasDescription()) {
			workspace.updateDescription(request.getDescription());
		}

		return UpdateWorkspaceResponse.from(workspace);
	}

	@Transactional
	public void updateWorkspacePassword(UpdateWorkspacePasswordRequest request, String code) {
		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		workspaceValidator.validatePasswordIfExists(workspace.getPassword(), request.getOriginalPassword());

		String encodedUpdatePassword = encodePasswordIfPresent(request.getUpdatePassword());
		workspace.updatePassword(encodedUpdatePassword);
	}

	@Transactional
	public void deleteWorkspace(DeleteWorkspaceRequest request, String code, Long id) {
		Workspace workspace = workspaceRepository.findByCode(code)
			.orElseThrow(WorkspaceNotFoundException::new);

		Member member = memberRepository.findById(id)
			.orElseThrow(MemberNotInWorkspaceException::new);

		member.decreaseMyWorkspaceCount();

		workspaceValidator.validatePasswordIfExists(workspace.getPassword(), request.getPassword());

		workspaceRepository.delete(workspace);
	}

	private String encodePasswordIfPresent(String password) {
		return Optional.ofNullable(password)
			.map(passwordEncoder::encode)
			.orElse(null);
	}
}