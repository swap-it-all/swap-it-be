package com.example.swapit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Tokens;

public interface TokensRepository extends JpaRepository<Tokens, Long> {
	Optional<Tokens> findByUserEmail(String email);

	Optional<Tokens> findByRefreshToken(String refreshToken);
}
