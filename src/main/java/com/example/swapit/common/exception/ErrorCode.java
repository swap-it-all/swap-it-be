package com.example.swapit.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ErrorCode {

	// common error
	SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내에 문제가 발생했습니다."),
	JSON_TYPE_ERROR(HttpStatus.BAD_REQUEST, "JSON 타입이 맞지 않습니다."),
	VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "입력 형식이 올바르지 않습니다."),

	// user error
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

	// goods error
	GOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "물건을 찾을 수 없습니다.");


	private final HttpStatus httpStatus;
	private final String message;

	public int getStatusCode() {
		return httpStatus.value();
	}
}
