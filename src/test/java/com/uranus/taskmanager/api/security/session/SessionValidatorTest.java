package com.uranus.taskmanager.api.security.session;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uranus.taskmanager.api.security.authentication.exception.UserNotLoggedInException;
import com.uranus.taskmanager.api.security.authorization.exception.UpdatePermissionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class SessionValidatorTest {

	@Mock
	private HttpSession session;
	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private SessionValidator sessionValidator;

	@Test
	@DisplayName("로그인 상태 검증 - 세션이 null인 경우 예외가 발생한다")
	void validateLoginStatus_WhenSessionIsNull() {
		// given
		when(request.getSession(false)).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> sessionValidator.validateLoginStatus(request))
			.isInstanceOf(UserNotLoggedInException.class);
	}

	@Test
	@DisplayName("로그인 상태 검증 - 로그인 ID가 없는 경우 예외가 발생한다")
	void validateLoginStatus_WhenLoginIdIsNull() {
		// given
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute(SessionAttributes.LOGIN_MEMBER_ID)).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> sessionValidator.validateLoginStatus(request))
			.isInstanceOf(UserNotLoggedInException.class);
	}

	@Test
	@DisplayName("로그인 상태 검증 - 정상 케이스")
	void validateLoginStatus_Success() {
		// given
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute(SessionAttributes.LOGIN_MEMBER_ID)).thenReturn(1L);

		// when & then
		assertThatNoException()
			.isThrownBy(() -> sessionValidator.validateLoginStatus(request));
	}

	@Test
	@DisplayName("업데이트 권한 검증 - 권한이 없는 경우 예외가 발생한다")
	void validateUpdatePermission_WhenAuthIsNull() {
		// given
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH)).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> sessionValidator.validateUpdatePermission(session))
			.isInstanceOf(UpdatePermissionException.class);
	}

	@Test
	@DisplayName("업데이트 권한 검증 - 권한이 false인 경우 예외가 발생한다")
	void validateUpdatePermission_WhenAuthIsFalse() {
		// given
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> sessionValidator.validateUpdatePermission(session))
			.isInstanceOf(UpdatePermissionException.class);
	}

	@Test
	@DisplayName("업데이트 권한 검증 - 만료 시간이 null인 경우 예외가 발생한다")
	void validateUpdatePermission_WhenExpiresAtIsNull() {
		// given
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH)).thenReturn(true);
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH_EXPIRES_AT)).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> sessionValidator.validateUpdatePermission(session))
			.isInstanceOf(UpdatePermissionException.class);
	}

	@Test
	@DisplayName("업데이트 권한 검증 - 만료된 경우 예외가 발생한다")
	void validateUpdatePermission_WhenExpired() {
		// given
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH)).thenReturn(true);
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH_EXPIRES_AT))
			.thenReturn(LocalDateTime.now().minusMinutes(1));

		// when & then
		assertThatThrownBy(() -> sessionValidator.validateUpdatePermission(session))
			.isInstanceOf(UpdatePermissionException.class);
	}

	@Test
	@DisplayName("업데이트 권한 검증 - 정상 케이스")
	void validateUpdatePermission_Success() {
		// given
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH)).thenReturn(true);
		when(session.getAttribute(SessionAttributes.UPDATE_AUTH_EXPIRES_AT))
			.thenReturn(LocalDateTime.now().plusMinutes(4));

		// when & then
		assertThatNoException()
			.isThrownBy(() -> sessionValidator.validateUpdatePermission(session));
	}
}