package com.example.swapit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {
	@Query("SELECT n FROM Notifications n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
	List<Notifications> findByUserAndReadNotOrderByCreatedAtDesc(@Param("user") Users user);

	void deleteAllByUser(Users users);
}
