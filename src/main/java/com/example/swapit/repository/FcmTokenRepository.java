package com.example.swapit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.swapit.domain.FcmToken;
import com.example.swapit.domain.Users;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
	Optional<FcmToken> findByUser(Users user);

	@Modifying
	void deleteByUser(Users users);
}