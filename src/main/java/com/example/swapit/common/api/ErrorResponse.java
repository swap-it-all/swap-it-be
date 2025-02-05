package com.example.swapit.common.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonPropertyOrder({"status", "errorCode", "message"})
public class ErrorResponse {
	private final int status;
	private final String errorCode;
	private final String message;
}
