package com.tissue.api.member.application.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tissue.api.member.domain.model.Member;
import com.tissue.api.member.exception.MemberNotFoundException;
import com.tissue.api.member.infrastructure.repository.MemberQueryRepository;
import com.tissue.api.member.presentation.dto.response.query.GetProfileResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberQueryService {

	private final MemberQueryRepository memberQueryRepository;

	/**
	 * Todo
	 *  - DTO(ProfileResponse)로 응답을 받도록 MemberQueryRepository에 메서드 정의, 수정
	 */
	@Transactional(readOnly = true)
	public GetProfileResponse getProfile(Long memberId) {
		Member member = memberQueryRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId));

		return GetProfileResponse.from(member);
	}
}
