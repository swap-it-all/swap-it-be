package com.example.swapit.service;

import com.example.swapit.domain.dto.NotificationRequestDto;

public interface NotificationWebsocketService {
	public void sendNotification(Long userId, NotificationRequestDto dto);
}