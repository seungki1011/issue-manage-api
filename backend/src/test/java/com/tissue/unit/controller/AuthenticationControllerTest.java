package com.tissue.unit.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import com.tissue.api.security.authentication.presentation.dto.request.LoginRequest;
import com.tissue.api.security.authentication.presentation.dto.response.LoginResponse;
import com.tissue.api.security.session.SessionAttributes;
import com.tissue.support.helper.ControllerTestHelper;

import jakarta.servlet.http.HttpServletRequest;

class AuthenticationControllerTest extends ControllerTestHelper {

	@Test
	@DisplayName("POST /auth/login - 로그인에 성공하면 OK를 기대하고, 세션에 로그인ID가 저장된다")
	void test1() throws Exception {
		// given
		LoginRequest loginRequest = LoginRequest.builder()
			.identifier("user123")
			.password("password123!")
			.build();
		LoginResponse loginResponse = LoginResponse.builder()
			.memberId(1L)
			.loginId("user123")
			.email("test@gmail.com")
			.build();
		when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_LOGIN_ID, "user123");

		// when & then
		mockMvc.perform((post("/api/v1/auth/login")
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))))
			.andExpect(jsonPath("$.data.loginId").value("user123"))
			.andExpect(status().isOk())
			.andDo(print());

		assertThat(session.getAttribute(SessionAttributes.LOGIN_MEMBER_LOGIN_ID)).isEqualTo(loginResponse.loginId());
	}

	@Test
	@DisplayName("POST /auth/login - 로그인 시 비밀번호 필드가 비어있으면 검증에 실패한다")
	void test2() throws Exception {
		// given
		LoginRequest loginRequest = LoginRequest.builder()
			.identifier("user123")
			.password("")
			.build();

		// when & then
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Accept-Language", "en")
				.content(objectMapper.writeValueAsString(loginRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(
				jsonPath("$.data..message").value(messageSource.getMessage("valid.notblank", null, Locale.ENGLISH)))
			.andDo(print());
	}

	@Test
	@DisplayName("POST /auth/logout - 로그아웃 시 세션 무효화가 호출된다")
	void test3() throws Exception {
		// given
		MockHttpSession session = new MockHttpSession();
		session.setAttribute(SessionAttributes.LOGIN_MEMBER_ID, 1L);

		doNothing().when(sessionValidator).validateLoginStatus(any());

		// when & then
		mockMvc.perform(post("/api/v1/auth/logout")
				.session(session))
			.andExpect(status().isOk())
			.andDo(print());

		verify(sessionManager).invalidateSession(any(HttpServletRequest.class));
	}

}
