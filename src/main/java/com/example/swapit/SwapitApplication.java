package com.example.swapit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class SwapitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SwapitApplication.class, args);
	}

}
