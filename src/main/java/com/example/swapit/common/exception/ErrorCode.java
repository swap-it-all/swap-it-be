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
	WEBSOCKET_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "웹소켓 권한이 없습니다."),

	// user error
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "권한이 없습니다."),

	// fcm error
	FCM_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "FCM 토큰이 잘못되었습니다."),
	FIREBASE_MESSAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 푸시 알림 전송에 실패하였습니다."),

	// goods error
	GOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "물건을 찾을 수 없습니다."),
	MISSING_CURSOR_CREATEDAT(HttpStatus.BAD_REQUEST, "최신순에는 createdAt 필드값 입력이 필수입니다."),
	MISSING_CURSOR_VALUE(HttpStatus.BAD_REQUEST, "정렬 조건에 맞는 cursor 필드 값이 필요합니다."),
	INVALID_SORT_BY(HttpStatus.BAD_REQUEST, "잘못된 정렬 조건입니다."),

	// image error
	IMAGE_READ_FAILED(HttpStatus.BAD_REQUEST, "이미지를 읽는데 실패하였습니다."),
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AWS S3 이미지 업로드에 실패했습니다. 다시 시도해주세요."),
	S3_NETWORK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AWS S3 통신에 실패했습니다."),
	IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 10개까지 올릴 수 있습니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 최대 한도를 초과했습니다. (5MB)"),
	INVALID_IMAGE_FORMAT(HttpStatus.BAD_REQUEST, "파일 확장자는 JPG, JPEG, PNG만 가능합니다."),
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),

	// categories error
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."),

	// login error
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
	NEW_REFRESH_TOKEN_FAIL(HttpStatus.BAD_REQUEST, "리프레시 토큰 발급에 실패했습니다."),
	INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
	GET_USER_INFO_FAIL(HttpStatus.BAD_REQUEST, "사용자 정보 조회에 실패했습니다."),
	LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),

	// trades error
	TRADES_NOT_FOUND(HttpStatus.NOT_FOUND, "거래를 찾을 수 없습니다."),
	TRADE_UNAUTHORIZED(HttpStatus.FORBIDDEN, "거래에 대한 권한이 없습니다."),
	DUPLICATE_TRADE_REQUEST(HttpStatus.BAD_REQUEST, "이미 해당 물건에 스왑 요청한 내역이 있습니다."),
	MAXIMUM_TRADE_REQUEST(HttpStatus.BAD_REQUEST, "가능한 스왑 요청 횟수를 초과하였습니다."),

	// chat error
	CHATROOMS_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

	// notifications error
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 내역을 찾을 수 없습니다."),

	// review error
	REVIEW_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "이 거래에 대한 리뷰를 작성할 권한이 없습니다."),
	REVIEW_TRADE_NOT_COMPLETED(HttpStatus.CONFLICT, "거래가 완료되지 않아 리뷰를 작성할 수 없습니다."),
	REVIEW_DUPLICATE(HttpStatus.CONFLICT, "이미 이 거래에 대한 리뷰를 작성했습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	public int getStatusCode() {
		return httpStatus.value();
	}
}