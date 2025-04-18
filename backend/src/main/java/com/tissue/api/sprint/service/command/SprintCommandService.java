package com.tissue.api.sprint.service.command;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.issue.domain.Issue;
import com.tissue.api.issue.service.command.IssueReader;
import com.tissue.api.sprint.domain.Sprint;
import com.tissue.api.sprint.domain.enums.SprintStatus;
import com.tissue.api.sprint.domain.event.SprintCompletedEvent;
import com.tissue.api.sprint.domain.event.SprintStartedEvent;
import com.tissue.api.sprint.domain.repository.SprintRepository;
import com.tissue.api.sprint.presentation.dto.request.AddSprintIssuesRequest;
import com.tissue.api.sprint.presentation.dto.request.CreateSprintRequest;
import com.tissue.api.sprint.presentation.dto.request.RemoveSprintIssueRequest;
import com.tissue.api.sprint.presentation.dto.request.UpdateSprintRequest;
import com.tissue.api.sprint.presentation.dto.request.UpdateSprintStatusRequest;
import com.tissue.api.sprint.presentation.dto.response.AddSprintIssuesResponse;
import com.tissue.api.sprint.presentation.dto.response.CreateSprintResponse;
import com.tissue.api.sprint.presentation.dto.response.UpdateSprintResponse;
import com.tissue.api.sprint.presentation.dto.response.UpdateSprintStatusResponse;
import com.tissue.api.workspace.domain.Workspace;
import com.tissue.api.workspace.service.command.WorkspaceReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SprintCommandService {

	private final SprintReader sprintReader;
	private final SprintRepository sprintRepository;
	private final WorkspaceReader workspaceReader;
	private final IssueReader issueReader;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public CreateSprintResponse createSprint(
		String workspaceCode,
		CreateSprintRequest request
	) {
		Workspace workspace = workspaceReader.findWorkspace(workspaceCode);

		Sprint sprint = Sprint.builder()
			.title(request.title())
			.goal(request.goal())
			.plannedStartDate(request.plannedStartDate())
			.plannedEndDate(request.plannedEndDate())
			.workspace(workspace)
			.build();

		Sprint savedSprint = sprintRepository.save(sprint);
		return CreateSprintResponse.from(savedSprint);
	}

	@Transactional
	public AddSprintIssuesResponse addIssues(
		String workspaceCode,
		String sprintKey,
		AddSprintIssuesRequest request
	) {
		Sprint sprint = sprintReader.findSprint(sprintKey, workspaceCode);

		List<Issue> issues = issueReader.findIssues(request.issueKeys(), workspaceCode);

		for (Issue issue : issues) {
			sprint.addIssue(issue);
		}

		return AddSprintIssuesResponse.of(sprint, request.issueKeys());
	}

	@Transactional
	public UpdateSprintResponse updateSprint(
		String workspaceCode,
		String sprintKey,
		UpdateSprintRequest request
	) {
		Sprint sprint = sprintReader.findSprint(sprintKey, workspaceCode);

		sprint.updateTitle(request.title() != null ? request.title() : sprint.getTitle());
		sprint.updateGoal(request.goal() != null ? request.goal() : sprint.getGoal());

		LocalDateTime startDate =
			request.plannedStartDate() != null ? request.plannedStartDate() : sprint.getPlannedStartDate();
		LocalDateTime endDate =
			request.plannedEndDate() != null ? request.plannedEndDate() : sprint.getPlannedEndDate();

		if (request.plannedStartDate() != null || request.plannedEndDate() != null) {
			sprint.updateDates(startDate, endDate);
		}

		return UpdateSprintResponse.from(sprint);
	}

	@Transactional
	public UpdateSprintStatusResponse updateSprintStatus(
		String workspaceCode,
		String sprintKey,
		UpdateSprintStatusRequest request,
		Long currentWorkspaceMemberId
	) {
		Sprint sprint = sprintReader.findSprint(sprintKey, workspaceCode);

		sprint.updateStatus(request.newStatus());

		if (sprint.getStatus() == SprintStatus.ACTIVE) {
			eventPublisher.publishEvent(SprintStartedEvent.createEvent(sprint, currentWorkspaceMemberId));
		} else if (sprint.getStatus() == SprintStatus.COMPLETED) {
			eventPublisher.publishEvent(SprintCompletedEvent.createEvent(sprint, currentWorkspaceMemberId));
		}

		return UpdateSprintStatusResponse.from(sprint);
	}

	@Transactional
	public void removeIssue(
		String workspaceCode,
		String sprintKey,
		RemoveSprintIssueRequest request
	) {
		Issue issue = issueReader.findIssueInSprint(sprintKey, request.issueKey(), workspaceCode);
		Sprint sprint = sprintReader.findSprint(sprintKey, workspaceCode);

		sprint.removeIssue(issue);
	}
}
