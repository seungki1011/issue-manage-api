package com.tissue.api.position.exception;

import org.springframework.http.HttpStatus;

import com.tissue.api.common.exception.domain.PositionException;

public class DuplicatePositionNameException extends PositionException {
	private static final String MESSAGE = "The name for the position is duplicate in this workspace.";
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

	public DuplicatePositionNameException() {
		super(MESSAGE, HTTP_STATUS);
	}

	public DuplicatePositionNameException(Throwable cause) {
		super(MESSAGE, HTTP_STATUS, cause);
	}
}
