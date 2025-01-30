package com.example.swapit.config.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
	private String accessToken;
	private String refreshToken;
	private String key;
}
