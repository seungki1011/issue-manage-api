package com.uranus.taskmanager.api.workspace.exception;

import org.springframework.http.HttpStatus;

import com.uranus.taskmanager.api.common.exception.WorkspaceException;

public class WorkspaceCodeCollisionHandleException extends WorkspaceException {

	private static final String MESSAGE = "Failed to solve workspace code collision";
	private static final HttpStatus HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

	public WorkspaceCodeCollisionHandleException() {
		super(MESSAGE, HTTP_STATUS);
	}

	public WorkspaceCodeCollisionHandleException(Throwable cause) {
		super(MESSAGE, HTTP_STATUS, cause);
	}
}