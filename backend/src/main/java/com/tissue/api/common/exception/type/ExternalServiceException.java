package com.tissue.api.common.exception.type;

import org.springframework.http.HttpStatus;

import com.tissue.api.common.exception.TissueException;

public class ExternalServiceException extends TissueException {
	public ExternalServiceException(String message) {
		super(message, HttpStatus.BAD_GATEWAY);
	}
}
