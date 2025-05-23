package com.tissue.api.workspacemember.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.workspacemember.domain.model.WorkspaceMember;
import com.tissue.api.workspacemember.infrastructure.repository.WorkspaceMemberRepository;
import com.tissue.api.workspacemember.exception.WorkspaceMemberNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberReader {

	private final WorkspaceMemberRepository workspaceMemberRepository;

	@Transactional(readOnly = true)
	public WorkspaceMember findWorkspaceMember(Long id) {
		return workspaceMemberRepository.findById(id)
			.orElseThrow(() -> new WorkspaceMemberNotFoundException(id));
	}

	@Transactional(readOnly = true)
	public WorkspaceMember findWorkspaceMember(Long id, String code) {
		return workspaceMemberRepository.findByMemberIdAndWorkspaceCode(id, code)
			.orElseThrow(() -> new WorkspaceMemberNotFoundException(id, code));
	}
}
