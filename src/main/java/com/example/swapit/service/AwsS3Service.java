package com.example.swapit.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface AwsS3Service {
	void uploadFiles(Long goodsId, List<MultipartFile> files);

	String generatePreSignedImageUrl(String objectKey);

	void deleteFile(Long goodsId, Long imagesId);

	void updateUserProfileImage(MultipartFile file);
}