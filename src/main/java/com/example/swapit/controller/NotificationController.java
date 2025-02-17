package com.example.swapit.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/api/user/notifications")
	public ApiResponse<List<NotificationDto>> getMyNotifications() {
		return ApiResponse.success(notificationService.getMyNotifications());
	}
}