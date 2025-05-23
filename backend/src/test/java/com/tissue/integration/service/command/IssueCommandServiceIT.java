package com.tissue.integration.service.command;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.common.exception.type.InvalidOperationException;
import com.tissue.api.issue.domain.model.Issue;
import com.tissue.api.issue.domain.model.enums.IssuePriority;
import com.tissue.api.issue.domain.model.types.Story;
import com.tissue.api.issue.presentation.controller.dto.request.AddParentIssueRequest;
import com.tissue.api.issue.presentation.controller.dto.request.create.CommonIssueCreateFields;
import com.tissue.api.issue.presentation.controller.dto.request.create.CreateTaskRequest;
import com.tissue.api.issue.presentation.controller.dto.request.update.CommonIssueUpdateFields;
import com.tissue.api.issue.presentation.controller.dto.request.update.UpdateStoryRequest;
import com.tissue.api.issue.presentation.controller.dto.response.IssueResponse;
import com.tissue.api.member.domain.model.Member;
import com.tissue.api.workspace.domain.model.Workspace;
import com.tissue.api.workspacemember.domain.model.WorkspaceMember;
import com.tissue.api.workspacemember.domain.model.enums.WorkspaceRole;
import com.tissue.support.helper.ServiceIntegrationTestHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class IssueCommandServiceIT extends ServiceIntegrationTestHelper {

	Workspace workspace;
	WorkspaceMember owner;
	WorkspaceMember workspaceMember1;
	WorkspaceMember workspaceMember2;

	Member ownerMember;
	Member member1;
	Member member2;

	@BeforeEach
	void setUp() {
		// create workspace
		workspace = testDataFixture.createWorkspace(
			"test workspace",
			null,
			null
		);

		// create member
		ownerMember = testDataFixture.createMember("owner");
		member1 = testDataFixture.createMember("member1");
		member2 = testDataFixture.createMember("member2");

		// add workspace members
		owner = testDataFixture.createWorkspaceMember(
			ownerMember,
			workspace,
			WorkspaceRole.OWNER
		);
		workspaceMember1 = testDataFixture.createWorkspaceMember(
			member1,
			workspace,
			WorkspaceRole.MEMBER
		);
		workspaceMember2 = testDataFixture.createWorkspaceMember(
			member2,
			workspace,
			WorkspaceRole.MEMBER
		);
	}

	@AfterEach
	public void tearDown() {
		databaseCleaner.execute();
	}

	@Test
	@Transactional
	@DisplayName("워크스페이스 멤버는 이슈를 생성할 수 있다(Task 생성)")
	void canCreateTaskIssue() {
		// given
		CreateTaskRequest request = CreateTaskRequest.builder()
			.common(CommonIssueCreateFields.builder()
				.title("test issue")
				.content("test content")
				.priority(IssuePriority.MEDIUM)
				.dueAt(LocalDateTime.now().plusDays(7))
				.build())
			.build();

		// when
		IssueResponse response = issueCommandService.createIssue(
			workspace.getCode(),
			member1.getId(),
			request
		);

		// then
		Issue issue = issueRepository.findById(1L).orElseThrow();

		assertThat(response.workspaceCode()).isEqualTo(issue.getWorkspaceCode());
		assertThat(response.issueKey()).isEqualTo(issue.getIssueKey());

		assertThat(issue.getTitle()).isEqualTo("test issue");
	}

	@Test
	@Transactional
	@DisplayName("제일 처음 생성된 이슈의 이슈키는 'ISSUE-1'이어야 한다")
	void whenFirstIssueIsCreatedIssueKeyMustBe_ISSUE_1() {
		// given
		CreateTaskRequest request = CreateTaskRequest.builder()
			.common(CommonIssueCreateFields.builder()
				.title("task issue")
				.content("task issue")
				.priority(IssuePriority.HIGH)
				.dueAt(LocalDateTime.now().plusDays(7))
				.build())
			.build();

		// when
		IssueResponse response = issueCommandService.createIssue(
			workspace.getCode(),
			member1.getId(),
			request
		);

		// then
		assertThat(response.issueKey()).isEqualTo("ISSUE-1");
		assertThat(response.workspaceCode()).isEqualTo(workspace.getCode());
	}

	@Test
	@Transactional
	@DisplayName("이슈키 prefix를 설정하지 않은 경우, 두 번째 이슈의 이슈키는 'ISSUE-2'이어야 한다")
	void whenSecondIssueIsCreatedTheIssueKeyMustBe_ISSUE_2() {
		// given
		Issue firstIssue = testDataFixture.createTask(
			workspace,
			"first issue (TASK type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		CreateTaskRequest request = CreateTaskRequest.builder()
			.common(CommonIssueCreateFields.builder()
				.title("second issue (TASK type)")
				.content("second issue (TASK type)")
				.priority(IssuePriority.MEDIUM)
				.dueAt(LocalDateTime.now().plusDays(7))
				.build())
			.build();

		// when
		IssueResponse response = issueCommandService.createIssue(
			workspace.getCode(),
			member1.getId(),
			request
		);

		// then
		assertThat(response.issueKey()).isEqualTo("ISSUE-2");
	}

	@Test
	@Transactional
	@DisplayName("이슈를 업데이트할 수 있다")
	void canEditIssue() {
		// given
		Issue issue = testDataFixture.createStory(
			workspace,
			"test issue (STORY type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// issue.updateCreatedBy(workspaceMember1.getId());

		UpdateStoryRequest request = UpdateStoryRequest.builder()
			.common(CommonIssueUpdateFields.builder()
				.title("updated issue")
				.content("updated issue")
				.priority(IssuePriority.HIGH)
				.dueAt(LocalDateTime.now())
				.build())
			.userStory("updated issue")
			.acceptanceCriteria("updated issue")
			.build();

		// when
		IssueResponse response = issueCommandService.updateIssue(
			workspace.getCode(),
			issue.getIssueKey(),
			member1.getId(),
			request
		);

		// then
		assertThat(response.issueKey()).isEqualTo(issue.getIssueKey());
		assertThat(response.workspaceCode()).isEqualTo(workspace.getCode());
	}

	@Test
	@Transactional
	@DisplayName("이슈 업데이트 시, summary를 null로 업데이트 가능하다")
	void canUpdateIssueSummaryFieldAsNull() {
		// given
		Issue issue = testDataFixture.createStory(
			workspace,
			"test issue (STORY type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// issue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		UpdateStoryRequest request = UpdateStoryRequest.builder()
			.common(CommonIssueUpdateFields.builder()
				.summary(null)
				.build())
			.build();

		// when
		IssueResponse response = issueCommandService.updateIssue(
			workspace.getCode(),
			issue.getIssueKey(),
			member1.getId(),
			request
		);

		// then
		assertThat(response.issueKey()).isEqualTo(issue.getIssueKey());
		assertThat(response.workspaceCode()).isEqualTo(issue.getWorkspaceCode());

		Issue updatedIssue = issueRepository.findByIssueKeyAndWorkspaceCode(
			response.issueKey(),
			response.workspaceCode()
		).get();

		assertThat(updatedIssue.getSummary()).isNull();

	}

	@Test
	@Disabled("테스트가 마음에 안듬. 개선 필요.")
	@Transactional
	@DisplayName("이슈를 업데이트 시, nullable=false인 필드에 대해서는 요청 필드가 null인 경우 업데이트 대상에서 제외된다")
	void forNonNullableFields_IfRequestFieldIsNull_UpdateIsNotApplied() {
		// given
		Issue issue = testDataFixture.createStory(
			workspace,
			"original title",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// issue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		// all fields except for summary is null for request
		UpdateStoryRequest request = UpdateStoryRequest.builder()
			.common(CommonIssueUpdateFields.builder()
				.summary("updated summary")
				.build())
			.build();

		// when
		IssueResponse response = issueCommandService.updateIssue(
			workspace.getCode(),
			issue.getIssueKey(),
			workspaceMember1.getId(),
			request
		);

		// then
		assertThat(response.issueKey()).isEqualTo(issue.getIssueKey());

		Issue updatedIssue = issueRepository.findByIssueKeyAndWorkspaceCode(
			response.issueKey(),
			response.workspaceCode()
		).get();

		assertThat(updatedIssue.getTitle()).isEqualTo("original title");
	}

	@Test
	@Transactional
	@DisplayName("요청의 이슈 타입과 업데이트를 위해 조회한 이슈 타입은 일치해야 한다")
	void updateIssueTypeMismatchIsNotAllowed() {
		// given
		Issue issue = testDataFixture.createTask(
			workspace,
			"test issue (TASK type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// issue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		UpdateStoryRequest request = UpdateStoryRequest.builder()
			.common(CommonIssueUpdateFields.builder()
				.title("updated issue")
				.content("updated issue")
				.priority(IssuePriority.HIGH)
				.dueAt(LocalDateTime.now().plusDays(7))
				.build())
			.userStory("updated issue")
			.acceptanceCriteria("updated issue")
			.build();

		// when & then
		assertThatThrownBy(
			() -> issueCommandService.updateIssue(workspace.getCode(), issue.getIssueKey(), workspaceMember1.getId(),
				request))
			.isInstanceOf(InvalidOperationException.class);
	}

	@Test
	@Transactional
	@DisplayName("STORY의 부모로 EPIC을 등록할 수 있다")
	void canAssignEpicAsParentIssueOfStory() {
		// given
		Issue parentIssue = testDataFixture.createEpic(
			workspace,
			"parent issue (EPIC type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);
		// parentIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		Issue childIssue = testDataFixture.createStory(
			workspace,
			"child issue (STORY type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);
		// childIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		// when
		IssueResponse response = issueCommandService.assignParentIssue(
			workspace.getCode(),
			childIssue.getIssueKey(),
			member1.getId(),
			new AddParentIssueRequest(parentIssue.getIssueKey())
		);

		// then
		Issue issue = issueRepository.findByIssueKeyAndWorkspaceCode(
			response.issueKey(),
			response.workspaceCode()
		).get();

		assertThat(issue.getParentIssue().getIssueKey()).isEqualTo(parentIssue.getIssueKey());
	}

	@Test
	@Transactional
	@DisplayName("EPIC 타입 이슈를 부모로 등록하는 경우 해당 EPIC 이슈의 스토리 포인트(storyPoint)는 자식 이슈들의 포인트 합산으로 계산된다")
	void epicParentStoryPointIsCalculated_WhenChildAssignsParentIssue() {
		// given
		Issue parentIssue = testDataFixture.createEpic(
			workspace,
			"parent issue (EPIC type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);
		// parentIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		Issue childIssue1 = Story.builder()
			.workspace(workspace)
			.title("child issue 1 (STORY type)")
			.content("test")
			.dueAt(LocalDateTime.now().plusDays(7))
			.storyPoint(1)
			.userStory("test")
			.acceptanceCriteria("test")
			.build();

		issueRepository.save(childIssue1);
		// childIssue1.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		Issue childIssue2 = Story.builder()
			.workspace(workspace)
			.title("child issue 2 (STORY type)")
			.content("test")
			.dueAt(LocalDateTime.now().plusDays(7))
			.storyPoint(3)
			.userStory("test")
			.acceptanceCriteria("test")
			.build();

		issueRepository.save(childIssue2);
		// childIssue2.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		// when - assign parent to childIssue1, 2
		issueCommandService.assignParentIssue(
			workspace.getCode(),
			childIssue1.getIssueKey(),
			workspaceMember1.getId(),
			new AddParentIssueRequest(parentIssue.getIssueKey())
		);

		issueCommandService.assignParentIssue(
			workspace.getCode(),
			childIssue2.getIssueKey(),
			workspaceMember1.getId(),
			new AddParentIssueRequest(parentIssue.getIssueKey())
		);

		// then
		Issue foundParentIssue = issueRepository.findByIssueKeyAndWorkspaceCode(
			parentIssue.getIssueKey(),
			workspace.getCode()).get();

		assertThat(foundParentIssue.getStoryPoint()).isEqualTo(4);
	}

	@Test
	@Transactional
	@DisplayName("이슈 업데이트에서 스토리 포인트를 업데이트 하는 경우, 해당 이슈의 부모가 Epic이면, Epic의 스토리 포인트를 갱신한다")
	void whenUpdateStoryPoint_IfParentIsEpic_EpicStoryPointIsRecalculated() {
		// given
		Issue parentIssue = testDataFixture.createEpic(
			workspace,
			"parent issue (EPIC type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		Issue childIssue = Story.builder()
			.workspace(workspace)
			.title("child issue 1 (STORY type)")
			.content("test")
			.dueAt(LocalDateTime.now().plusDays(7))
			.storyPoint(1)
			.userStory("test")
			.acceptanceCriteria("test")
			.parentIssue(parentIssue)
			.build();
		issueRepository.save(childIssue);

		// childIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		UpdateStoryRequest request = UpdateStoryRequest.builder()
			.common(CommonIssueUpdateFields.builder()
				.build())
			.storyPoint(10) // update story point to 10
			.build();

		// when
		issueCommandService.updateIssue(
			workspace.getCode(),
			childIssue.getIssueKey(),
			workspaceMember1.getId(),
			request
		);

		// then
		Issue foundParentIssue = issueRepository.findByIssueKeyAndWorkspaceCode(
			parentIssue.getIssueKey(),
			workspace.getCode()
		).get();

		assertThat(foundParentIssue.getStoryPoint()).isEqualTo(10);
	}

	@Test
	@Transactional
	@DisplayName("이슈의 부모를 변경할 수 있다")
	void canChangeParentIssueOfIssue() {
		// given
		Issue parentIssue = testDataFixture.createEpic(
			workspace,
			"parent issue (EPIC type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);
		// parentIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		Issue childIssue = testDataFixture.createStory(
			workspace,
			"child issue (STORY type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);
		// childIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());
		childIssue.updateParentIssue(parentIssue);

		// 변경할 부모 이슈 생성
		Issue newParentIssue = testDataFixture.createEpic(
			workspace,
			"new parent issue (EPIC type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);
		// newParentIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		// when
		IssueResponse response = issueCommandService.assignParentIssue(
			workspace.getCode(),
			childIssue.getIssueKey(),
			member1.getId(),
			new AddParentIssueRequest(newParentIssue.getIssueKey())
		);

		// then
		assertThat(response.workspaceCode()).isEqualTo(workspace.getCode());
		assertThat(response.issueKey()).isEqualTo(childIssue.getIssueKey());

		Issue issue = issueRepository.findByIssueKeyAndWorkspaceCode(
			childIssue.getIssueKey(),
			workspace.getCode()
		).get();

		assertThat(issue.getParentIssue().getIssueKey()).isEqualTo(newParentIssue.getIssueKey());
	}

	@Test
	@Transactional
	@DisplayName("부모 관계를 해제할 수 있다")
	void canRemoveParentRelationship() {
		// given
		Issue parentIssue = testDataFixture.createEpic(
			workspace,
			"parent issue (EPIC type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// parentIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		Issue childIssue = testDataFixture.createStory(
			workspace,
			"child issue (STORY type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// childIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());
		childIssue.updateParentIssue(parentIssue);

		// when
		IssueResponse response = issueCommandService.removeParentIssue(
			workspace.getCode(),
			childIssue.getIssueKey(),
			member1.getId()
		);

		log.info("response = {}", response);

		// then
		assertThat(response.issueKey()).isEqualTo(childIssue.getIssueKey());

		Issue issue = issueRepository.findByIssueKeyAndWorkspaceCode(
			parentIssue.getIssueKey(),
			workspace.getCode()
		).get();

		assertThat(issue.getParentIssue()).isNull();
	}

	@Test
	@Transactional
	@DisplayName("SUBTASK의 부모 이슈는 해제할 수 없다")
	void cannotRemoveParentOfSubTask() {
		// given
		Issue parentIssue = testDataFixture.createTask(
			workspace,
			"parent issue (Task type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// parentIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());

		Issue childIssue = testDataFixture.createSubTask(
			workspace,
			"child issue (SUB_TASK type)",
			IssuePriority.MEDIUM,
			LocalDateTime.now().plusDays(7)
		);

		// childIssue.updateCreatedByWorkspaceMember(workspaceMember1.getId());
		childIssue.updateParentIssue(parentIssue);

		// when & then
		assertThatThrownBy(() -> issueCommandService.removeParentIssue(
			workspace.getCode(),
			childIssue.getIssueKey(),
			workspaceMember1.getId()
		)).isInstanceOf(InvalidOperationException.class);
	}

	//
	// @Test
	// @DisplayName("이슈 상태 업데이트를 성공하면 이슈 상태 업데이트 응답을 반환한다")
	// void updateIssueStatus_success_returnUpdateStatusResponse() {
	// 	// given
	// 	CreateIssueRequest createRequest = new CreateIssueRequest(
	// 		IssueType.TASK,
	// 		"Test Issue",
	// 		"Test issue content",
	// 		IssuePriority.HIGH,
	// 		LocalDate.now(),
	// 		null
	// 	);
	// 	issueCommandService.createIssue(workspace.getCode(), createRequest);
	//
	// 	UpdateStatusRequest updateStatusRequest = new UpdateStatusRequest(IssueStatus.IN_PROGRESS);
	//
	// 	// when
	// 	UpdateStatusResponse response = issueCommandService.updateIssueStatus(1L, workspace.getCode(), updateStatusRequest);
	//
	// 	// then
	// 	assertThat(response.issueId()).isEqualTo(1L);
	// 	assertThat(response.status()).isEqualTo(IssueStatus.IN_PROGRESS);
	// }
	//
	// @Test
	// @DisplayName("이슈 상태를 IN_REVIEW로 직접 업데이트 시도하는 경우 예외가 발생한다")
	// void updateIssueStatus_fails_ifUpdateDirectlyToInReview() {
	// 	// given
	// 	CreateIssueRequest createRequest = new CreateIssueRequest(
	// 		IssueType.TASK,
	// 		"Test Issue",
	// 		"Test issue content",
	// 		IssuePriority.HIGH,
	// 		LocalDate.now(),
	// 		null
	// 	);
	// 	issueCommandService.createIssue(workspace.getCode(), createRequest);
	//
	// 	UpdateStatusRequest updateStatusRequest = new UpdateStatusRequest(IssueStatus.IN_REVIEW);
	//
	// 	// when & then
	// 	assertThatThrownBy(() -> issueCommandService.updateIssueStatus(1L, workspace.getCode(), updateStatusRequest))
	// 		.isInstanceOf(DirectUpdateToInReviewException.class);
	// }
	//
	// @Test
	// @DisplayName("이슈 상태를 처음으로 IN_PROGRESS로 업데이트하는 경우, startedAt이 현재 날짜와 시간으로 기록된다")
	// void updateIssueStatus_toInProgress_startedAtIsRecorded() {
	// 	// given
	// 	CreateIssueRequest createRequest = new CreateIssueRequest(
	// 		IssueType.TASK,
	// 		"Test Issue",
	// 		"Test issue content",
	// 		IssuePriority.HIGH,
	// 		LocalDate.now(),
	// 		null
	// 	);
	// 	issueCommandService.createIssue(workspace.getCode(), createRequest);
	//
	// 	UpdateStatusRequest updateStatusRequest = new UpdateStatusRequest(IssueStatus.IN_PROGRESS);
	//
	// 	// when
	// 	LocalDateTime timeBeforeUpdate = LocalDateTime.now();
	// 	issueCommandService.updateIssueStatus(1L, workspace.getCode(), updateStatusRequest);
	//
	// 	// then
	// 	Issue issue = issueRepository.findById(1L).orElseThrow();
	//
	// 	assertThat(issue.getStartedAt()).isAfter(timeBeforeUpdate);
	// 	assertThat(issue.getStartedAt()).isBefore(timeBeforeUpdate.plusMinutes(1));
	// }
	//
	// @Test
	// @DisplayName("이슈 상태를 DONE으로 업데이트하는 경우 finishedAt이 현재 날짜와 시간으로 기록된다")
	// void updateIssueStatus_toDone_finishedAtIsRecorded() {
	// 	// given
	// 	CreateIssueRequest createRequest = new CreateIssueRequest(
	// 		IssueType.TASK,
	// 		"Test Issue",
	// 		"Test issue content",
	// 		IssuePriority.HIGH,
	// 		LocalDate.now(),
	// 		null
	// 	);
	// 	issueCommandService.createIssue(workspace.getCode(), createRequest);
	//
	// 	UpdateStatusRequest updateStatusRequest = new UpdateStatusRequest(IssueStatus.DONE);
	//
	// 	// when
	// 	LocalDateTime timeBeforeUpdate = LocalDateTime.now();
	// 	issueCommandService.updateIssueStatus(1L, workspace.getCode(), updateStatusRequest);
	//
	// 	// then
	// 	Issue issue = issueRepository.findById(1L).orElseThrow();
	//
	// 	assertThat(issue.getFinishedAt()).isAfter(timeBeforeUpdate);
	// 	assertThat(issue.getFinishedAt()).isBefore(timeBeforeUpdate.plusMinutes(1));
	// }
}
