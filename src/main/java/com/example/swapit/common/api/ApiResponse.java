package com.example.swapit.common.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"success", "message", "results"})
public class ApiResponse<T> {
	private final boolean success;
	private final String message;
	private final T results;

	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(true, "요청에 성공하였습니다.", null);
	}

	public static <T> ApiResponse<T> success(String message) {
		return new ApiResponse<>(true, message, null);
	}

	public static <T> ApiResponse<T> success(T results) {
		return new ApiResponse<>(true, "요청에 성공하였습니다.", results);
	}

	public static <T> ApiResponse<T> success(String message, T results) {
		return new ApiResponse<>(true, message, results);
	}

	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(false, message, null);
	}
}
