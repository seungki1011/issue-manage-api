package com.uranus.taskmanager.api.workspacemember.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uranus.taskmanager.api.workspacemember.WorkspaceRole;
import com.uranus.taskmanager.api.workspacemember.domain.WorkspaceMember;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

	Page<WorkspaceMember> findByMemberId(Long id, Pageable pageable);

	Optional<WorkspaceMember> findByMemberLoginIdAndWorkspaceCode(String loginId, String workspaceCode);

	Optional<WorkspaceMember> findByMemberIdAndWorkspaceCode(Long id, String workspaceCode);

	Optional<WorkspaceMember> findByMemberIdAndWorkspaceId(Long memberId, Long workspaceId);

	boolean existsByMemberIdAndRole(Long memberId, WorkspaceRole role);

	boolean existsByMemberIdAndWorkspaceCode(Long memberId, String workspaceCode);

	@Query("SELECT wm FROM WorkspaceMember wm "
		+ "WHERE (wm.member.loginId = :identifier OR wm.member.email = :identifier) "
		+ "AND wm.workspace.code = :workspaceCode")
	Optional<WorkspaceMember> findByMemberIdentifierAndWorkspaceCode(
		@Param("identifier") String identifier,
		@Param("workspaceCode") String workspaceCode
	);
}