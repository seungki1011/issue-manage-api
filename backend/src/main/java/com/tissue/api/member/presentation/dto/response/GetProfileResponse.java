package com.tissue.api.member.presentation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tissue.api.member.domain.JobType;
import com.tissue.api.member.domain.Member;

public record GetProfileResponse(
	String loginId,
	String email,

	String lastName,
	String firstName,
	LocalDate birthDate,
	JobType jobType,
	String biography,

	int ownedWorkspaceCount,

	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static GetProfileResponse from(Member member) {
		return new GetProfileResponse(
			member.getLoginId(),
			member.getEmail(),
			member.getName().getLastName(),
			member.getName().getFirstName(),
			member.getBirthDate(),
			member.getJobType(),
			member.getBiography(),
			member.getMyWorkspaceCount(),
			member.getCreatedDate(),
			member.getLastModifiedDate()
		);
	}
}
