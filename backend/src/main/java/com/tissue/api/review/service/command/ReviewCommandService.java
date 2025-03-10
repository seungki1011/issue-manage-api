package com.tissue.api.review.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.common.exception.type.ForbiddenOperationException;
import com.tissue.api.issue.domain.Issue;
import com.tissue.api.issue.domain.enums.IssueStatus;
import com.tissue.api.issue.service.query.IssueQueryService;
import com.tissue.api.review.domain.IssueReviewer;
import com.tissue.api.review.domain.Review;
import com.tissue.api.review.domain.enums.ReviewStatus;
import com.tissue.api.review.domain.repository.IssueReviewerRepository;
import com.tissue.api.review.domain.repository.ReviewRepository;
import com.tissue.api.review.presentation.dto.request.CreateReviewRequest;
import com.tissue.api.review.presentation.dto.request.UpdateReviewRequest;
import com.tissue.api.review.presentation.dto.request.UpdateReviewStatusRequest;
import com.tissue.api.review.presentation.dto.response.CreateReviewResponse;
import com.tissue.api.review.presentation.dto.response.UpdateReviewResponse;
import com.tissue.api.review.presentation.dto.response.UpdateReviewStatusResponse;
import com.tissue.api.review.service.query.ReviewQueryService;
import com.tissue.api.workspacemember.domain.WorkspaceMember;
import com.tissue.api.workspacemember.domain.WorkspaceRole;
import com.tissue.api.workspacemember.service.query.WorkspaceMemberQueryService;

import lombok.RequiredArgsConstructor;

/**
 * Todo
 *  - 알림 서비스 구현 필요
 *    - 리뷰 상태 변경 -> 모든 리뷰가 작성되었고 전부 APPROVED -> 이슈 assignees에게 알림
 */
@Service
@RequiredArgsConstructor
public class ReviewCommandService {

	private final ReviewQueryService reviewQueryService;
	private final IssueQueryService issueQueryService;
	private final WorkspaceMemberQueryService workspaceMemberQueryService;
	private final ReviewRepository reviewRepository;
	private final IssueReviewerRepository issueReviewerRepository;

	@Transactional
	public CreateReviewResponse createReview(
		String workspaceCode,
		String issueKey,
		Long reviewerWorkspaceMemberId,
		CreateReviewRequest request
	) {
		Issue issue = issueQueryService.findIssue(issueKey, workspaceCode);

		IssueReviewer issueReviewer = findIssueReviewer(issueKey, reviewerWorkspaceMemberId);

		issue.validateReviewIsCreateable();

		Review review = issueReviewer.addReview(
			request.status(),
			request.title(),
			request.content()
		);

		Review savedReview = reviewRepository.save(review);

		updateIssueStatusBasedOnReviewStatus(issue, request.status());

		return CreateReviewResponse.from(savedReview);
	}

	@Transactional
	public UpdateReviewResponse updateReview(
		Long reviewId,
		Long reviewerWorkspaceMemberId,
		UpdateReviewRequest request
	) {
		Review review = reviewQueryService.findReview(reviewId);
		review.validateIsAuthor(reviewerWorkspaceMemberId);

		review.updateTitle(request.title());
		review.updateContent(request.content());

		return UpdateReviewResponse.from(review);
	}

	@Transactional
	public UpdateReviewStatusResponse updateReviewStatus(
		String workspaceCode,
		String issueKey,
		Long reviewId,
		Long requesterWorkspaceMemberId,
		UpdateReviewStatusRequest request
	) {
		Issue issue = issueQueryService.findIssue(issueKey, workspaceCode);

		WorkspaceMember requester = workspaceMemberQueryService.findWorkspaceMember(requesterWorkspaceMemberId);
		Review review = reviewQueryService.findReview(reviewId);

		if (requester.roleIsLowerThan(WorkspaceRole.MANAGER)) {
			review.validateIsAuthor(requesterWorkspaceMemberId);
		}

		review.updateStatus(request.status());
		updateIssueStatusBasedOnReviewStatus(issue, request.status());

		return UpdateReviewStatusResponse.from(review);
	}

	private IssueReviewer findIssueReviewer(String issueKey, Long reviewerWorkspaceMemberId) {
		return issueReviewerRepository.findByIssueKeyAndReviewerId(issueKey, reviewerWorkspaceMemberId)
			.orElseThrow(() -> new ForbiddenOperationException(String.format(
				"Must be a reviewer to create a review. issueKey: %s, workspaceMemberId: %d",
				issueKey, reviewerWorkspaceMemberId)));
	}

	private void updateIssueStatusBasedOnReviewStatus(Issue issue, ReviewStatus reviewStatus) {
		// CHANGES_REQUESTED의 경우 자동 상태 변경
		if (reviewStatus == ReviewStatus.CHANGES_REQUESTED) {
			issue.updateStatus(IssueStatus.CHANGES_REQUESTED);
			return;
		}

		/*
		 * Todo
		 *  - 모든 리뷰어가 승인한 경우, 작업자에게 알림
		 *  - 알림 서비스 구현 필요
		 *  - 자동으로 이슈 상태 DONE으로 변경 X
		 *  - 알림은 이벤트 리스너로 구현하는 것이 좋을 듯
		 */
		boolean allApproved = issue.getReviewers().stream()
			.allMatch(reviewer ->
				reviewer.getCurrentReviewStatus(issue.getCurrentReviewRound())
					== ReviewStatus.APPROVED
			);

		if (allApproved) {
			// Todo: 알림 서비스 구현 후 추가
		}
	}
}
