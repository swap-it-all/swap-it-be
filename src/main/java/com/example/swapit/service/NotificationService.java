package com.example.swapit.service;

import java.util.List;

import com.example.swapit.domain.dto.NotificationDto;

public interface NotificationService {
	List<NotificationDto> getMyNotifications();
}