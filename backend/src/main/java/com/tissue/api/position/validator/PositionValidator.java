package com.tissue.api.position.validator;

import org.springframework.stereotype.Component;

import com.tissue.api.common.exception.type.InvalidOperationException;
import com.tissue.api.position.domain.model.Position;
import com.tissue.api.position.infrastructure.repository.PositionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PositionValidator {

	private final PositionRepository positionRepository;

	public void validatePositionIsUsed(Position position) {
		if (positionRepository.existsByWorkspaceMembers(position)) {
			throw new InvalidOperationException(
				String.format(
					"There is a workspace member that is using this position. position id: %d, position name: %s",
					position.getId(), position.getName()
				)
			);
		}
	}
}
