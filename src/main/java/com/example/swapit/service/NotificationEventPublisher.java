package com.example.swapit.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.swapit.domain.NotificationEvent;
import com.example.swapit.domain.NotificationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

	private final ApplicationEventPublisher eventPublisher;

	// 기본 URL 사용
	public void publishNotification(Long userId, NotificationType type) {
		eventPublisher.publishEvent(new NotificationEvent(this, userId, type));
	}

	// URL에 동적 값 적용
	public void publishNotification(Long userId, NotificationType type, Object... urlParams) {
		eventPublisher.publishEvent(new NotificationEvent(this, userId, type, urlParams));
	}
}