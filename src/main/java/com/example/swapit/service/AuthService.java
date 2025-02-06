package com.example.swapit.service;

import java.util.Map;
import java.util.Optional;

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
import com.example.swapit.repository.TokensRepository;
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
	private final TokensRepository tokensRepository;

	/**
	 * Refresh Token을 검증하고 새로운 Access Token을 발급
	 */
	public Map<String, Object> refresh(String token) {
		try {
			String refreshToken = token.replace("Bearer ", "");
			String email = jwtProvider.getEmailFromRefreshToken(refreshToken);

			// 토큰 검증 실패 시 명시적 응답
			if (!jwtService.validateRefreshToken(email, refreshToken)) {
				return Map.of(
					"success", false,
					"message", "유효하지 않은 리프레시 토큰입니다."
				);
			}

			// 사용자 조회 및 존재하지 않을 경우 예외 발생
			Optional<Users> userOptional = usersRepository.findByEmail(email);
			if (userOptional.isEmpty()) {
				return Map.of(
					"success", false,
					"message", "사용자를 찾을 수 없습니다."
				);
			}
			Users user = userOptional.get();

			// 새로운 토큰 생성 및 업데이트
			TokenDTO newToken = jwtProvider.createToken(email);
			jwtService.updateRefreshToken(user, newToken.getRefreshToken());

			// 성공 응답
			return Map.of(
				"success", true,
				"message", "새로운 토큰이 발급되었습니다.",
				"results", newToken
			);

		} catch (Exception e) {
			// 기타 예외 처리
			return Map.of(
				"success", false,
				"message", "토큰 갱신 중 오류 발생: " + e.getMessage()
			);
		}
	}

	/**
	 * Access Token을 이용하여 사용자 정보를 조회
	 */
	public Map<String, Object> getUserInfo(String token) {
		try {
			// "Bearer " 제거 후 실제 토큰만 추출
			String accessToken = token.replace("Bearer ", "");

			// 토큰 유효성 검사
			if (!jwtProvider.validateToken(accessToken)) {
				return Map.of(
					"success", false,
					"message", "유효하지 않은 토큰입니다."
				);
			}

			// 토큰에서 이메일 추출
			String email = jwtProvider.getEmailFromToken(accessToken);

			// 이메일로 사용자 조회
			Users user = usersRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

			// 응답 데이터 생성
			UserResponseDTO response = UserResponseDTO.builder()
				.nickname(user.getNickname())
				.email(user.getEmail())
				.profileImgUrl(user.getProfileImageUrl())
				.loginInfo(user.getLoginInfo())
				.build();

			return Map.of(
				"success", true,
				"message", "사용자 정보 조회를 성공하였습니다.",
				"results", response
			);
		} catch (Exception e) {
			return Map.of(
				"success", false,
				"message", "사용자 정보 조회 실패: " + e.getMessage()
			);
		}
	}

	/**
	 * 구글 로그인 처리 로직
	 */
	public Map<String, Object> googleLogin(String googleAccessToken) {
		try {
			// 구글 사용자 정보 가져오기
			Users user = getGoogleUserInfo(googleAccessToken);

			// 자체 JWT 발급 및 Refresh Token 저장
			TokenDTO jwtToken = jwtProvider.createToken(user.getEmail());
			jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());

			// 성공 응답 반환
			return Map.of(
				"success", true,
				"message", "구글 로그인에 성공하였습니다.",
				"results", jwtToken
			);

		} catch (Exception e) {
			return Map.of(
				"success", false,
				"message", "구글 로그인 실패: " + e.getMessage()
			);
		}
	}

	/**
	 * 구글 사용자 정보 요청
	 */
	public Users getGoogleUserInfo(String accessToken) {
		String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", accessToken);

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				userInfoUrl, HttpMethod.GET, requestEntity, String.class);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(response.getBody());

			// 필수 필드가 존재하는지 체크
			if (responseJson.has("email")) {
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
			} else {
				throw new RuntimeException("구글 사용자 정보에 이메일이 없습니다.");
			}
		} catch (Exception e) {
			throw new RuntimeException("구글 사용자 정보를 가져오는 중 오류 발생", e);
		}
	}

	/**
	 * 카카오 로그인 처리 로직
	 */
	public Map<String, Object> kakaoLogin(String kakaoAccessToken) {

		try {
			// 카카오 사용자 정보 가져오기
			Users user = getKakaoUserInfo(kakaoAccessToken);

			// 자체 JWT 발급 및 Refresh Token 저장
			TokenDTO jwtToken = jwtProvider.createToken(user.getEmail());
			jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());

			return Map.of(
				"success", true,
				"message", "응답에 성공하였습니다.",
				"results", jwtToken
			);
		} catch (Exception e) {
			return Map.of(
				"success", false,
				"message", "카카오 로그인 실패: " + e.getMessage()
			);
		}
	}

	/**
	 * 카카오 사용자 정보 요청
	 */
	private Users getKakaoUserInfo(String accessToken) {
		String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		RestTemplate restTemplate = new RestTemplate();

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				userInfoUrl, HttpMethod.GET, requestEntity, String.class);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode responseJson = objectMapper.readTree(response.getBody());

			if (responseJson.has("kakao_account") && responseJson.get("kakao_account").has("email")) {
				String email = responseJson.get("kakao_account").get("email").asText();
				String nickname = responseJson.get("properties").get("nickname").asText();
				String profileImageUrl = responseJson.get("properties").get("profile_image").asText();
				String role = "ROLE_USER";

				// DB에서 사용자 조회 or 저장
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
			} else {
				throw new IllegalArgumentException("카카오 사용자 정보에 이메일이 없습니다.");
			}

		} catch (Exception e) {
			throw new RuntimeException("카카오 사용자 정보를 가져오는 중 오류 발생", e);
		}
	}

	public void deleteRefreshToken(String token) {
		String refreshToken = token.replace("Bearer ", "");

		tokensRepository.findByRefreshToken(refreshToken)
			.ifPresent(tokensRepository::delete);
	}
}
