package com.tissue.api.sprint.presentation.dto.request;

import java.time.LocalDateTime;

import com.tissue.api.common.validator.annotation.size.text.LongText;
import com.tissue.api.common.validator.annotation.size.text.ShortText;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateSprintRequest(

	@ShortText
	@NotBlank(message = "{valid.notblank}")
	String title,

	@LongText
	@NotBlank(message = "{valid.notblank}")
	String goal,

	@NotNull(message = "{valid.notnull}")
	@FutureOrPresent(message = "{valid.startdate}")
	LocalDateTime plannedStartDate,

	@NotNull(message = "{valid.notnull}")
	@Future(message = "{valid.enddate}")
	LocalDateTime plannedEndDate
) {
}
