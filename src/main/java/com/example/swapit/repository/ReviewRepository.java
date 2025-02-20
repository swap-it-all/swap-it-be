package com.example.swapit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Reviews;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
	boolean existsByTradeAndWriter(Trades trade, Users writer);

	List<Reviews> findAllByRevieweeOrderByCreatedAtDesc(Users reviewee);
}