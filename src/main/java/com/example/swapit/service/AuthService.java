package com.example.swapit.service;

import static com.example.swapit.util.Constant.*;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.config.security.jwt.JwtService;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.repository.UsersRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtProvider jwtProvider;
	private final JwtService jwtService;
	private final UsersRepository usersRepository;

	/**
	 * Refresh Token을 검증하고 새로운 Access Token을 발급
	 */
	public ResponseEntity<?> refresh(String token) {
		String refreshToken = token.replace("Bearer ", "");
		String email = jwtProvider.getEmailFromToken(refreshToken);

		if (!jwtService.validateRefreshToken(email, refreshToken)) {
			return ResponseEntity.status(401).body(null); // Unauthorized
		}

		// 새 토큰 생성
		Users user = usersRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		TokenDTO newToken = jwtProvider.createToken(user.getEmail());
		jwtService.updateRefreshToken(user, newToken.getRefreshToken());

		return ResponseEntity.ok()
			.body(Map.of(
				"success", true,
				"message", "응답에 성공하였습니다.",
				"results", newToken
			));
	}

	/**
	 * Access Token을 이용하여 사용자 정보를 조회
	 */
	public ResponseEntity<?> getUserInfo(String token) {
		// "Bearer " 제거 후 실제 토큰만 추출
		String accessToken = token.replace("Bearer ", "");

		// 토큰 유효성 검사
		if (!jwtProvider.validateToken(accessToken)) {
			return ResponseEntity.status(401).build(); // Unauthorized
		}

		// 토큰에서 이메일 추출
		String email = jwtProvider.getEmailFromToken(accessToken);

		// 이메일로 사용자 조회
		Users user = usersRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		// 응답 데이터 생성
		UserResponseDTO response = UserResponseDTO.builder()
			.nickname(user.getNickname())
			.email(user.getEmail())
			.profileImgUrl(user.getProfileImageUrl())
			.provider(user.getProvider())
			.build();

		return ResponseEntity.ok()
			.body(Map.of(
				"success", true,
				"message", "응답에 성공하였습니다.",
				"results", response
			));
	}

	/**
	 * 카카오 로그인 처리 로직
	 */
	public ResponseEntity<?> kakaoLogin(String code) {
		// 1. 카카오 Access Token 요청
		String kakaoAccessToken = getKakaoAccessToken(code);
		if (kakaoAccessToken == null) {
			throw new IllegalStateException("Failed to retrieve Kakao Access Token");
		}

		// 2. 카카오 사용자 정보 가져오기
		Users user = getKakaoUserInfo(kakaoAccessToken);

		// 3. 자체 JWT 발급 및 Refresh Token 저장
		TokenDTO jwtToken = jwtProvider.createToken(user.getEmail());
		jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());

		return ResponseEntity.ok()
			.body(Map.of(
				"success", true,
				"message", "응답에 성공하였습니다.",
				"results", jwtToken
			));
	}

	/**
	 * 카카오 Access Token 요청
	 */
	private String getKakaoAccessToken(String code) {
		String tokenRequestUrl = "https://kauth.kakao.com/oauth/token";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		String body = "grant_type=authorization_code"
			+ "&client_id=" + KAKAO_CLIENT_ID
			+ "&redirect_uri=" + KAKAO_REDIRECT_URI
			+ "&code=" + code;

		HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> response = restTemplate.exchange(
			tokenRequestUrl, HttpMethod.POST, requestEntity, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(response.getBody());
			return responseJson.get("access_token").asText();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 카카오 사용자 정보 요청
	 */
	private Users getKakaoUserInfo(String accessToken) {
		String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> response = restTemplate.exchange(
			userInfoUrl, HttpMethod.POST, requestEntity, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(response.getBody());

			String email = responseJson.get("kakao_account").get("email").asText();
			String nickname = responseJson.get("properties").get("nickname").asText();
			String profileImageUrl = responseJson.get("properties").get("profile_image").asText();

			// DB에서 사용자 조회 or 저장
			return usersRepository.findByEmail(email)
				.orElseGet(() -> {
					Users newUser = Users.builder()
						.nickname(nickname)
						.email(email)
						.profileImageUrl(profileImageUrl)
						.provider("kakao")
						.build();
					return usersRepository.save(newUser);
				});

		} catch (Exception e) {
			return null;
		}
	}
}
