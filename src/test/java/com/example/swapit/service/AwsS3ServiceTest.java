package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;
import com.example.swapit.repository.GoodsRepository;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ExtendWith(MockitoExtension.class)
class AwsS3ServiceTest {

	@InjectMocks
	private AwsS3ServiceImpl awsS3Service;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private S3Presigner s3Presigner;

	@Mock
	private S3Client s3Client;

	@Mock
	private MultipartFile mockFile;

	private final String bucketName = "test-bucket";
	private final String s3Key = "images/goods/1/sample.jpg";
	private static final String preSignedUrl = "https://s3.test-bucket.com/sample.jpg";
	private static final String INVALID_FILE_NAME = "test-document.pdf";
	private static final Long goodsId = 1L;
	private static final String VALID_FILE_NAME = "test-image.jpg";
	private static final String CONTENT_TYPE = "image/jpeg";

	@Test
	@DisplayName("파일 업로드 성공")
	void uploadFiles_Success() throws IOException {
		// Given
		Goods mockGoods = mock(Goods.class);
		when(mockGoods.getId()).thenReturn(goodsId);
		when(mockFile.getOriginalFilename()).thenReturn(VALID_FILE_NAME);
		when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
		when(mockFile.getBytes()).thenReturn(new byte[10]);

		when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);

		// When
		List<Pair<String, String>> uploadedFiles = awsS3Service.uploadFiles(mockGoods, List.of(mockFile));

		// Then
		assertFalse(uploadedFiles.isEmpty());
		assertEquals(1, uploadedFiles.size());
		assertTrue(uploadedFiles.get(0).getFirst().contains("goods/1/"));
		assertEquals("image/jpeg", uploadedFiles.get(0).getSecond());

		verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
	}

	@Test
	@DisplayName("이미지 삭제 테스트 성공")
	void deleteFile_Success() {
		// Given
		GoodsImages mockImage = mock(GoodsImages.class);
		when(mockImage.getS3Key()).thenReturn(s3Key);

		when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);

		// When
		awsS3Service.deleteFile(mockImage);

		// Then
		verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
	}

	@Test
	@DisplayName("프로필 이미지 변경 테스트 성공")
	void updateUserProfileImage_Success() throws IOException {
		// given
		Users mockUser = mock(Users.class);
		when(mockUser.getUsersId()).thenReturn(1L);
		when(mockUser.getProfileImageUrl()).thenReturn(null);
		when(mockFile.getOriginalFilename()).thenReturn(VALID_FILE_NAME);
		when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
		when(mockFile.getBytes()).thenReturn(new byte[10]);

		when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(null);

		// When
		String uploadedS3Key = awsS3Service.updateUserProfileImage(mockUser, mockFile);

		// Then
		assertTrue(uploadedS3Key.contains("users/1/"));
		verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
	}

	@Test
	@DisplayName("잘못된 파일 확장자 업로드 시 예외 발생 테스트")
	void uploadFiles_ShouldThrowExceptionForInvalidExtension() {
		// Given
		Goods mockGoods = mock(Goods.class);
		when(mockFile.getOriginalFilename()).thenReturn(INVALID_FILE_NAME);

		// When & Then
		assertThrows(CustomException.class, () -> awsS3Service.uploadFiles(mockGoods, List.of(mockFile)));
	}

	@Test
	@DisplayName("S3 업로드 실패 시 예외 발생 테스트")
	void uploadFiles_ShouldThrowExceptionOnS3Failure() throws IOException {
		// Given
		Goods mockGoods = mock(Goods.class);
		when(mockGoods.getId()).thenReturn(1L);
		when(mockFile.getOriginalFilename()).thenReturn(VALID_FILE_NAME);
		when(mockFile.getContentType()).thenReturn(CONTENT_TYPE);
		when(mockFile.getBytes()).thenReturn(new byte[10]);

		doThrow(new RuntimeException("S3 upload failed"))
			.when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

		// When & Then
		assertThrows(CustomException.class, () -> awsS3Service.uploadFiles(mockGoods, List.of(mockFile)));
	}
}