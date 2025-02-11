package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Categories;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.GoodsQuality;
import com.example.swapit.domain.Users;
import com.example.swapit.repository.GoodsRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
class AwsS3ServiceTest {

	@InjectMocks
	private AwsS3ServiceImpl awsS3Service;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private S3Client s3Client;

	@Mock
	private MultipartFile mockFile;

	private static final String BUCKET_NAME = "test-bucket";
	private static final Long goodsId = 1L;
	private static final String VALID_FILE_NAME = "test-image.jpg";
	private static final String INVALID_FILE_NAME = "test-document.pdf";
	private static final String CONTENT_TYPE = "image/jpeg";
	private static final long FILE_SIZE = 1024 * 1024; // 1MB
	private static final long EXCEEDED_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	private Users testUser;
	private Categories testCategory;
	private Goods testGood;

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

		// ReflectionTestUtils.setField(testGood, "id", 1L);
		when(goodsRepository.findById(anyLong())).thenReturn(Optional.of(testGood));
	}

	@Test
	@DisplayName("파일 업로드 성공")
	void uploadFiles_Success() throws IOException {
		// given
		when(mockFile.getOriginalFilename()).thenReturn(VALID_FILE_NAME);
		when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
		when(mockFile.getSize()).thenReturn(FILE_SIZE);
		when(mockFile.getBytes()).thenReturn(new byte[(int)FILE_SIZE]);

		// Mock S3 응답
		PutObjectResponse mockResponse = (PutObjectResponse)PutObjectResponse.builder()
			.sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
			.build();
		when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(mockResponse);

		// when: 파일 업로드 실행
		awsS3Service.uploadFiles(goodsId, List.of(mockFile));

		// then: S3 업로드 및 DB 저장 검증
		verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
		verify(goodsRepository, times(1)).findById(goodsId);
	}

	@Test
	@DisplayName("잘못된 확장자 파일 업로드")
	void uploadFiles_Fail_ByInvalidFileFormat() {
		// given
		when(mockFile.getOriginalFilename()).thenReturn(INVALID_FILE_NAME);

		// when & then
		CustomException exception = assertThrows(CustomException.class, () ->
			awsS3Service.uploadFiles(goodsId, List.of(mockFile))
		);

		assertEquals(ErrorCode.INVALID_IMAGE_FORMAT, exception.getErrorCode());
	}

	@Test
	@DisplayName("S3 파일 업로드 실패")
	void uploadFiles_Fail_S3UploadFailure() throws IOException {
		// given
		when(mockFile.getOriginalFilename()).thenReturn(VALID_FILE_NAME);
		when(mockFile.getBytes()).thenReturn(new byte[(int)FILE_SIZE]);

		// when
		when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
			.thenThrow(S3Exception.builder().message("S3 Upload Failed").build());

		// then
		CustomException exception = assertThrows(CustomException.class, () ->
			awsS3Service.uploadFiles(goodsId, List.of(mockFile))
		);

		assertEquals(ErrorCode.S3_NETWORK_FAILED, exception.getErrorCode());
	}

	@Test
	@DisplayName("10개 초과의 이미지 업로드 시도")
	void testUploadFiles_ImageCountExceeded_ShouldThrowException() {
		// given
		for (int i = 0; i < 8; i++) {
			testGood.addImage(
				GoodsImages.builder().fileName("fileName").contentType("content-type").build()
			);
		}

		List<MultipartFile> newFiles = List.of(mockFile, mockFile, mockFile); // 3개 업로드

		// when & then: 10개 초과 시 예외 발생 검증
		CustomException exception = assertThrows(CustomException.class, () ->
			awsS3Service.uploadFiles(goodsId, newFiles)
		);

		assertEquals(ErrorCode.IMAGE_COUNT_EXCEEDED, exception.getErrorCode());
	}
}