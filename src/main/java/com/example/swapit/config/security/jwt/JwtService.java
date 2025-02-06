package com.example.swapit.config.security.jwt;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.swapit.domain.Tokens;
import com.example.swapit.domain.Users;
import com.example.swapit.repository.TokensRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final TokensRepository repository;

	public void saveRefreshToken(Users user, String refreshToken) {
		Optional<Tokens> existingTokenOpt = repository.findByUserEmail(user.getEmail());

		// 만료 시간 설정 (14일 후)
		Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(60 * 60 * 24 * 14));

		if (existingTokenOpt.isPresent()) {
			Tokens existingToken = existingTokenOpt.get();
			existingToken.setRefreshToken(refreshToken);
			existingToken.setExpiresAt(expiresAt);
			repository.save(existingToken);
		} else {
			// Tokens 객체 생성 후 저장
			repository.save(new Tokens(user, refreshToken, expiresAt));
		}
	}

	public void updateRefreshToken(Users user, String newRefreshToken) {
		repository.findByUserEmail(user.getEmail())
			.ifPresent(repository::delete); // 기존 토큰 삭제
		saveRefreshToken(user, newRefreshToken); // 새 토큰 저장
	}

	public boolean validateRefreshToken(String email, String refreshToken) {
		// Email을 통해 Refresh Token 조회
		return repository.findByUserEmail(email)
			.filter(token -> token.getRefreshToken().equals(refreshToken))
			.isPresent();
	}

}
