package com.example.swapit.service;

import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;

public interface AwsS3Service {
	List<Pair<String, String>> uploadFiles(Goods good, List<MultipartFile> files);

	void deleteFile(GoodsImages image);

	void deleteFileFromS3(String s3Key);

	String updateUserProfileImage(Users user, MultipartFile file);

	void deleteFilesFromS3(List<GoodsImages> images);
}