package com.example.swapit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.swapit.domain.Tokens;
import com.example.swapit.domain.Users;

public interface TokensRepository extends JpaRepository<Tokens, Long> {
	Optional<Tokens> findByUserUsersId(Long usersId);

	Optional<Tokens> findByRefreshToken(String refreshToken);

	@Modifying
	void deleteByUser(Users users);
}
