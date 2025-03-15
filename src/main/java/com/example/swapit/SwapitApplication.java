package com.example.swapit;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableJpaAuditing
public class SwapitApplication {

	@PostConstruct
	public void init() { // JVM 타임존 설정
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		log.info("JVM TimeZone Set to: {}", TimeZone.getDefault().getID());
	}

	public static void main(String[] args) {
		SpringApplication.run(SwapitApplication.class, args);
	}

}
