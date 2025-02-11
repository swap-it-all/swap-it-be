package com.example.swapit.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.swapit.common.api.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.error("[유효하지 않은 매개변수 Exception 발생] errorMessage : {}", e.getMessage());
		return ResponseEntity
			.status(e.getStatusCode())
			.body(new ErrorResponse(
				e.getStatusCode().value(),
				ErrorCode.VALIDATION_FAIL.name(),
				e.getMessage()
			));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException e) {
		log.error("[파일 용량 초과 Exception 발생] errorMessage : {}", e.getMessage());
		return ResponseEntity
			.status(e.getStatusCode())
			.body(new ErrorResponse(
				e.getStatusCode().value(),
				ErrorCode.FILE_SIZE_EXCEEDED.name(),
				ErrorCode.FILE_SIZE_EXCEEDED.getMessage()
			));
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		log.error("[Custom Exception 발생] errorCode : {}, errorMessage : {}", e.getErrorCode(), e.getMessage());
		return ResponseEntity
			.status(e.getStatusCode())
			.body(new ErrorResponse(
				e.getStatusCode(),
				e.getErrorCode().name(),
				e.getMessage()));
	}
}