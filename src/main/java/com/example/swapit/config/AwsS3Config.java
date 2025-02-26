package com.example.swapit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

	@Bean
	@Profile("prd")
	public S3Client prdS3Client() {
		return S3Client.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(InstanceProfileCredentialsProvider.create())
			.build();
	}

	@Bean
	@Profile("dev")
	public S3Client devS3Client(
		@Value("${cloud.aws.credentials.access-key}") String accessKey,
		@Value("${cloud.aws.credentials.secret-key}") String secretKey
	) {
		return S3Client.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)
			))
			.build();
	}

	@Bean
	@Profile("test")
	public S3Client testS3Client(
		@Value("${cloud.aws.credentials.access-key}") String accessKey,
		@Value("${cloud.aws.credentials.secret-key}") String secretKey
	) {
		return S3Client.builder()
			.region(Region.AP_NORTHEAST_2)
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)
			))
			.build();
	}

	@Bean
	@Profile("prd")
	public S3Presigner prdS3Presigner() {
		return S3Presigner.builder()
			.region(Region.AP_NORTHEAST_2) // S3 리전 설정 (서울)
			.credentialsProvider(DefaultCredentialsProvider.create()) // IAM Role 또는 환경 변수 사용
			.build();
	}

	@Bean
	@Profile("dev")
	public S3Presigner devS3Presigner(
		@Value("${cloud.aws.credentials.access-key}") String accessKey,
		@Value("${cloud.aws.credentials.secret-key}") String secretKey
	) {
		return S3Presigner.builder()
			.region(Region.AP_NORTHEAST_2) // S3 리전 설정 (서울)
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)
			)) // IAM Role 또는 환경 변수 사용
			.build();
	}

	@Bean
	@Profile("test")
	public S3Presigner testS3Presigner(
		@Value("${cloud.aws.credentials.access-key}") String accessKey,
		@Value("${cloud.aws.credentials.secret-key}") String secretKey
	) {
		return S3Presigner.builder()
			.region(Region.AP_NORTHEAST_2) // S3 리전 설정 (서울)
			.credentialsProvider(StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey)
			)) // IAM Role 또는 환경 변수 사용
			.build();
	}
}