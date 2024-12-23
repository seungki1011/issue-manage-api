package com.tissue.api.issue.domain.types;

import java.time.LocalDate;

import com.tissue.api.issue.exception.EpicCannotHaveParentException;
import com.tissue.api.workspace.domain.Workspace;
import com.tissue.api.issue.domain.Issue;
import com.tissue.api.issue.domain.enums.IssuePriority;
import com.tissue.api.issue.domain.enums.IssueType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("EPIC")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Epic extends Issue {

	@Column(nullable = false)
	private String businessGoal;

	private LocalDate targetReleaseDate;
	private LocalDate hardDeadLine;

	@Builder
	public Epic(
		Workspace workspace,
		String title,
		String content,
		String summary,
		IssuePriority priority,
		LocalDate dueDate,
		String businessGoal,
		LocalDate targetReleaseDate,
		LocalDate hardDeadLine
	) {
		super(workspace, IssueType.EPIC, title, content, summary, priority, dueDate);
		this.businessGoal = businessGoal;
		this.targetReleaseDate = targetReleaseDate;
		this.hardDeadLine = hardDeadLine;
	}

	@Override
	protected void validateParentIssue(Issue parentIssue) {
		throw new EpicCannotHaveParentException();
	}
}
