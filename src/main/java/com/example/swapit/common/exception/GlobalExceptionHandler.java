package com.example.swapit.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.swapit.common.api.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		return ResponseEntity
			.status(e.getStatusCode())
			.body(new ErrorResponse(
				e.getStatusCode(),
				e.getErrorCode().name(),
				e.getMessage()
			));
	}
}
