package com.example.swapit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.NotificationListDto;
import com.example.swapit.service.notification.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ApiResponse<NotificationListDto> getMyNotifications() {
		return ApiResponse.success(notificationService.getMyNotifications());
	}

	@PatchMapping("/{notificationsId}/read")
	public ApiResponse<Void> markNotificationRead(@PathVariable Long notificationsId) {
		notificationService.notificationRead(notificationsId);
		return ApiResponse.success();
	}
}