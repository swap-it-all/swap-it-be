package com.example.swapit.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.GoodsDetailDto;
import com.example.swapit.domain.dto.GoodsDto;
import com.example.swapit.domain.dto.GoodsImageDto;
import com.example.swapit.domain.dto.GoodsListDto;
import com.example.swapit.domain.dto.GoodsRequestDto;
import com.example.swapit.repository.CategoriesRepository;
import com.example.swapit.repository.GoodsImagesRepository;
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
	private final GoodsImagesRepository goodsImagesRepository;
	private final AwsS3Service awsS3Service;

	private static final int size = 30;
	private static final int MAX_IMAGES = 10; // 이미지 최대 개수

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

		// todo : 트래픽이 많아지면 Goods 엔티티에 "대표 이미지" 필드 추가
		List<GoodsDto> goodsDtoList = paginateGoods.stream()
			.map(good -> {
				String firstImageUrl = goodsImagesRepository.findFirstByGoodOrderByIdAsc(good)
					.map(GoodsImages::getS3Key)
					.map(awsS3Service::generatePreSignedImageUrl)
					.orElse(null);
				return GoodsDto.of(good, firstImageUrl);
			}).toList();

		Long lastCursorId = paginateGoods.isEmpty() ? null : paginateGoods.get(paginateGoods.size() - 1).getId();

		return new GoodsListDto(goodsDtoList, hasNext, lastCursorId, goodsDtoList.size());
	}

	@Override
	public List<GoodsDto> getMyGoods() {
		Users user = currentUserService.getCurrentUser();
		log.debug("사용자 ID ({}) 가 내 물건 목록 조회.", user.getUsersId());

		List<Goods> findGoods = goodsRepository.findByUserOrderByCreatedAtDesc(user);

		// todo : 트래픽이 많아지면 Goods 엔티티에 "대표 이미지" 필드 추가
		return findGoods.stream()
			.map(good -> {
				String firstImageUrl = goodsImagesRepository.findFirstByGoodOrderByIdAsc(good)
					.map(GoodsImages::getS3Key)
					.map(awsS3Service::generatePreSignedImageUrl)
					.orElse(null);
				return GoodsDto.of(good, firstImageUrl);
			}).toList();
	}

	@Override
	public GoodsDetailDto getGoodDetail(Long goodsId) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		// todo :  redis로 ip 제한 걸어서 30초 이내로 다시 요청할 때 변경 가능하도록 하능하게 하면 비기능 향상.
		// 물건의 viewCount 증가 (새로고침할 때 viewCount가 무한히 증가됨. -> 이 부분은.)
		good.incrementViewCount();

		// 물건 이미지 리스트 조회
		List<GoodsImageDto> photos = goodsImagesRepository.findByGood(good)
			.stream()
			.map(image -> new GoodsImageDto(image.getId(), awsS3Service.generatePreSignedImageUrl(image.getS3Key())))
			.toList();

		return GoodsDetailDto.of(good, photos);
	}

	@Override
	public Long insertGood(GoodsRequestDto goodsRequestDto) {
		Users user = currentUserService.getCurrentUser();
		log.debug("사용자 ID ({}) 가 Goods Title ({}) 생성 시도.", user.getUsersId(), goodsRequestDto.getTitle());

		Categories category = categoriesRepository.findById(goodsRequestDto.getCategoryId())
			.orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

		Goods good = goodsRequestDto.toEntity(user, category);
		goodsRepository.save(good);
		return good.getId();
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

	@Override
	public void uploadGoodImages(Long goodsId, List<MultipartFile> images) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		// 최대 이미지 개수를 초과하는지 검증
		if (good.getGoodsImagesList().size() + images.size() > MAX_IMAGES) {
			throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
		}

		// S3에 이미지 업로드
		List<Pair<String, String>> uploadImages = awsS3Service.uploadFiles(good, images);

		// DB에 이미지 업로드
		for (Pair<String, String> image : uploadImages) {
			goodsImagesRepository.save(
				GoodsImages.builder()
					.good(good)
					.s3Key(image.getFirst())
					.contentType(image.getSecond())
					.build());
		}
	}

	@Override
	public void deleteGoodImage(Long goodsId, Long imagesId) {
		// DB에서 이미지 조회
		GoodsImages image = goodsImagesRepository.findById(imagesId)
			.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

		// 이미지가 해당 상품에 속하는지 검증
		if (!image.getGood().getId().equals(goodsId)) {
			throw new CustomException(ErrorCode.IMAGE_NOT_FOUND);
		}

		// S3에서 이미지 삭제
		awsS3Service.deleteFile(image);

		// DB에서 이미지 삭제
		goodsImagesRepository.delete(image);
	}
}