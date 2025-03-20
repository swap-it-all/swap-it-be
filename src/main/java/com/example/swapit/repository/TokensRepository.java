package com.example.swapit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Tokens;
import com.example.swapit.domain.Users;

public interface TokensRepository extends JpaRepository<Tokens, Long> {
	Optional<Tokens> findByUserEmail(String email);

	Optional<Tokens> findByRefreshToken(String refreshToken);

	void deleteByUser(Users users);
}
