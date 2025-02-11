package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsDto;
import com.example.swapit.domain.dto.GoodsListDto;
import com.example.swapit.domain.dto.GoodsRequestDto;
import com.example.swapit.repository.CategoriesRepository;
import com.example.swapit.repository.GoodsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

	private final GoodsRepository goodsRepository;
	private final CategoriesRepository categoriesRepository;
	private final CurrentUserService currentUserService;

	private final int size = 30;

	@Override
	public GoodsListDto getGoods(
		Long cursorValue, Long cursorId, LocalDateTime createdAt, List<Long> categoryIds, String keyword, String sortBy
	) {
		// Goods 조회
		List<Goods> goodsList = goodsRepository.findGoodsByCursor(
			cursorValue, cursorId, createdAt, categoryIds, keyword, sortBy, size);

		boolean hasNext = goodsList.size() > size;

		// 요청한 크기만큼 데이터 제한
		List<Goods> paginateGoods = hasNext ? goodsList.subList(0, size) : goodsList;

		List<GoodsDto> goodsDtoList = paginateGoods.stream()
			.map(GoodsDto::of)
			.toList();

		Long lastCursorId = paginateGoods.isEmpty() ? null : paginateGoods.get(paginateGoods.size() - 1).getId();

		return new GoodsListDto(goodsDtoList, hasNext, lastCursorId, goodsDtoList.size());
	}

	@Override
	public List<GoodsDto> getMyGoods() {
		Users user = currentUserService.getCurrentUser();
		log.debug("사용자 ID ({}) 가 내 물건 목록 조회.", user.getUsersId());

		List<Goods> findGoods = goodsRepository.findByUserOrderByCreatedAtDesc(user);
		return findGoods.stream().map(GoodsDto::of).toList();
	}

	@Override
	public GoodsDetailDto getGoodDetail(Long goodsId) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		// 물건의 viewCount 증가 (새로고침할 때 viewCount가 무한히 증가됨. -> 이 부분은 redis로 ip 제한 걸어서 30초 이내로 다시 요청할 때 변경 가능함.)
		good.incrementViewCount();

		return GoodsDetailDto.of(good);
	}

	@Override
	public void insertGood(GoodsRequestDto goodsRequestDto) {
		Users user = currentUserService.getCurrentUser();
		log.debug("사용자 ID ({}) 가 Goods Title ({}) 생성 시도.", user.getUsersId(), goodsRequestDto.getTitle());

		Categories category = categoriesRepository.findById(goodsRequestDto.getCategoryId())
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

		goodsRepository.save(goodsRequestDto.toEntity(user, category));
	}

	@Override
	public void updateGood(Long goodsId, GoodsRequestDto goodsRequestDto) {
		Users currentUser = currentUserService.getCurrentUser();
		log.debug("사용자 ID ({}) 가 Goods ID ({}) 수정 시도.", currentUser.getUsersId(), goodsId);

		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		checkAuthorization(good.getUser(), currentUser);

		Categories category = categoriesRepository.findById(goodsRequestDto.getCategoryId())
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

		good.update(goodsRequestDto, category);
	}

	@Override
	public void deleteGood(Long goodsId) {
		Users currentUser = currentUserService.getCurrentUser();
		log.debug("사용자 ID ({}) 가 Goods ID ({}) 삭제 시도.", currentUser.getUsersId(), goodsId);

		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		checkAuthorization(good.getUser(), currentUser);

		goodsRepository.delete(good);
	}

	private void checkAuthorization(Users goodsUser, Users currentUser) {
		log.info("(글 소유주 ID: {}), (로그인한 사용자 ID: {})", goodsUser.getUsersId(), currentUser.getUsersId());
		if (!goodsUser.getUsersId().equals(currentUser.getUsersId())) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
	}
}