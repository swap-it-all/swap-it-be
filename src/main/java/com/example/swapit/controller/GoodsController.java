package com.example.swapit.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsRequestDto;
import com.example.swapit.service.GoodsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController("/api")
@RequiredArgsConstructor
public class GoodsController {

	private final GoodsService goodsService;

	@GetMapping("/all/goods/{goodsId}")
	public ApiResponse<GoodsDetailDto> getDetailGood(@PathVariable Long goodsId) {
		return ApiResponse.success(
			goodsService.getGoodDetail(goodsId)
		);
	}

	@PostMapping("/user/goods/register")
	public ApiResponse<Void> insertGood(@Valid @RequestBody GoodsRequestDto goodsRequestDto) {
		goodsService.insertGood(goodsRequestDto);
		return ApiResponse.successKeyword("물건 등록");
	}

	@PutMapping("/user/goods/{goodsId}")
	public ApiResponse<Void> updateGood(
		@PathVariable Long goodsId,
		@Valid @RequestBody GoodsRequestDto goodsRequestDto
	) {
		goodsService.updateGood(goodsId, goodsRequestDto);
		return ApiResponse.successKeyword("수정");
	}

	@DeleteMapping("/user/goods/{goodsId}")
	public ApiResponse<Void> deleteGood(@PathVariable Long goodsId) {
		goodsService.deleteGood(goodsId);
		return ApiResponse.successKeyword("삭제");
	}
}
