package com.example.swapit.config;

import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.google.firebase.FirebaseApp;

@TestConfiguration
public class TestFirebaseConfig {

	@Bean
	@Primary
	public FirebaseApp firebaseAppMock() {
		return mock(FirebaseApp.class);
	}
}