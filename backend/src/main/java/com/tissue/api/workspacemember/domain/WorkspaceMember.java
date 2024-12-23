package com.tissue.api.workspacemember.domain;

import com.tissue.api.common.entity.BaseEntity;
import com.tissue.api.member.domain.Member;
import com.tissue.api.position.domain.Position;
import com.tissue.api.workspacemember.exception.InvalidRoleUpdateException;
import com.tissue.api.position.exception.PositionNotFoundException;
import com.tissue.api.workspace.domain.Workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(
			name = "UK_WORKSPACE_NICKNAME",
			columnNames = {"workspace_code", "nickname"})
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceMember extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MEMBER_ID", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORKSPACE_ID", nullable = false)
	private Workspace workspace;

	@Column(name = "WORKSPACE_CODE", nullable = false)
	private String workspaceCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "POSITION_ID")
	private Position position;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WorkspaceRole role;

	@Column(nullable = false)
	private String nickname;

	@Builder
	public WorkspaceMember(Member member, Workspace workspace, WorkspaceRole role, String nickname) {
		this.member = member;
		this.workspace = workspace;
		this.role = role;
		this.nickname = nickname;
		this.workspaceCode = workspace.getCode();
	}

	public static WorkspaceMember addWorkspaceMember(Member member, Workspace workspace, WorkspaceRole role,
		String nickname) {
		WorkspaceMember workspaceMember = WorkspaceMember.builder()
			.member(member)
			.workspace(workspace)
			.role(role)
			.nickname(nickname)
			.build();

		member.getWorkspaceMembers().add(workspaceMember);
		workspace.getWorkspaceMembers().add(workspaceMember);

		return workspaceMember;
	}

	public static WorkspaceMember addOwnerWorkspaceMember(Member member, Workspace workspace) {
		member.increaseMyWorkspaceCount();
		workspace.increaseMemberCount();
		return addWorkspaceMember(member, workspace, WorkspaceRole.OWNER, member.getEmail());
	}

	public static WorkspaceMember addCollaboratorWorkspaceMember(Member member, Workspace workspace) {
		workspace.increaseMemberCount();
		return addWorkspaceMember(member, workspace, WorkspaceRole.COLLABORATOR, member.getEmail());
	}

	public void remove() {
		this.workspace.decreaseMemberCount();
		this.member.getWorkspaceMembers().remove(this);
		this.workspace.getWorkspaceMembers().remove(this);
	}

	public void changePosition(Position position) {
		if (position != null) {
			validatePositionBelongsToWorkspace(position);
		}
		this.position = position;
	}

	public void removePosition() {
		if (this.position != null) {
			this.position.getWorkspaceMembers().remove(this);
			this.position = null;
		}
	}

	public void updateRole(WorkspaceRole role) {
		validateCannotUpdateToOwnerRole(role);
		this.role = role;
	}

	public void updateRoleFromOwnerToAdmin() {
		validateCurrentRoleIsOwner();
		updateRole(WorkspaceRole.ADMIN);
		this.member.decreaseMyWorkspaceCount();
	}

	public void updateRoleToOwner() {
		validateCurrentRoleIsNotOwner();
		this.role = WorkspaceRole.OWNER;
		this.member.increaseMyWorkspaceCount();
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	private void validatePositionBelongsToWorkspace(Position position) {
		if (!position.getWorkspaceCode().equals(this.workspaceCode)) {
			throw new PositionNotFoundException();
		}
	}

	private void validateCannotUpdateToOwnerRole(WorkspaceRole newRole) {
		if (newRole == WorkspaceRole.OWNER) {
			throw new InvalidRoleUpdateException(
				"You cannot directly change to OWNER role. Use ownership transfer instead.");
		}
	}

	private void validateCurrentRoleIsOwner() {
		if (this.role != WorkspaceRole.OWNER) {
			throw new InvalidRoleUpdateException("Current role must be OWNER.");
		}
	}

	private void validateCurrentRoleIsNotOwner() {
		if (this.role == WorkspaceRole.OWNER) {
			throw new InvalidRoleUpdateException("Current role cannot be OWNER.");
		}
	}
}
