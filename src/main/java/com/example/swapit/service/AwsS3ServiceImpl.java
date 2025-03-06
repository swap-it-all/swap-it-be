package com.example.swapit.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png"); // 이미지 가능 확장자
	private final String baseUrl = "images/";
	private final S3Client s3Client;

	/**
	 * 이미지 업로드
	 */
	@Override
	public List<Pair<String, String>> uploadFiles(Goods good, List<MultipartFile> files) {
		return files.stream()
			.map(file -> {
				if (!isValidImageFile(file.getOriginalFilename())) {
					throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
				}
				String s3Key = uploadFileToS3(file, "goods/" + good.getId());
				return Pair.of(s3Key, file.getContentType());
			}).toList();
	}

	@Override
	public void deleteFile(GoodsImages image) {
		// S3에서 이미지 삭제
		deleteFileFromS3(baseUrl + image.getS3Key());
	}

	@Override
	public String updateUserProfileImage(Users user, MultipartFile file) {
		isValidImageFile(file.getOriginalFilename());
		deleteFileFromS3(user.getProfileImageUrl()); // 기존 이미지 삭제
		return uploadFileToS3(file, "users/" + user.getUsersId());
	}

	/**
	 *  S3에 파일 업로드 후 S3 key 반환
	 */
	private String uploadFileToS3(MultipartFile file, String path) {
		String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		String s3Key = path + "/" + uniqueFileName;

		try {
			PutObjectRequest putRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(baseUrl + s3Key)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
		} catch (IOException e) {
			log.error("파일 변환 실패 : {}", ErrorCode.IMAGE_READ_FAILED.getMessage() + " /" + e.getMessage());
			throw new CustomException(ErrorCode.IMAGE_READ_FAILED);
		} catch (Exception e) {
			log.error("AWS S3 업로드 실패: {}", e.getMessage());
			throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
		}

		return s3Key;
	}

	/**
	 * S3에서 파일 삭제
	 */
	private void deleteFileFromS3(String s3Key) {
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(baseUrl + s3Key)
			.build();

		s3Client.deleteObject(deleteObjectRequest);
	}

	/**
	 *  이미지 파일 검증 (확장자 체크)
	 */
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