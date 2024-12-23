package com.tissue.api.workspacemember.presentation.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class InviteMembersRequest {

	@NotEmpty(message = "Member identifiers must not be empty")
	private Set<String> memberIdentifiers;

	private InviteMembersRequest(Set<String> memberIdentifiers) {
		this.memberIdentifiers = memberIdentifiers;
	}

	public static InviteMembersRequest of(Set<String> memberIdentifiers) {
		return new InviteMembersRequest(memberIdentifiers);
	}
}
