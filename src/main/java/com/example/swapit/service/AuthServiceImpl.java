package com.example.swapit.service;

import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.config.security.jwt.JwtService;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.repository.TokensRepository;
import com.example.swapit.repository.UsersRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final JwtProvider jwtProvider;
	private final JwtService jwtService;
	private final UsersRepository usersRepository;
	private final TokensRepository tokensRepository;
	private final RestTemplate restTemplate;

	@Override
	public TokenDTO refresh(String token) {
		try {
			String refreshToken = token.replace("Bearer ", "");
			String email = jwtProvider.getEmailFromRefreshToken(refreshToken);

			if (!jwtService.validateRefreshToken(email, refreshToken)) {
				throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
			}

			Optional<Users> userOptional = usersRepository.findByEmail(email);
			if (userOptional.isEmpty()) {
				throw new CustomException(ErrorCode.USER_NOT_FOUND);
			}

			TokenDTO newToken = jwtProvider.createToken(email);
			jwtService.updateRefreshToken(userOptional.get(), newToken.getRefreshToken());

			return newToken;
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.NEW_REFRESH_TOKEN_FAIL);
		}
	}

	@Override
	public UserResponseDTO getUserInfo(String token) {
		try {
			String accessToken = token.replace("Bearer ", "");

			if (!jwtProvider.validateToken(accessToken)) {
				throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
			}

			String email = jwtProvider.getEmailFromToken(accessToken);
			Users user = usersRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

			return UserResponseDTO.builder()
				.nickname(user.getNickname())
				.email(user.getEmail())
				.profileImgUrl(user.getProfileImageUrl())
				.loginInfo(user.getLoginInfo())
				.build();
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
		}
	}

	@Override
	public TokenDTO googleLogin(String googleAccessToken) {
		try {
			Users user = getGoogleUserInfo(googleAccessToken);
			TokenDTO jwtToken = jwtProvider.createToken(user.getEmail());
			jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());
			return jwtToken;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.LOGIN_FAIL);
		}
	}

	@Override
	public TokenDTO kakaoLogin(String kakaoAccessToken) {
		try {
			Users user = getKakaoUserInfo(kakaoAccessToken);
			TokenDTO jwtToken = jwtProvider.createToken(user.getEmail());
			jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());
			return jwtToken;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.LOGIN_FAIL);
		}
	}

	@Override
	public Users getGoogleUserInfo(String accessToken) {
		String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", accessToken);

		try {
			ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

			JsonNode responseJson = new ObjectMapper().readTree(response.getBody());
			String email = responseJson.get("email").asText();
			String nickname = responseJson.has("name") ? responseJson.get("name").asText() : "Google User";
			String profileImageUrl = responseJson.has("picture") ? responseJson.get("picture").asText() : "";
			String role = "ROLE_USER";

			return usersRepository.findByEmail(email)
				.orElseGet(() -> {
					Users newUser = Users.builder()
						.nickname(nickname)
						.email(email)
						.profileImageUrl(profileImageUrl)
						.loginInfo("google")
						.role(role)
						.build();
					return usersRepository.save(newUser);
				});
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
		}
	}

	@Override
	public Users getKakaoUserInfo(String accessToken) {
		// 카카오 사용자 정보 가져오는 로직 동일하게 구현
		String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		try {
			ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

			JsonNode responseJson = new ObjectMapper().readTree(response.getBody());
			String email = responseJson.get("kakao_account").get("email").asText();
			String nickname = responseJson.get("properties").get("nickname").asText();
			String profileImageUrl = responseJson.get("properties").get("profile_image").asText();
			String role = "ROLE_USER";

			return usersRepository.findByEmail(email)
				.orElseGet(() -> {
					Users newUser = Users.builder()
						.nickname(nickname)
						.email(email)
						.profileImageUrl(profileImageUrl)
						.loginInfo("kakao")
						.role(role)
						.build();
					return usersRepository.save(newUser);
				});
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
		}
	}

	@Override
	public void deleteRefreshToken(String token) {
		tokensRepository.findByRefreshToken(token.replace("Bearer ", ""))
			.ifPresent(tokensRepository::delete);
	}
}
