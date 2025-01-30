package com.example.swapit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.config.oauth.JwtProvider;
import com.example.swapit.config.oauth.JwtService;
import com.example.swapit.config.oauth.TokenDTO;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.RefreshTokenRequest;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {

	private final JwtProvider jwtProvider;
	private final JwtService jwtService;
	private final UsersRepository usersRepository;

	//http://localhost:8080/oauth2/authorization/google

	@PostMapping("/api/user/auth/refresh")
	public ResponseEntity<TokenDTO> refresh(@RequestBody RefreshTokenRequest request) {
		String email = jwtProvider.getEmailFromToken(request.getRefreshToken());

		if (!jwtService.validateRefreshToken(email, request.getRefreshToken())) {
			return ResponseEntity.status(401).body(null); // Unauthorized
		}

		// 새 토큰 생성
		Users user = usersRepository.findByEmail(email)
			.orElseThrow(() -> new IllegalArgumentException("User not found"));

		TokenDTO newToken = jwtProvider.createToken(user.getEmail());
		jwtService.updateRefreshToken(user, newToken.getRefreshToken());

		return ResponseEntity.ok(newToken);
	}

	@GetMapping("/api/user/auth/my")
	public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
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

		return ResponseEntity.ok(response);
	}
}
