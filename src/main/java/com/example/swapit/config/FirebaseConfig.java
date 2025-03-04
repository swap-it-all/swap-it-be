package com.example.swapit.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

	@Bean
	@Profile("dev")
	public FirebaseApp firebaseAppDev(@Value("${firebase.config-path}") String configPath) throws IOException {
		InputStream serviceAccount = new ClassPathResource(configPath).getInputStream();

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(serviceAccount))
			.build();

		return initializeFirebaseApp(options);
	}

	@Bean
	@Profile("prd")
	public FirebaseApp firebaseAppProd(@Value("${firebase.config-env}") String firebaseConfig) throws IOException {
		InputStream serviceAccount = new ByteArrayInputStream(firebaseConfig.getBytes());

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(GoogleCredentials.fromStream(serviceAccount))
			.build();

		return initializeFirebaseApp(options);
	}

	/**
	 * FirebaseApp이 중복 초기화되지 않도록 관리
	 */
	private FirebaseApp initializeFirebaseApp(FirebaseOptions options) {
		List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
		if (firebaseApps.isEmpty()) {
			return FirebaseApp.initializeApp(options);
		} else {
			return firebaseApps.get(0);
		}
	}
}