package com.example.swapit.service;

import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;

public interface AuthService {
	TokenDTO refresh(String token);

	UserResponseDTO getUserInfo(String token);

	TokenDTO googleLogin(String idTokenString);

	TokenDTO kakaoLogin(String kakaoAccessToken);

	Users getGoogleUserInfo(String idTokenString);

	Users getKakaoUserInfo(String accessToken);

	void deleteRefreshToken(String token);

	void withdrawGoogleUser(String googleToken, String reason);

	void withdrawKakaoUser(String kakaoToken, String reason);
}
