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

		// 웹소켓 알림 전송 (앱이 온라인 상태) : "/user/queue/notifications" 구독
		boolean isWebsocketSent = false;
		try {
			messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications",
				NotificationDto.of(noti));
			isWebsocketSent = true;
		} catch (Exception e) {
			// todo : 이상 없으면 이후에 로그 제거
			log.info("[WS알림LOG] 알림 웹소켓 전송 실패 -> FCM으로 전송. {}", e.getMessage());
		}

		// 앱이 백그라운드 상태일 때, FCM 알림 전송 (웹소켓 실패 or FCM 알림 필요)
		if (!isWebsocketSent) {
			fcmTokenRepository.findByUser(user).ifPresent(fcmToken -> {
				fcmNotificationService.sendFcmNotification(fcmToken.getFcmToken(), noti);
			});
		}
	}
}