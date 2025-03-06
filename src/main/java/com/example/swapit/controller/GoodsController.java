package com.example.swapit.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.dto.good.GoodsDetailDto;
import com.example.swapit.domain.dto.good.GoodsDto;
import com.example.swapit.domain.dto.good.GoodsListDto;
import com.example.swapit.domain.dto.good.GoodsRequestDto;
import com.example.swapit.service.GoodsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GoodsController {

	private static final int MAX_FILES = 10;
	private final GoodsService goodsService;

	/**
	 * 배포 확인용
	 * todo : 운영 시, 삭제.
	 */
	@GetMapping("/all/sample")
	public String hello() {
		return "안녕하세요";
	}

	@GetMapping("/all/goods")
	public ApiResponse<GoodsListDto> getAllGoods(
		@RequestParam(required = false) Long cursorValue,                // 커서 필드 기준 값(조회수, 가격)
		@RequestParam(required = false) Long cursorId,                    // 마지막 조회 항목 ID
		@RequestParam(required = false) LocalDateTime createdAt,        // 최신순일 경우, 커서 기준 값
		@RequestParam(required = false) List<Long> categoryIds,            // 카테고리 필터
		@RequestParam(required = false) String keyword,                    // 검색어
		@RequestParam(defaultValue = "popular") String sortBy            // 정렬 기준 (기본값: 최신순)
	) {
		log.debug("cursorValue : {}, cursorId : {}, createdAt : {}, categoryIds : {}, keyword : {}, sortBy : {}",
			cursorValue, cursorId, createdAt, categoryIds, keyword, sortBy);

		// 허용된 sortBy 값 체크
		List<String> allowedSortValues = List.of("popular", "recent", "priceHigh", "priceLow");
		if (sortBy != null && !allowedSortValues.contains(sortBy)) {
			throw new CustomException(ErrorCode.INVALID_SORT_BY);
		}
		// 필수 요청 값 검증
		if ("recent".equals(sortBy) && cursorId != null && createdAt == null) {
			throw new CustomException(ErrorCode.MISSING_CURSOR_CREATEDAT);
		}
		if (cursorId != null && cursorValue == null) {
			throw new CustomException(ErrorCode.MISSING_CURSOR_VALUE);
		}

		return ApiResponse.success(
			goodsService.getGoods(cursorValue, cursorId, createdAt, categoryIds, keyword, sortBy)
		);
	}

	@GetMapping("/all/goods/{goodsId}")
	public ApiResponse<GoodsDetailDto> getDetailGood(@PathVariable Long goodsId) {
		return ApiResponse.success(
			goodsService.getGoodDetail(goodsId)
		);
	}

	@GetMapping("/user/goods/my")
	public ApiResponse<List<GoodsDto>> getMyAllGoods() {
		return ApiResponse.success(goodsService.getMyGoods());
	}

	@PostMapping("/user/goods/register")
	public ApiResponse<Long> insertGood(@Valid @RequestBody GoodsRequestDto goodsRequestDto) {
		return ApiResponse.success(goodsService.insertGood(goodsRequestDto));
	}

	@PutMapping("/user/goods/{goodsId}")
	public ApiResponse<Void> updateGood(
		@PathVariable Long goodsId,
		@Valid @RequestBody GoodsRequestDto goodsRequestDto
	) {
		goodsService.updateGood(goodsId, goodsRequestDto);
		return ApiResponse.success();
	}

	@DeleteMapping("/user/goods/{goodsId}")
	public ApiResponse<Void> deleteGood(@PathVariable Long goodsId) {
		goodsService.deleteGood(goodsId);
		return ApiResponse.success();
	}

	// 물건 사진 API

	@PostMapping("/user/goods/{goodsId}/images")
	public ApiResponse<Void> uploadImages(
		@PathVariable Long goodsId,
		@RequestParam List<MultipartFile> images
	) {
		// 최대 이미지 개수를 초과하는지 검증
		if (images.size() > MAX_FILES) {
			throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
		}
		goodsService.uploadGoodImages(goodsId, images);
		return ApiResponse.success();
	}

	@DeleteMapping("/user/goods/{goodsId}/images/{imagesId}")
	public ApiResponse<Void> deleteImage(
		@PathVariable Long goodsId,
		@PathVariable Long imagesId
	) {
		goodsService.deleteGoodImage(goodsId, imagesId);
		return ApiResponse.success();
	}
}