package com.uranus.taskmanager.api.authentication.exception;

import org.springframework.http.HttpStatus;

public class UserNotLoggedInException extends AuthenticationException {

	private static final String MESSAGE = "Login is required to access.";
	private static final HttpStatus HTTP_STATUS = HttpStatus.UNAUTHORIZED;

	public UserNotLoggedInException() {
		super(MESSAGE, HTTP_STATUS);
	}

	public UserNotLoggedInException(Throwable cause) {
		super(MESSAGE, HTTP_STATUS, cause);
	}
}