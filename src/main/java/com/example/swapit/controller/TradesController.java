package com.example.swapit.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.TradesRequestDto;
import com.example.swapit.service.TradesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TradesController {
	private final TradesService tradesService;

	@PostMapping("/user/swap/request")
	public ApiResponse<Void> requestTrade(@RequestBody @Valid TradesRequestDto tradesRequestDto) {
		tradesService.requestTrade(tradesRequestDto);
		return ApiResponse.success();
	}

	@DeleteMapping("/user/swap/cancel/{tradesId}")
	public ApiResponse<Void> cancelTrade(@PathVariable Long tradesId) {
		tradesService.cancelTrade(tradesId);
		return ApiResponse.success();
	}

	@PatchMapping("/user/swap/accept/{tradesId}")
	public ApiResponse<Void> acceptTrade(@PathVariable Long tradesId) {
		tradesService.acceptTrade(tradesId);
		return ApiResponse.success();
	}

	@PatchMapping("/user/swap/reject/{tradesId}")
	public ApiResponse<Void> rejectTrade(@PathVariable Long tradesId) {
		tradesService.rejectTrade(tradesId);
		return ApiResponse.success();
	}
}