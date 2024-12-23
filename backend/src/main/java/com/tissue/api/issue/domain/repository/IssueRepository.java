package com.tissue.api.issue.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tissue.api.issue.domain.Issue;

public interface IssueRepository extends JpaRepository<Issue, Long> {

	@Query("SELECT i FROM Issue i JOIN FETCH i.workspace WHERE i.id = :id")
	Optional<Issue> findByIdWithWorkspace(@Param("id") Long id);

	Optional<Issue> findByIdAndWorkspaceCode(Long id, String workspaceCode);

	/**
	 * 워크스페이스 코드와 이슈 ID로 이슈와 그 하위 이슈들을 함께 조회합니다.
	 * 부모 이슈 설정 시 N+1 문제를 방지하기 위해 fetch join을 사용합니다.
	 */
	@Query("SELECT i FROM Issue i "
		+ "LEFT JOIN FETCH i.childIssues "
		+ "WHERE i.workspaceCode = :workspaceCode "
		+ "AND i.id = :issueId")
	Optional<Issue> findByWorkspaceCodeAndIdWithChildren(
		@Param("workspaceCode") String workspaceCode,
		@Param("issueId") Long issueId
	);

	/**
	 * 워크스페이스의 이슈들을 페이징하여 조회합니다.
	 * 추후 이슈 목록 조회 기능 구현 시 사용할 수 있습니다.
	 */
	Page<Issue> findByWorkspaceCode(String workspaceCode, Pageable pageable);
}
