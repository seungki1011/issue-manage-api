package com.tissue.api.issue.domain.enums;

public enum IssueRelationType {

	RELEVANT,
	BLOCKS,
	BLOCKED_BY,
	CAUSES,
	CAUSED_BY;

	public IssueRelationType getOpposite() {
		return switch (this) {
			case BLOCKS -> BLOCKED_BY;
			case BLOCKED_BY -> BLOCKS;
			case CAUSES -> CAUSED_BY;
			case CAUSED_BY -> CAUSES;
			case RELEVANT -> RELEVANT;
		};
	}
}
