package com.example.swapit.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final CurrentUserService currentUserService;

	@Override
	@Transactional(readOnly = true)
	public List<NotificationDto> getMyNotifications() {
		Users user = currentUserService.getCurrentUser();
		return notificationRepository.findByUserAndReadNotOrderByCreatedAtDesc(user)
			.stream()
			.map(NotificationDto::of)
			.toList();
	}
}