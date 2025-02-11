package com.example.swapit.service;

import java.io.IOException;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png");

	private final S3Client s3Client;
	private final GoodsRepository goodsRepository;

	@Override
	public void uploadFiles(Long goodsId, List<MultipartFile> files) {
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
				String path = "goods/" + goodsId + "/";

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
					Goods good = goodsRepository.findById(goodsId)
						.orElseThrow(() -> new CustomException(ErrorCode.GOOD_NOT_FOUND));

					good.addImage(GoodsImages.builder()
						.fileName(uniqueFileName)
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