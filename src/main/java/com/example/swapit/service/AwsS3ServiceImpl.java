package com.example.swapit.service;

import static com.google.common.io.Files.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	private final String baseUrl = "images/";
	private final S3Client s3Client;
	private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png"); // 이미지 가능 확장자
	private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png");

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
				String contentType = detectMimeType(file);
				String s3Key = uploadFileToS3(file, "goods/" + good.getId(), contentType);
				return Pair.of(s3Key, contentType);
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

		String contentType = detectMimeType(file);
		return uploadFileToS3(file, "users/" + user.getUsersId(), contentType);
	}

	/**
	 *  S3에 파일 업로드 후 S3 key 반환
	 */
	private String uploadFileToS3(MultipartFile file, String path, String contentType) {
		String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		String s3Key = path + "/" + uniqueFileName;

		try {
			PutObjectRequest putRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(baseUrl + s3Key)
				.contentType(contentType)
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
	@Override
	public void deleteFileFromS3(String s3Key) {
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

	/**
	 * MultipartFile에서 정확한 MIME 타입 감지하는 함수
	 */
	private String detectMimeType(MultipartFile file) {
		try {
			// Java에서 파일 MIME 타입을 직접 감지
			Path tempFile = Files.createTempFile("upload", file.getOriginalFilename());
			Files.copy(file.getInputStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			String mimeType = Files.probeContentType(tempFile);
			Files.delete(tempFile); // 임시 파일 삭제
			log.debug("[이미지 업로드 감지] 이미지 확장자 확인 : {}", mimeType);

			if (mimeType == null || mimeType.equals("application/octet-stream")) {
				// MIME 타입을 감지하지 못하면 확장자로 결정
				String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

				return switch (fileExtension) {
					case "jpg", "jpeg" -> "image/jpeg";
					case "png" -> "image/png";
					default -> throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
				};
			}

			if (ALLOWED_MIME_TYPES.contains(mimeType)) {
				return mimeType; // 허용된 이미지 포맷이면 그대로 반환
			}

			// 이미지이지만 허용되지 않은 경우 예외 발생
			if (mimeType.startsWith("image/")) {
				throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);
			}
			// 이미지가 아니면 예외 발생
			throw new CustomException(ErrorCode.INVALID_IMAGE_FORMAT);

		} catch (IOException e) {
			log.error("MIME 타입 감지 실패: {}", e.getMessage());
			throw new CustomException(ErrorCode.IMAGE_READ_FAILED);
		}
	}

	/**
	 * S3에서 한 번에 여러 이미지 삭제
	 */
	@Override
	public void deleteFilesFromS3(List<GoodsImages> images) {
		List<ObjectIdentifier> objects = images.stream()
			.map(image -> ObjectIdentifier.builder()
				.key(baseUrl + image.getS3Key())
				.build())
			.collect(Collectors.toList());

		DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
			.bucket(bucketName)
			.delete(Delete.builder().objects(objects).build())
			.build();

		try {
			s3Client.deleteObjects(deleteObjectsRequest);
			log.info("S3에서 {}개의 이미지가 성공적으로 삭제되었습니다.", objects.size());
		} catch (S3Exception e) {
			log.error("S3 이미지 삭제 중 오류 발생", e);
		}
	}
}