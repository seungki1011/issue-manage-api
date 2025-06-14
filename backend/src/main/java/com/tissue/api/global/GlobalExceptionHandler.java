package com.tissue.api.global;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.tissue.api.common.dto.ApiResponse;
import com.tissue.api.common.dto.FieldErrorDto;
import com.tissue.api.common.exception.TissueException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo
 *  - AOP를 사용한 로깅 적용
 *  - 중복 로직 제거: CommonException을 처리하는 핸들러로 수정
 */
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ApiResponse<Void> unexpectedException(Exception exception) {
		log.error("Unexpected Exception: ", exception);

		return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR,
			"A unexpected problem has occured",
			null);
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(RuntimeException.class)
	public ApiResponse<Void> unexpectedRuntimeException(RuntimeException exception) {
		log.error("Unexpected RuntimeException:", exception);

		return ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR,
			"A unexpected problem has occured",
			null);
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ApiResponse<List<FieldErrorDto>> handleValidationException(MethodArgumentNotValidException exception) {
		log.error("Validation Exception: ", exception);

		List<FieldErrorDto> errors = extractFieldErrors(exception.getBindingResult());

		return ApiResponse.fail(HttpStatus.BAD_REQUEST,
			"One or more fields have validation errors",
			errors);
	}

	private List<FieldErrorDto> extractFieldErrors(BindingResult bindingResult) {
		return bindingResult.getFieldErrors().stream()
			.map(this::toFieldErrorDto)
			.toList();
	}

	private FieldErrorDto toFieldErrorDto(FieldError error) {
		String rejectedValue = Objects.toString(error.getRejectedValue(), "");
		return new FieldErrorDto(error.getField(), rejectedValue, error.getDefaultMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		// enum 타입 변환 실패로 인한 예외인 경우
		if (ex.getCause() instanceof InvalidFormatException) {
			return ApiResponse.fail(HttpStatus.BAD_REQUEST, "Invalid enum value provided", null);
		}

		// 그 외의 요청 본문 관련 예외
		return ApiResponse.fail(HttpStatus.BAD_REQUEST, "Invalid request body", null);
	}

	@ExceptionHandler(TissueException.class)
	public ResponseEntity<ApiResponse<Void>> handleTissueException(TissueException exception) {
		log.error("Tissue Exception: ", exception);

		String message = exception.getMessage();

		return ResponseEntity
			.status(exception.getHttpStatus())
			.body(ApiResponse.fail(exception.getHttpStatus(), message, null));
	}
}
