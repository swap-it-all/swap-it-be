package com.example.swapit.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Result<T> {
	private final boolean success;
	private final T data;
	private final String message;

	public static <T> Result<T> success(T data) {
		return new Result<>(true, data, null);
	}

	public static <T> Result<T> fail(String message) {
		return new Result<>(false, null, message);
	}
}
