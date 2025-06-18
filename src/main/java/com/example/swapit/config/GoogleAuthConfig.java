package com.example.swapit.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Configuration
public class GoogleAuthConfig {
	@Value("${google.web-client-id}")
	private String googleWebClientId;

	@Bean
	public GoogleIdTokenVerifier googleIdTokenVerifier() {
		return new GoogleIdTokenVerifier.Builder(
			new NetHttpTransport(),
			GsonFactory.getDefaultInstance()
		)
			.setAudience(Collections.singletonList(googleWebClientId))
			.build();
	}
}
