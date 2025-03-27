package com.example.swapit.service.notification;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.Notifications;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.repository.FcmTokenRepository;
import com.example.swapit.repository.NotificationRepository;
import com.example.swapit.repository.UsersRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final SimpMessagingTemplate messagingTemplate;
	private final NotificationRepository notificationRepository;
	private final UsersRepository usersRepository;
	private final FcmTokenRepository fcmTokenRepository;
	private final FcmNotificationService fcmNotificationService;

	@EventListener
	public void handleNotification(NotificationEvent event) {
		// 사용자 조회
		Long userId = event.getUserId();
		Users user = usersRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 알림 저장
		Notifications noti = event.toEntity(user);
		notificationRepository.save(noti);

		// 알림 설정이 꺼져 있으면 전송 생략
		if (!user.isNotificationEnabled()) {
			log.info("[알림 꺼짐] 사용자 ID {}: 알림 전송하지 않음 {}", userId, noti.getId());
			return;
		}

		// 웹소켓 알림 전송 (앱이 온라인 상태) : "/user/queue/notifications" 구독된 상태
		try {
			messagingTemplate.convertAndSendToUser(
				userId.toString(), "/queue/notifications", NotificationDto.of(noti)
			);
		} catch (Exception e) {
			log.info("[WS알림LOG] 알림 웹소켓 전송 실패 -> FCM으로 전송. {}", e.getMessage());
			sendFcm(user, noti);
		}
	}

	private void sendFcm(Users user, Notifications noti) {
		fcmTokenRepository.findByUser(user).ifPresentOrElse(
			token -> fcmNotificationService.sendFcmNotification(token.getFcmToken(), noti),
			() -> log.warn("[FCM알림 실패] 사용자 ID {}: FCM 토큰 없음", user.getUsersId())
		);
	}
}