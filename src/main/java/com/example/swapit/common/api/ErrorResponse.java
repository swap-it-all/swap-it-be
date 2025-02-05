package com.example.swapit.common.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonPropertyOrder({"status", "errorCode", "message"})
public class ErrorResponse {
	private int status;
	private String errorCode;
	private String message;
}
