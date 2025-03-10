package com.tissue.api.workspacemember.service.command;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.common.exception.type.DuplicateResourceException;
import com.tissue.api.position.domain.Position;
import com.tissue.api.position.service.query.PositionQueryService;
import com.tissue.api.team.domain.Team;
import com.tissue.api.team.service.query.TeamQueryService;
import com.tissue.api.workspacemember.domain.WorkspaceMember;
import com.tissue.api.workspacemember.domain.repository.WorkspaceMemberRepository;
import com.tissue.api.workspacemember.presentation.dto.request.UpdateNicknameRequest;
import com.tissue.api.workspacemember.presentation.dto.request.UpdateRoleRequest;
import com.tissue.api.workspacemember.presentation.dto.response.AssignPositionResponse;
import com.tissue.api.workspacemember.presentation.dto.response.AssignTeamResponse;
import com.tissue.api.workspacemember.presentation.dto.response.RemoveWorkspaceMemberResponse;
import com.tissue.api.workspacemember.presentation.dto.response.TransferOwnershipResponse;
import com.tissue.api.workspacemember.presentation.dto.response.UpdateNicknameResponse;
import com.tissue.api.workspacemember.presentation.dto.response.UpdateRoleResponse;
import com.tissue.api.workspacemember.service.query.WorkspaceMemberQueryService;
import com.tissue.api.workspacemember.validator.WorkspaceMemberValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceMemberCommandService {

	private final WorkspaceMemberQueryService workspaceMemberQueryService;
	private final PositionQueryService positionQueryService;
	private final TeamQueryService teamQueryService;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final WorkspaceMemberValidator workspaceMemberValidator;

	@Transactional
	public UpdateNicknameResponse updateNickname(
		Long workspaceMemberId,
		UpdateNicknameRequest request
	) {
		try {
			WorkspaceMember workspaceMember = workspaceMemberQueryService.findWorkspaceMember(workspaceMemberId);

			workspaceMember.updateNickname(request.nickname());
			workspaceMemberRepository.saveAndFlush(workspaceMember);

			return UpdateNicknameResponse.from(workspaceMember);

		} catch (DataIntegrityViolationException | ConstraintViolationException e) {
			log.error("Duplicate nickname: ", e);

			throw new DuplicateResourceException(
				String.format("Nickname already exists for this workspace. nickname: %s", request.nickname()), e);
		}
	}

	@Transactional
	public UpdateRoleResponse updateWorkspaceMemberRole(
		Long targetWorkspaceMemberId,
		Long requesterWorkspaceMemberId,
		UpdateRoleRequest request
	) {
		WorkspaceMember requester = workspaceMemberQueryService.findWorkspaceMember(requesterWorkspaceMemberId);
		WorkspaceMember target = workspaceMemberQueryService.findWorkspaceMember(targetWorkspaceMemberId);

		workspaceMemberValidator.validateRoleUpdate(requester, target);

		target.updateRole(request.updateWorkspaceRole());

		return UpdateRoleResponse.from(target);
	}

	@Transactional
	public AssignPositionResponse assignPosition(
		String workspaceCode,
		Long positionId,
		Long workspaceMemberId
	) {
		Position position = positionQueryService.findPosition(positionId, workspaceCode);
		WorkspaceMember workspaceMember = workspaceMemberQueryService.findWorkspaceMember(workspaceMemberId);

		workspaceMember.addPosition(position);

		return AssignPositionResponse.from(workspaceMember);
	}

	@Transactional
	public void removePosition(
		String workspaceCode,
		Long positionId,
		Long workspaceMemberId
	) {
		Position position = positionQueryService.findPosition(positionId, workspaceCode);
		WorkspaceMember workspaceMember = workspaceMemberQueryService.findWorkspaceMember(workspaceMemberId);

		workspaceMember.removePosition(position);
	}

	@Transactional
	public AssignTeamResponse assignTeam(
		String workspaceCode,
		Long teamId,
		Long workspaceMemberId
	) {
		Team team = teamQueryService.findTeam(teamId, workspaceCode);
		WorkspaceMember workspaceMember = workspaceMemberQueryService.findWorkspaceMember(workspaceMemberId);

		workspaceMember.addTeam(team);

		return AssignTeamResponse.from(workspaceMember);
	}

	@Transactional
	public void removeTeam(
		String workspaceCode,
		Long teamId,
		Long workspaceMemberId
	) {
		Team team = teamQueryService.findTeam(teamId, workspaceCode);
		WorkspaceMember workspaceMember = workspaceMemberQueryService.findWorkspaceMember(workspaceMemberId);

		workspaceMember.removeTeam(team);
	}

	@Transactional
	public TransferOwnershipResponse transferWorkspaceOwnership(
		Long targetWorkspaceMemberId,
		Long requesterWorkspaceMemberId
	) {
		WorkspaceMember requester = workspaceMemberQueryService.findWorkspaceMember(requesterWorkspaceMemberId);
		WorkspaceMember target = workspaceMemberQueryService.findWorkspaceMember(targetWorkspaceMemberId);

		requester.updateRoleFromOwnerToAdmin();
		target.updateRoleToOwner();

		return TransferOwnershipResponse.from(requester, target);
	}

	@Transactional
	public RemoveWorkspaceMemberResponse removeWorkspaceMember(
		Long targetWorkspaceMemberId,
		Long requesterWorkspaceMemberId
	) {
		WorkspaceMember requester = workspaceMemberQueryService.findWorkspaceMember(requesterWorkspaceMemberId);
		WorkspaceMember target = workspaceMemberQueryService.findWorkspaceMember(targetWorkspaceMemberId);

		workspaceMemberValidator.validateRemoveMember(requester, target);

		target.remove();
		workspaceMemberRepository.delete(target);

		return RemoveWorkspaceMemberResponse.from(target);
	}
}
