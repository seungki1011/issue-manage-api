package com.tissue.api.review.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tissue.api.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Optional<Review> findByIdAndIssueKeyAndWorkspaceCode(Long reviewId, String issueKey, String workspaceCode);
}
