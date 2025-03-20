package com.example.swapit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.WithdrawReasons;

public interface WithdrawReasonsRepository extends JpaRepository<WithdrawReasons, Long> {
}
