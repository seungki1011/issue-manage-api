package com.uranus.taskmanager.api.member.presentation.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uranus.taskmanager.api.common.ApiResponse;
import com.uranus.taskmanager.api.member.presentation.dto.request.MemberEmailUpdateRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.MemberPasswordUpdateRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.MemberWithdrawRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.SignupRequest;
import com.uranus.taskmanager.api.member.presentation.dto.request.UpdateAuthRequest;
import com.uranus.taskmanager.api.member.presentation.dto.response.MemberEmailUpdateResponse;
import com.uranus.taskmanager.api.member.presentation.dto.response.MyWorkspacesResponse;
import com.uranus.taskmanager.api.member.presentation.dto.response.SignupResponse;
import com.uranus.taskmanager.api.member.service.MemberQueryService;
import com.uranus.taskmanager.api.member.service.MemberService;
import com.uranus.taskmanager.api.security.authentication.interceptor.LoginRequired;
import com.uranus.taskmanager.api.security.authentication.presentation.dto.LoginMember;
import com.uranus.taskmanager.api.security.authentication.resolver.ResolveLoginMember;
import com.uranus.taskmanager.api.security.session.SessionManager;
import com.uranus.taskmanager.api.security.session.SessionValidator;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
	/**
	 * Todo
	 *  - 회원 정보 조회
	 *    - 나의 이메일, 비밀번호, 가입 날짜
	 *  - 비밀번호 찾기 (세션 불필요)
	 * 	  - 가입한 이메일, 로그인 ID를 통한 비밀번호 찾기
	 * 	  - 기입한 로그인 ID, 이메일이 일치하면 이메일로 임시 비밀번호 보내기
	 * 	  - 또는 비밀번호 재설정 링크 보내기
	 */
	private final MemberService memberService;
	private final MemberQueryService memberQueryService;

	private final SessionManager sessionManager;
	private final SessionValidator sessionValidator;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/signup")
	public ApiResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {

		SignupResponse response = memberService.signup(request);
		return ApiResponse.created("Signup successful.", response);
	}

	@LoginRequired
	@PostMapping("/update-auth")
	public ApiResponse<Void> getUpdateAuthorization(
		@RequestBody @Valid UpdateAuthRequest request,
		@ResolveLoginMember LoginMember loginMember,
		HttpSession session) {

		memberQueryService.validatePasswordForUpdate(request, loginMember.getId());
		sessionManager.createUpdatePermission(session);

		return ApiResponse.okWithNoContent("Update authorization granted.");
	}

	@LoginRequired
	@PatchMapping("/email")
	public ApiResponse<MemberEmailUpdateResponse> updateEmail(
		@RequestBody @Valid MemberEmailUpdateRequest request,
		@ResolveLoginMember LoginMember loginMember,
		HttpSession session) {

		sessionValidator.validateUpdatePermission(session);
		MemberEmailUpdateResponse response = memberService.updateEmail(request, loginMember.getId());
		sessionManager.updateSessionEmail(session, request.getUpdateEmail());

		return ApiResponse.ok("Email update successful.", response);
	}

	@LoginRequired
	@PatchMapping("/password")
	public ApiResponse<Void> updatePassword(
		@RequestBody @Valid MemberPasswordUpdateRequest request,
		@ResolveLoginMember LoginMember loginMember,
		HttpSession session) {

		sessionValidator.validateUpdatePermission(session);
		memberService.updatePassword(request, loginMember.getId());

		return ApiResponse.okWithNoContent("Password update successful.");
	}

	@LoginRequired
	@DeleteMapping
	public ApiResponse<Void> withdrawMember(
		@RequestBody MemberWithdrawRequest request,
		@ResolveLoginMember LoginMember loginMember,
		HttpSession session) {

		sessionValidator.validateUpdatePermission(session);
		memberService.withdrawMember(request, loginMember.getId());
		session.invalidate();

		return ApiResponse.okWithNoContent("Member withdrawal successful.");
	}

	/**
	 * Todo
	 *  - MemberWorkspaceController로 이동
	 */
	@LoginRequired
	@GetMapping("/workspaces")
	public ApiResponse<MyWorkspacesResponse> getMyWorkspaces(
		@ResolveLoginMember LoginMember loginMember,
		Pageable pageable) {

		MyWorkspacesResponse response = memberQueryService.getMyWorkspaces(loginMember.getId(), pageable);

		return ApiResponse.ok("Currently joined workspaces found.", response);
	}
}