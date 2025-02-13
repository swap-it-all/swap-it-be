package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.Users;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.GoodsRepository;

@ExtendWith(MockitoExtension.class)
class GoodsImagesServiceTest {

	@InjectMocks
	private GoodsServiceImpl goodsService;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private GoodsImagesRepository goodsImagesRepository;

	@Mock
	private AwsS3Service awsS3Service;

	private static final int MAX_IMAGES = 10;

	private Goods testGood;
	private Users testUser;
	private Categories testCategory;

	@BeforeEach
	void setUp() {
		testUser = Users.builder()
			.usersId(1L)
			.nickname("testUser")
			.profileImageUrl("/images/testUser")
			.email("test@gmail.com")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();

		testCategory = Categories.builder()
			.id(1L)
			.name("ELECTRONICS")
			.build();

		testGood = Goods.builder()
			.user(testUser)
			.title("test 물건")
			.price(1000L)
			.quality(GoodsQuality.NEW)
			.category(testCategory)
			.content("싸게 드려요! 교환주세요!")
			.build();
	}

	@Test
	@DisplayName("물건 사진 업로드 성공")
	void uploadGoodImages_Success() {
		// GIVEN
		List<MultipartFile> mockImages = List.of(
			new MockMultipartFile("file1", "image1.jpg", "image/jpeg", new byte[] {1, 2, 3}),
			new MockMultipartFile("file2", "image2.jpg", "image/jpeg", new byte[] {4, 5, 6})
		);

		List<Pair<String, String>> uploadedImages = List.of(
			Pair.of("s3-key-1", "image/jpeg"),
			Pair.of("s3-key-2", "image/jpeg")
		);

		when(goodsRepository.findById(1L)).thenReturn(Optional.of(testGood));
		when(awsS3Service.uploadFiles(any(Goods.class), anyList())).thenReturn(uploadedImages);

		// WHEN: 상품 이미지 업로드 수행
		goodsService.uploadGoodImages(1L, mockImages);

		// THEN: 업로드된 이미지가 저장되었는지 검증
		verify(goodsRepository, times(1)).findById(1L);
		verify(awsS3Service, times(1)).uploadFiles(testGood, mockImages);
		verify(goodsImagesRepository, times(2)).save(any(GoodsImages.class));
	}

	@Test
	@DisplayName("최대 이미지 개수를 초과하는 이미지 업로드 요청 실패")
	void uploadGoodImages_ThrowsException_WhenImageCountExceedsLimit() {
		// GIVEN: 기존에 MAX_IMAGES 개수만큼 존재하는 상품 이미지 리스트
		List<GoodsImages> existingImages = new ArrayList<>();
		for (int i = 0; i < MAX_IMAGES; i++) {
			existingImages.add(
				GoodsImages.builder()
					.good(testGood)
					.s3Key("s3-key-" + i)
					.contentType("content-type")
					.build());
		}

		// Mock 객체를 사용하여 `getId()` 값을 설정
		Goods spyGood = spy(testGood); // `testGood`을 spy로 감싸서 ID를 설정 가능하도록 만듦
		when(spyGood.getId()).thenReturn(1L); // getId()가 1L을 반환하도록 설정

		// Mock 설정: 상품에 대한 이미지 리스트 반환
		when(goodsImagesRepository.findByGood(spyGood)).thenReturn(existingImages);
		when(goodsRepository.findById(anyLong())).thenReturn(Optional.of(spyGood)); // 모든 ID 값 허용

		// 새로운 이미지 추가 요청
		List<MultipartFile> newImages = List.of(
			new MockMultipartFile("file1", "image1.jpg", "image/jpeg", new byte[] {1, 2, 3})
		);

		// WHEN & THEN: 이미지 개수 초과 시 예외 발생
		assertThrows(CustomException.class, () -> goodsService.uploadGoodImages(spyGood.getId(), newImages));

		// 검증: Stub이 올바르게 호출되었는지 확인
		verify(goodsRepository, times(1)).findById(spyGood.getId());
		verify(goodsImagesRepository, times(1)).findByGood(spyGood);
	}

	@Test
	@DisplayName("물건 이미지 삭제 성공")
	void deleteGoodImage_Success() {
		// GIVEN: 존재하는 상품과 이미지
		Goods spyGood = spy(testGood); // `testGood`을 spy로 감싸서 ID를 설정 가능하게 만듦
		when(spyGood.getId()).thenReturn(1L); // ID가 1L을 반환하도록 설정

		GoodsImages mockImage = GoodsImages.builder()
			.good(spyGood)
			.s3Key("s3-key")
			.contentType("content-type")
			.build();

		when(goodsImagesRepository.findById(anyLong())).thenReturn(Optional.of(mockImage));

		// WHEN: 상품 이미지 삭제 수행
		goodsService.deleteGoodImage(1L, 10L);

		// THEN: S3에서 삭제하고, DB에서 제거되었는지 검증
		verify(awsS3Service, times(1)).deleteFile(mockImage);
		verify(goodsImagesRepository, times(1)).delete(mockImage);
	}

	@Test
	@DisplayName("존재하지 않는 물건 이미지 삭제 실패")
	void deleteGoodImage_ThrowsException_WhenImageNotFound() {
		// GIVEN: 존재하지 않는 이미지
		when(goodsImagesRepository.findById(999L)).thenReturn(Optional.empty());

		// WHEN & THEN: 이미지가 없으면 예외 발생
		assertThrows(CustomException.class, () -> goodsService.deleteGoodImage(1L, 999L));
	}

	@Test
	@DisplayName("다른 상품의 이미지 삭제 실패")
	void deleteGoodImage_ThrowsException_WhenImageDoesNotBelongToGoods() {
		// GIVEN: 다른 상품의 이미지
		Goods testGood2 = Goods.builder()
			.user(testUser)
			.title("test 물건")
			.price(1000L)
			.quality(GoodsQuality.NEW)
			.category(testCategory)
			.content("싸게 드려요! 교환주세요!")
			.build();

		Goods spyGood2 = spy(testGood2); // testGood2를 spy로 감싸서 ID 설정 가능하게 만듦
		when(spyGood2.getId()).thenReturn(2L); // ID가 2L을 반환하도록 설정

		GoodsImages mockImage = GoodsImages.builder()
			.good(spyGood2)
			.s3Key("s3-key")
			.contentType("content-type")
			.build();

		when(goodsImagesRepository.findById(anyLong())).thenReturn(Optional.of(mockImage));

		// WHEN & THEN: 이미지가 다른 상품에 속하면 예외 발생
		assertThrows(CustomException.class, () -> goodsService.deleteGoodImage(1L, 10L));
	}
}