package com.uranus.taskmanager.api.security.authentication.presentation.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uranus.taskmanager.api.common.ApiResponse;
import com.uranus.taskmanager.api.security.authentication.interceptor.LoginRequired;
import com.uranus.taskmanager.api.security.authentication.presentation.dto.request.LoginRequest;
import com.uranus.taskmanager.api.security.authentication.presentation.dto.response.LoginResponse;
import com.uranus.taskmanager.api.security.authentication.service.AuthenticationService;
import com.uranus.taskmanager.api.security.session.SessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService authenticationService;
	private final SessionManager sessionManager;

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
		HttpServletRequest request) {

		LoginResponse loginResponse = authenticationService.login(loginRequest);
		sessionManager.createLoginSession(request.getSession(), loginResponse);

		return ApiResponse.ok("Login successful.", loginResponse);
	}

	@LoginRequired
	@PostMapping("/logout")
	public ApiResponse<Void> logout(HttpServletRequest request) {

		sessionManager.invalidateSession(request);

		return ApiResponse.okWithNoContent("Logout successful.");
	}

}