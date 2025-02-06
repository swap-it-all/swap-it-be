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

		Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(60 * 60 * 24 * 14));

		if (existingTokenOpt.isPresent()) {
			Tokens existingToken = existingTokenOpt.get();
			existingToken.setRefreshToken(refreshToken);
			existingToken.setExpiresAt(expiresAt);
			repository.save(existingToken);
		} else {
			repository.save(new Tokens(user, refreshToken, expiresAt));
		}
	}

	public void updateRefreshToken(Users user, String newRefreshToken) {
		repository.findByUserEmail(user.getEmail())
			.ifPresent(repository::delete);
		saveRefreshToken(user, newRefreshToken);
	}

	public boolean validateRefreshToken(String email, String refreshToken) {
		return repository.findByUserEmail(email)
			.filter(token -> token.getRefreshToken().equals(refreshToken))
			.isPresent();
	}

}
