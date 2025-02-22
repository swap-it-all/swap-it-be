package com.example.swapit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.swapit.domain.Reviews;
import com.example.swapit.domain.Users;

import io.lettuce.core.dynamic.annotation.Param;

public interface ReviewRepository extends JpaRepository<Reviews, Long> {
	List<Reviews> findAllByRevieweeOrderByCreatedAtDesc(Users reviewee);

	@Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Reviews r WHERE r.reviewee = :reviewee")
	Double averageRatingByReviewee(@Param("reviewee") Users reviewee);

	List<Reviews> findTop3ByRevieweeOrderByCreatedAtDesc(Users reviewee);
}