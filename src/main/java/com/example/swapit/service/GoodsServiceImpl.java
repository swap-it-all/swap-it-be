package com.example.swapit.service;

import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsListDto;
import com.example.swapit.domain.dto.GoodsRequestDto;
import com.example.swapit.repository.CategoriesRepository;
import com.example.swapit.repository.GoodsRepository;
import com.example.swapit.repository.UsersRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

	private final GoodsRepository goodsRepository;
	private final UsersRepository usersRepository;
	private final CategoriesRepository categoriesRepository;

	@Override
	public GoodsListDto getGoods() {
		return null;
	}

	@Override
	public GoodsDetailDto getGoodDetail(Long goodsId) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		return GoodsDetailDto.of(good);
	}

	@Override
	public void insertGood(GoodsRequestDto goodsRequestDto) {
		Users user = findUser();
		Categories category = categoriesRepository.findById(goodsRequestDto.getCategoryId())
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

		goodsRepository.save(goodsRequestDto.toEntity(user, category));
	}

	@Override
	public void updateGood(Long goodsId, GoodsRequestDto goodsRequestDto) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Users currentUser = findUser();

		checkAuthorization(good.getUser(), currentUser);

		Categories category = categoriesRepository.findById(goodsRequestDto.getCategoryId())
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

		good.update(goodsRequestDto, category);
	}

	@Override
	public void deleteGood(Long goodsId) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));
		Users currentUser = findUser();

		checkAuthorization(good.getUser(), currentUser);

		goodsRepository.delete(good);
	}

	// todo: User 가져오는 부분 변경
	private Users findUser() {
		return usersRepository.findById(1L)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
	}

	private void checkAuthorization(Users goodsUser, Users currentUser) {
		log.info("(글 소유주 ID: {}), (로그인한 사용자 ID: {})", goodsUser.getUsersId(), currentUser.getUsersId());
		if (!goodsUser.getUsersId().equals(currentUser.getUsersId())) {
			throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
	}
}
