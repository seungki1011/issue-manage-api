package com.tissue.api.issue.presentation.dto.request.update;

import java.time.LocalDate;

import com.tissue.api.common.validator.annotation.size.text.ContentText;
import com.tissue.api.common.validator.annotation.size.text.LongText;
import com.tissue.api.common.validator.annotation.size.text.ShortText;
import com.tissue.api.common.validator.annotation.size.text.StandardText;
import com.tissue.api.issue.domain.Issue;
import com.tissue.api.issue.domain.enums.IssuePriority;
import com.tissue.api.issue.domain.enums.IssueType;
import com.tissue.api.issue.domain.types.Epic;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateEpicRequest(
	@ShortText
	@NotBlank(message = "{valid.notblank}")
	String title,

	@ContentText
	@NotBlank(message = "{valid.notblank}")
	String content,

	@StandardText
	String summary,

	IssuePriority priority,
	LocalDate dueDate,

	@LongText
	@NotBlank(message = "{valid.notblank}")
	String businessGoal,

	LocalDate targetReleaseDate,
	LocalDate hardDeadLine

) implements UpdateIssueRequest {

	@Override
	public IssueType getType() {
		return IssueType.EPIC;
	}

	@Override
	public void update(Issue issue) {
		Epic epic = (Epic)issue;

		epic.updateTitle(title);
		epic.updateContent(content);
		epic.updateSummary(summary);
		epic.updatePriority(priority);
		epic.updateDueDate(dueDate);
		epic.updateBusinessGoal(businessGoal);
		epic.updateTargetReleaseDate(targetReleaseDate);
		epic.updateHardDeadLine(hardDeadLine);
	}
}
