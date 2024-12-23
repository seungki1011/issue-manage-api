package com.tissue.api.position.presentation.dto.request;

import com.tissue.api.common.ColorType;

import jakarta.validation.constraints.NotNull;

public record UpdatePositionColorRequest(
	@NotNull
	ColorType colorType
) {
}
