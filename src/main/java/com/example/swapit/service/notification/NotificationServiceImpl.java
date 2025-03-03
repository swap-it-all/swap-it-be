package com.example.swapit.service.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.domain.dto.NotificationListDto;
import com.example.swapit.repository.NotificationRepository;
import com.example.swapit.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;
	private final CurrentUserService currentUserService;

	@Override
	@Transactional(readOnly = true)
	public NotificationListDto getMyNotifications() {
		Users user = currentUserService.getCurrentUser();
		return new NotificationListDto(
			notificationRepository.findByUserAndReadNotOrderByCreatedAtDesc(user)
				.stream()
				.map(NotificationDto::of)
				.toList());
	}

	@Override
	@Transactional
	public void notificationRead(Long notificationId) {
		Notifications noti = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
		noti.setRead(true);
		notificationRepository.save(noti);
	}
}