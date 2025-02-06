package com.example.swapit.service;

import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;

public interface AuthService {
	TokenDTO refresh(String token);

	UserResponseDTO getUserInfo(String token);

	TokenDTO googleLogin(String googleAccessToken);

	TokenDTO kakaoLogin(String kakaoAccessToken);

	Users getGoogleUserInfo(String accessToken);

	Users getKakaoUserInfo(String accessToken);

	void deleteRefreshToken(String token);
}
