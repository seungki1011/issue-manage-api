package com.tissue.api.issue.domain;

import static com.tissue.api.issue.domain.enums.IssueStatus.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tissue.api.assignee.domain.IssueAssignee;
import com.tissue.api.common.entity.WorkspaceContextBaseEntity;
import com.tissue.api.common.exception.type.ForbiddenOperationException;
import com.tissue.api.common.exception.type.InvalidOperationException;
import com.tissue.api.issue.domain.enums.IssuePriority;
import com.tissue.api.issue.domain.enums.IssueRelationType;
import com.tissue.api.issue.domain.enums.IssueStatus;
import com.tissue.api.issue.domain.enums.IssueType;
import com.tissue.api.review.domain.IssueReviewer;
import com.tissue.api.review.domain.enums.ReviewStatus;
import com.tissue.api.sprint.domain.SprintIssue;
import com.tissue.api.workspace.domain.Workspace;
import com.tissue.api.workspacemember.domain.WorkspaceMember;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Todo 1
 *  - workspaceCode + issueKey 복합 인덱스를 위한 필드 고려?
 *  - 모든 이슈의 조회를 workspaceCode + issueKey를 사용 중
 * <br>
 * Todo 2
 *  - dueDate가 null인 경우의 처리가 필요
 * 	  - 예시: null이면 1주일 후의 날짜를 dueDate로 설정(생성자에서)
 *  - parentIssue 추가하는 경우 해당 parentIssue에는 현재의 이슈가 childIssue로 추가
 *    - 만약 Issue를 처음으로 생성하는 경우라면, 해당 Issue의 id는 어떻게 처리되더라?
 *    - IDENTITY의 경우 DB에서 조회가 필요했던 것 같은데... 한번 찾아보자
 *  - 리뷰어 신청을 해서 리뷰 상태 중 하나라도 PENDING이라면 이슈 status는 IN_REVIEW
 * <br>
 * Todo 3
 *  - 동시성 문제 해결을 위해서 이슈 생성에 spring-retry 적용
 *  - Workspace에서 issueKeyPrefix와 nextIssueNumber를 관리하기 때문에,
 *  Workspace에 Optimistic locking을 적용한다
 * <br>
 * Todo 4
 *  - 상태 업데이트는 도메인 이벤트(Domain Event) 발행으로 구현하는 것을 고려
 *  - 상태 변경과 관련된 부가 작업들(알림 발송, 감사 로그 기록 등)을 이벤트 핸들러에서 처리할 수 있어 확장성이 좋아짐
 * <br>
 * Todo 5
 *  - 상태 패턴(State, State Machine Pattern)의 사용 고려
 *  - 상태 변경 규칙을 한 곳에서 명확하게 관리할 수 있고, 새로운 상태나 규칙을 추가하기도 쉬워짐
 * <br>
 * Todo 6
 *  - 이슈 상태 변화에 대한 검증을 그냥 validator 클래스에서 정의해서 서비스에서 진행 고려
 * <br>
 * Todo 7
 *  - difficulty를 Issue로 이동
 */
@Entity
@Getter
@EqualsAndHashCode(of = {"issueKey", "workspaceCode"}, callSuper = false)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Issue extends WorkspaceContextBaseEntity {

	private static final int MAX_REVIEWERS = 10;
	private static final int MAX_ASSIGNEES = 50;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String issueKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", insertable = false, updatable = false)
	private IssueType type;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WORKSPACE_ID", nullable = false)
	private Workspace workspace;

	@Column(name = "WORKSPACE_CODE", nullable = false)
	private String workspaceCode;

	@Column(nullable = false)
	private String title;

	@Lob
	@Column(nullable = false)
	private String content;

	@Lob
	private String summary;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IssueStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IssuePriority priority;

	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;
	private LocalDateTime reviewRequestedAt;

	private LocalDate dueDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ISSUE_ID")
	private Issue parentIssue;

	@OneToMany(mappedBy = "parentIssue")
	private List<Issue> childIssues = new ArrayList<>();

	@OneToMany(mappedBy = "sourceIssue", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IssueRelation> outgoingRelations = new ArrayList<>();

	@OneToMany(mappedBy = "targetIssue", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IssueRelation> incomingRelations = new ArrayList<>();

	@OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IssueReviewer> reviewers = new ArrayList<>();

	@Column(nullable = false)
	private int currentReviewRound = 0;

	@OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<IssueAssignee> assignees = new ArrayList<>();

	@OneToMany(mappedBy = "issue")
	private List<SprintIssue> sprintIssues = new ArrayList<>();

	protected Issue(
		Workspace workspace,
		IssueType type,
		String title,
		String content,
		String summary,
		IssuePriority priority,
		LocalDate dueDate
	) {
		this.issueKey = workspace.getIssueKey();
		workspace.increaseNextIssueNumber();

		addToWorkspace(workspace);

		this.type = type;
		this.title = title;
		this.content = content;
		this.summary = summary;
		this.status = IssueStatus.TODO;
		this.priority = priority != null ? priority : IssuePriority.MEDIUM;
		this.dueDate = dueDate;
	}

	public void requestReview() {
		validateReviewersExist();

		if (isNotFirstReviewRound()) {
			validateCanStartNewReviewRound();
		}
		this.currentReviewRound++;
		this.updateStatus(IssueStatus.IN_REVIEW);
	}

	public IssueReviewer addReviewer(WorkspaceMember workspaceMember) {
		validateReviewerLimit();
		validateIsReviewer(workspaceMember);

		IssueReviewer reviewer = new IssueReviewer(workspaceMember, this);
		reviewers.add(reviewer);

		return reviewer;
	}

	public void removeReviewer(WorkspaceMember workspaceMember) {
		IssueReviewer issueReviewer = findIssueReviewer(workspaceMember);
		validateHasReviewForCurrentRound(issueReviewer);

		reviewers.remove(issueReviewer);
	}

	public void validateCanRemoveReviewer(Long requesterWorkspaceMemberId, Long reviewerWorkspaceMemberId) {
		if (requesterWorkspaceMemberId.equals(reviewerWorkspaceMemberId)) {
			return;
		}

		boolean isNotAssignee = !isAssignee(requesterWorkspaceMemberId);

		if (isNotAssignee) {
			throw new ForbiddenOperationException(String.format(
				"Must be the reviewer or be a assignee to remove the reviewer."
					+ " requesterWorkspaceMemberId: %d, reviewerWorkspaceMemberId: %d",
				requesterWorkspaceMemberId, reviewerWorkspaceMemberId));
		}
	}

	public void validateReviewIsCreateable() {
		boolean isStatusNotInReview = status != IssueStatus.IN_REVIEW;

		if (isStatusNotInReview) {
			throw new InvalidOperationException(String.format(
				"Issue status must be IN_REVIEW to create a review. Current status: %s",
				status));
		}
	}

	private IssueReviewer findIssueReviewer(WorkspaceMember workspaceMember) {
		return reviewers.stream()
			.filter(r -> r.getReviewer().getId().equals(workspaceMember.getId()))
			.findFirst()
			.orElseThrow(() -> new ForbiddenOperationException(String.format(
				"Not a reviewer assigned to this issue. workspaceMemberId: %d, nickname: %s",
				workspaceMember.getId(), workspaceMember.getNickname()))
			);
	}

	private void validateReviewersExist() {
		if (reviewers.isEmpty()) {
			throw new InvalidOperationException("Cannot request review if there are no assigned reviewers.");
		}
	}

	private void validateHasReviewForCurrentRound(IssueReviewer issueReviewer) {
		if (issueReviewer.hasReviewForRound(currentReviewRound)) {
			throw new InvalidOperationException(String.format(
				"Cannot remove reviewer that already has a review for the current round. Current round: %d",
				currentReviewRound));
		}
	}

	private void validateCanStartNewReviewRound() {
		// 현재 상태 검증
		boolean isStatusNotChangesRequested = status != IssueStatus.CHANGES_REQUESTED;

		if (isStatusNotChangesRequested) {
			throw new InvalidOperationException(String.format(
				"Issue status must be CHANGES_REQUESTED to start a new review round. Current issue status: %s",
				status));
		}

		// 현재 라운드의 모든 리뷰어가 리뷰를 작성했는지 검증
		boolean hasIncompleteReviews = reviewers.stream()
			.noneMatch(reviewer -> reviewer.hasReviewForRound(currentReviewRound));

		if (hasIncompleteReviews) {
			throw new InvalidOperationException(String.format(
				"Reviewers that have not completed their review for this round exist. Current round: %d",
				currentReviewRound));
		}
	}

	private void validateReviewerLimit() {
		if (reviewers.size() >= MAX_REVIEWERS) {
			throw new InvalidOperationException(
				String.format("The max number of reviewers for a single issue is %d.", MAX_REVIEWERS));
		}
	}

	private void validateIsReviewer(WorkspaceMember workspaceMember) {
		boolean isAlreadyReviewer = reviewers.stream()
			.anyMatch(r -> r.getReviewer().getId().equals(workspaceMember.getId()));

		if (isAlreadyReviewer) {
			throw new InvalidOperationException(String.format(
				"Workspace member is already a reviewer. workspaceMemberId: %d",
				workspaceMember.getId()));
		}
	}

	public void validateIssueTypeMatch(IssueType type) {
		boolean typeNotMatch = this.type != type;

		if (typeNotMatch) {
			throw new InvalidOperationException(String.format(
				"Issue type does not match the needed type. Issue type: %s, Required type: %s",
				this.type, type));
		}
	}

	public boolean isBlockedBy(Issue issue) {
		return incomingRelations.stream()
			.anyMatch(relation ->
				relation.getSourceIssue().equals(issue)
					&& relation.getRelationType() == IssueRelationType.BLOCKS
			);
	}

	public IssueAssignee addAssignee(WorkspaceMember workspaceMember) {
		validateAssigneeLimit();
		validateBelongsToWorkspace(workspaceMember);
		validateNotAlreadyAssigned(workspaceMember);

		IssueAssignee assignee = new IssueAssignee(this, workspaceMember);

		assignees.add(assignee);
		return assignee;
	}

	public void removeAssignee(WorkspaceMember assignee) {
		IssueAssignee issueAssignee = findIssueAssignee(assignee);
		assignees.remove(issueAssignee);
	}

	public void validateIsAssignee(Long workspaceMemberId) {
		boolean isNotAssignee = !isAssignee(workspaceMemberId);

		if (isNotAssignee) {
			throw new ForbiddenOperationException(String.format(
				"Must be an assignee of this issue. issue key: %s, workspace member id: %d",
				issueKey, workspaceMemberId));
		}
	}

	public void validateIsAssigneeOrAuthor(Long workspaceMemberId) {
		if (isAssignee(workspaceMemberId)) {
			return;
		}
		if (isAuthor(workspaceMemberId)) {
			return;
		}
		throw new ForbiddenOperationException(
			String.format("Must be the author or an assignee of this issue. issueKey: %s", issueKey)
		);
	}

	private IssueAssignee findIssueAssignee(WorkspaceMember assignee) {
		return assignees.stream()
			.filter(ia -> ia.getAssignee().getId().equals(assignee.getId()))
			.findFirst()
			.orElseThrow(() -> new InvalidOperationException(String.format(
				"Is not a assignee assigned to this issue. workspaceMemberId: %d, nickname: %s",
				assignee.getId(), assignee.getNickname()))
			);
	}

	private boolean isAssignee(Long workspaceMemberId) {
		return assignees.stream()
			.anyMatch(issueAssignee -> issueAssignee.getAssignee().getId().equals(workspaceMemberId));
	}

	private boolean isAuthor(Long workspaceMemberId) {
		return this.getCreatedByWorkspaceMember().equals(workspaceMemberId);
	}

	private void validateAssigneeLimit() {
		if (assignees.size() >= MAX_ASSIGNEES) {
			throw new InvalidOperationException(
				String.format("The maximum number of assignees for a single issue is %d", MAX_ASSIGNEES));
		}
	}

	private void validateBelongsToWorkspace(WorkspaceMember workspaceMember) {
		boolean hasDifferentWorkspaceCode = !workspaceMember.getWorkspaceCode().equals(workspaceCode);

		if (hasDifferentWorkspaceCode) {
			throw new InvalidOperationException(String.format(
				"Assignee must belong to this workspace. expected: %s , actual: %s",
				workspaceMember.getWorkspaceCode(), workspaceCode));
		}
	}

	private void validateNotAlreadyAssigned(WorkspaceMember assignee) {
		boolean isAlreadyAssigned = isAssignee(assignee.getId());

		if (isAlreadyAssigned) {
			throw new InvalidOperationException(String.format(
				"Workspace member is already assigned to this issue. workspaceMemberId: %d, nickname: %s",
				assignee.getId(), assignee.getNickname()));
		}
	}

	public void updateTitle(String title) {
		this.title = title;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void updateSummary(String summary) {
		this.summary = summary;
	}

	public void updateDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public void updateStatus(IssueStatus newStatus) {
		validateStatusTransition(newStatus);
		this.status = newStatus;
		updateTimestamps(newStatus);
	}

	public void updatePriority(IssuePriority priority) {
		this.priority = priority;
	}

	public void updateParentIssue(Issue parentIssue) {
		validateParentIssue(parentIssue);
		removeParentRelationship();

		this.parentIssue = parentIssue;
		parentIssue.getChildIssues().add(this);
	}

	public void addToWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.workspaceCode = workspace.getCode();
		workspace.getIssues().add(this);
	}

	public void removeParentRelationship() {
		if (parentIssue != null) {
			parentIssue.getChildIssues().remove(this);
			parentIssue = null;
		}
	}

	public void validateCanRemoveParent() {
	}

	public boolean isNotFirstReviewRound() {
		return currentReviewRound != 0;
	}

	public void validateHasChildIssues() {
		boolean hasChildIssues = !childIssues.isEmpty();

		if (hasChildIssues) {
			throw new InvalidOperationException(
				String.format("Cannot delete issue with child issues. issueKey: %s", issueKey)
			);
		}
	}

	private void updateTimestamps(IssueStatus newStatus) {
		if (newStatus == IN_PROGRESS && startedAt == null) {
			startedAt = LocalDateTime.now();
			return;
		}
		if (newStatus == IssueStatus.IN_REVIEW) {
			reviewRequestedAt = LocalDateTime.now();
			return;
		}
		if (newStatus == IssueStatus.DONE) {
			finishedAt = LocalDateTime.now();
		}
	}

	/**
	 * Todo
	 *  - 설정 엔티티를 구현하면, forceReviewEnabled=true인 경우 다음 구현
	 *   - 리뷰가 강제되는 리뷰의 difficulty 수준을 설정 할 수 있도록 구현
	 *   - 리뷰를 하지 않아도 되는 priority 수준을 설정 할 수 있도록 구현
	 *   - 자식 이슈가 무조건 DONE이어야지 부모 이슈를 DONE으로 변경 가능하도록 만들기? -> 설정으로 제공할 수 있게 ㄱㄱ
	 *     - 구현한다면, "부모-자식"을 "BLOCKED_BY 자식-BLOCKS 부모" 관계가 자동으로 설정되도록 관계 설정 서비스도 같이 호출
	 */
	protected void validateStatusTransition(IssueStatus newStatus) {
		// 기본 상태 전이 검증
		validateBasicTransition(newStatus);

		// 특수한 상태 전이 검증
		if (newStatus == IssueStatus.DONE) {
			validateTransitionToDone();
		}
	}

	private void validateBasicTransition(IssueStatus newStatus) {
		Set<IssueStatus> allowedStatuses = getAllowedNextStatuses();

		boolean statusNotAllowed = !allowedStatuses.contains(newStatus);

		if (statusNotAllowed) {
			throw new InvalidOperationException(
				String.format("Cannot transition status from %s to %s.", status, newStatus)
			);
		}
	}

	private void validateTransitionToDone() {
		/*
		 * Todo: 워크스페이스 마다 가지는 설정을 관리하는 엔티티를 만들자.
		 *  - isForceReviewEnabled==true: DONE으로 변경하기 위해서는 리뷰어 등록, 모든 리뷰가 APPROVED이어야 함
		 */
		// if (!isForceReviewEnabled) {
		// 	return;
		// }

		if (reviewers.isEmpty()) {
			throw new InvalidOperationException("Review is required to complete this issue.");
		}
		if (isAllReviewsNotApproved()) {
			throw new InvalidOperationException("All reviews must be approved to complete this issue.");
		}

		validateBlockingIssuesAreDone();
	}

	private boolean isAllReviewsNotApproved() {
		return !reviewers.stream()
			.allMatch(reviewer ->
				reviewer.getCurrentReviewStatus(currentReviewRound) == ReviewStatus.APPROVED
			);
	}

	private void validateBlockingIssuesAreDone() {
		List<Issue> blockingIssues = incomingRelations.stream()
			.filter(relation -> relation.getRelationType() == IssueRelationType.BLOCKED_BY)
			.map(IssueRelation::getSourceIssue)
			.filter(issue -> issue.getStatus() != IssueStatus.DONE)
			.toList();

		if (!blockingIssues.isEmpty()) {
			String blockingIssueKeys = blockingIssues.stream()
				.map(Issue::getIssueKey)
				.collect(Collectors.joining(", "));

			throw new InvalidOperationException(
				String.format(
					"Cannot complete this issue. Blocking issues must be completed first: %s",
					blockingIssueKeys
				)
			);
		}
	}

	private Set<IssueStatus> getAllowedNextStatuses() {
		return switch (status) {
			case TODO -> Set.of(IN_PROGRESS, PAUSED, CLOSED);
			case IN_PROGRESS -> Set.of(IN_REVIEW, PAUSED, DONE, CLOSED);
			case IN_REVIEW -> Set.of(CHANGES_REQUESTED, DONE);
			case CHANGES_REQUESTED -> Set.of(IN_REVIEW);
			case PAUSED -> Set.of(IN_PROGRESS, CLOSED);
			case DONE -> Set.of();
			case CLOSED -> Set.of();
		};
	}

	protected abstract void validateParentIssue(Issue parentIssue);

}
