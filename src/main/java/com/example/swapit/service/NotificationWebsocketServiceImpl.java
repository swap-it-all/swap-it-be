package com.example.swapit.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.domain.dto.NotificationRequestDto;
import com.example.swapit.repository.NotificationRepository;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationWebsocketServiceImpl implements NotificationWebsocketService {

	private final SimpMessagingTemplate messagingTemplate;
	private final NotificationRepository notificationRepository;
	private final UsersRepository usersRepository;

	@Override
	public void sendNotification(Long userId, NotificationRequestDto dto) {
		Users user = usersRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		Notifications noti = dto.toEntity(user, dto.getType(), dto.getUrl());
		notificationRepository.save(noti);

		// websocket 실시간 알림 전송 : "/user/queue/notifications" 구독
		messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", NotificationDto.of(noti));
	}
}