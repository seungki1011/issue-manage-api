package com.tissue.api.position.exception;

import org.springframework.http.HttpStatus;

import com.tissue.api.common.exception.domain.PositionException;

public class PositionInUseException extends PositionException {
	private static final String MESSAGE = "Cannot delete position that is in use.";
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

	public PositionInUseException() {
		super(MESSAGE, HTTP_STATUS);
	}

	public PositionInUseException(Throwable cause) {
		super(MESSAGE, HTTP_STATUS, cause);
	}
}
