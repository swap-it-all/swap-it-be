package com.example.swapit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Trades;

public interface TradesRepository extends JpaRepository<Trades, Long> {
}
