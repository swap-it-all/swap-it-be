package com.example.swapit.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private ErrorCode errorCode;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public int getStatusCode() {
		return errorCode.getStatusCode();
	}
}
