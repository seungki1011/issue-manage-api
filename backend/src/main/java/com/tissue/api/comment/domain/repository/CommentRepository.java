package com.tissue.api.comment.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tissue.api.comment.domain.Comment;
import com.tissue.api.comment.domain.IssueComment;
import com.tissue.api.comment.domain.ReviewComment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	Optional<IssueComment> findByIdAndIssue_IssueKeyAndIssue_WorkspaceCode(
		Long id,
		String issueKey,
		String workspaceCode
	);

	Optional<ReviewComment> findByIdAndReview_IdAndReview_IssueKey(
		Long commentId,
		Long reviewId,
		String issueKey
	);

	@Query("SELECT rc FROM ReviewComment rc "
		+ "JOIN rc.review r "
		+ "WHERE rc.id = :commentId "
		+ "AND r.id = :reviewId "
		+ "AND r.issueKey = :issueKey ")
	Optional<ReviewComment> findByIdAndReviewIdAndIssueKey(
		@Param("commentId") Long commentId,
		@Param("reviewId") Long reviewId,
		@Param("issueKey") String issueKey
	);
}
