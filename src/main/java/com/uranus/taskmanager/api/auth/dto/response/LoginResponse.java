package com.uranus.taskmanager.api.auth.dto.response;

import com.uranus.taskmanager.api.member.domain.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

	private final String loginId;
	private final String email;

	public static LoginResponse fromEntity(Member member) {
		return LoginResponse.builder()
			.loginId(member.getLoginId())
			.email(member.getEmail())
			.build();
	}

}
