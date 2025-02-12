package com.example.swapit.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.repository.GoodsRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png"); // 이미지 가능 확장자
	private static final int MAX_IMAGES = 10; // 이미지 최대 개수
	private static final int IMAGE_SHOW_TIME_LIMIT = 10; // 이미지 링크 유지 시간 (10분)

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final GoodsRepository goodsRepository;

	/**
	 * 이미지 업로드
	 */
	@Override
	public void uploadFiles(Long goodsId, List<MultipartFile> files) {
		Goods good = goodsRepository.findById(goodsId)
			.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

		// 최대 이미지 개수를 초과하는지 검증
		if (good.getGoodsImagesList().size() + files.size() > MAX_IMAGES) {
			throw new CustomException(ErrorCode.IMAGE_COUNT_EXCEEDED);
		}

		try {
			for (MultipartFile file : files) {
				// 1. 확장자 검증 (이미지 파일만 허용)
				if (!isValidImageFile(file.getOriginalFilename())) {
					throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
				}

				// 2. 파일 정보 생성
				String originalFileName = file.getOriginalFilename();
				String contentType = file.getContentType();
				String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
				String path = "images/goods/" + goodsId + "/";

				// 3. S3 업로드 요청 생성
				PutObjectRequest putRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key(path + uniqueFileName)
					.contentType(contentType)
					.contentLength(file.getSize())
					.build();

				// 4. S3로 파일 업로드 실행 -> 응답 response
				PutObjectResponse response = s3Client.putObject(
					putRequest, RequestBody.fromBytes(file.getBytes()));

				// 5. S3에 업로드 성공 시, 물건 DB에 업데이트
				if (response.sdkHttpResponse().isSuccessful()) {
					good.addImage(GoodsImages.builder()
						.good(good)
						.s3Key(path + uniqueFileName)
						.contentType(contentType)
						.build());
				} else {
					throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
				}
			}
		} catch (IOException e) {
			log.error("good ID {} 의 {}", goodsId, ErrorCode.IMAGE_READ_FAILED.getMessage() + " /" + e.getMessage());
			throw new CustomException(ErrorCode.IMAGE_READ_FAILED);
		} catch (S3Exception e) {
			log.error("AWS S3 통신 에러 발생: {}", e.getMessage());
			throw new CustomException(ErrorCode.S3_NETWORK_FAILED);
		} catch (IllegalStateException e) {
			log.error("AWS S3 업로드 실패: {}", e.getMessage());
			throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
		}
	}

	/**
	 * 단일 이미지 조회
	 *  todo : 버킷 이름이 보여서, CloudFront 도입
	 */
	@Override
	public String generatePreSignedImageUrl(String objectKey) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
			.bucket(bucketName)
			.key(objectKey)
			.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofMinutes(IMAGE_SHOW_TIME_LIMIT))
			.getObjectRequest(getObjectRequest)
			.build();

		// todo : 앱 배포 시, 앱에서만 사용하도록 CustomHeader 추가.
		PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
		return presignedRequest.url().toString();
	}

	@Override
	public void deleteFile(Long goodsId, Long imagesId) {

	}

	@Override
	public void updateUserProfileImage(MultipartFile file) {

	}

	private boolean isValidImageFile(String originalFileName) {
		if (originalFileName == null || originalFileName.isEmpty()) {
			return false;
		}

		// 파일 확장자 추출 (소문자로 변환)
		String fileExtension = originalFileName.substring(
			originalFileName.lastIndexOf(".") + 1).toLowerCase();

		return ALLOWED_EXTENSIONS.contains(fileExtension);
	}
}