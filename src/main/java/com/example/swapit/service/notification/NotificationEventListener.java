package com.example.swapit.service.notification;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.websocket.WebsocketDestination;
import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.NotificationType;
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
	private final SimpUserRegistry simpUserRegistry;
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
			log.debug("[알림 꺼짐] 사용자 ID {}: 알림 전송하지 않음 {}", userId, noti.getId());
			return;
		}

		// 웹소켓 알림 전송 (앱이 온라인 상태)
		NotificationDto notiDto = NotificationDto.of(noti);
		String userKey = userId.toString();

		// CHAT 알림 -> 채팅방 구독 중이면 알림 생략
		if (noti.getType().equals(NotificationType.CHAT)) {
			String chatTopic = WebsocketDestination.CHAT_TOPIC.withKey(noti.getRelatedData().toString());
			if (isSubscribed(userKey, chatTopic)) {
				log.debug("[CHAT 알림 무시] 유저 {}가 채팅방 {} 구독 중 → 알림 전송 생략", userId, noti.getRelatedData().toString());
				return;
			}
		}

		if (isSubscribed(userKey, WebsocketDestination.NOTIFICATION_QUEUE.getPath())) {
			messagingTemplate.convertAndSendToUser(userKey, "/queue/notifications", notiDto);
			log.debug("[WS알림 전송] 유저id={}, payload={} ", userId, notiDto);
		} else {
			log.debug("[WS알림 스킵] 유저id={}는 현재 오프라인 상태 → FCM 전송", userId);
			sendFcm(user, noti);
		}
	}

	private boolean isSubscribed(String userKey, String destination) {
		SimpUser user = simpUserRegistry.getUser(userKey);
		log.debug("[WS 유저 조회] userId={}, simpUser.getName()={}", userKey,
			user != null ? user.getName() : "없음");

		if (user == null)
			return false;

		for (SimpSession session : user.getSessions()) {
			log.debug("[WS 세션] sessionId={}, subscription 수={}", session.getId(),
				session.getSubscriptions().size());
			for (SimpSubscription sub : session.getSubscriptions()) {
				if (destination.equals(sub.getDestination())) {
					return true;
				}
			}
		}
		return false;
	}

	private void sendFcm(Users user, Notifications noti) {
		fcmTokenRepository.findByUser(user).ifPresentOrElse(
			token -> fcmNotificationService.sendFcmNotification(token.getFcmToken(), noti),
			() -> log.warn("[FCM알림 실패] 사용자 ID {}: FCM 토큰 없음", user.getUsersId())
		);
	}
}