package com.uranus.taskmanager.api.issue.presentation.dto.request.create;

import java.time.LocalDate;

import com.uranus.taskmanager.api.issue.domain.Issue;
import com.uranus.taskmanager.api.issue.domain.enums.Difficulty;
import com.uranus.taskmanager.api.issue.domain.enums.IssuePriority;
import com.uranus.taskmanager.api.issue.domain.enums.IssueType;
import com.uranus.taskmanager.api.issue.domain.types.Task;
import com.uranus.taskmanager.api.workspace.domain.Workspace;

import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
	@NotBlank String title,
	@NotBlank String content,
	String summary,
	IssuePriority priority,
	LocalDate dueDate,
	Difficulty difficulty,
	Long parentIssueId
) implements CreateIssueRequest {

	@Override
	public IssueType getType() {
		return IssueType.TASK;
	}

	@Override
	public Issue to(Workspace workspace, Issue parentIssue) {
		return Task.builder()
			.workspace(workspace)
			.title(title)
			.content(content)
			.summary(summary)
			.priority(priority)
			.dueDate(dueDate)
			.difficulty(difficulty)
			.parentIssue(parentIssue)
			.build();
	}
}