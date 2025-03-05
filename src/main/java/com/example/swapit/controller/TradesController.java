package com.example.swapit.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.trade.MyGoodsDto;
import com.example.swapit.domain.dto.trade.MyRequestDto;
import com.example.swapit.domain.dto.trade.TradeMyGoodsRequestDto;
import com.example.swapit.domain.dto.trade.TradesGoodsListResponseDto;
import com.example.swapit.domain.dto.trade.TradesRequestDto;
import com.example.swapit.service.TradesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/swap")
@RequiredArgsConstructor
public class TradesController {
	private final TradesService tradesService;

	@PostMapping("/request")
	public ApiResponse<Long> requestTrade(@RequestBody @Valid TradesRequestDto tradesRequestDto) {
		return ApiResponse.success(tradesService.requestTrade(tradesRequestDto));
	}

	@DeleteMapping("/cancel/{tradesId}")
	public ApiResponse<Void> cancelTrade(@PathVariable Long tradesId) {
		tradesService.cancelTrade(tradesId);
		return ApiResponse.success();
	}

	@PatchMapping("/accept/{tradesId}")
	public ApiResponse<Void> acceptTrade(@PathVariable Long tradesId) {
		tradesService.acceptTrade(tradesId);
		return ApiResponse.success();
	}

	@PatchMapping("/reject/{tradesId}")
	public ApiResponse<Void> rejectTrade(@PathVariable Long tradesId) {
		tradesService.rejectTrade(tradesId);
		return ApiResponse.success();
	}

	@PatchMapping("/complete/{tradesId}")
	public ApiResponse<Void> completeTrade(@PathVariable Long tradesId) {
		tradesService.completeTrade(tradesId);
		return ApiResponse.success();
	}

	@GetMapping("/my-goods")
	public ApiResponse<TradesGoodsListResponseDto<MyGoodsDto>> getMyGoods() {
		return ApiResponse.success(new TradesGoodsListResponseDto<>(tradesService.getMyGoods()));
	}

	@GetMapping("/my-goods/{goodsId}/requests")
	public ApiResponse<TradeMyGoodsRequestDto> getGoodsRequests(@PathVariable Long goodsId) {
		return ApiResponse.success(tradesService.getGoodsRequests(goodsId));
	}

	@GetMapping("/my-requests")
	public ApiResponse<TradesGoodsListResponseDto<MyRequestDto>> getMyRequests() {
		return ApiResponse.success(new TradesGoodsListResponseDto<>(tradesService.getMyRequests()));
	}
}