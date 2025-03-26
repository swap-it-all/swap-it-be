package com.example.swapit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.common.api.ApiResponse;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.domain.dto.NotificationListDto;
import com.example.swapit.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

	private MockMvc mvc;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private NotificationController notificationController;

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(notificationController).build();
	}

	@Test
	@DisplayName("알림 목록 조회 API 테스트")
	void getMyNotificationsTest() throws Exception {
		// Given
		List<NotificationDto> notifications = List.of(
			new NotificationDto(1L, "CHAT", "Title 1", "Body 1", 1L, LocalDateTime.now()),
			new NotificationDto(2L, "REQUESTED", "Title 2", "Body 2", 2L, LocalDateTime.now())
		);
		NotificationListDto notificationListDto = new NotificationListDto(notifications);
		ApiResponse<NotificationListDto> mockResponse = ApiResponse.success(notificationListDto);

		when(notificationService.getMyNotifications()).thenReturn(mockResponse.getResults());

		// When & Then
		mvc.perform(get("/api/user/notifications"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.notifications.length()").value(2))
			.andExpect(jsonPath("$.results.notifications[0].notificationsId").value(1))
			.andExpect(jsonPath("$.results.notifications[0].type").value("CHAT"))
			.andExpect(jsonPath("$.results.notifications[0].title").value("Title 1"))
			.andExpect(jsonPath("$.results.notifications[0].body").value("Body 1"))
			.andExpect(jsonPath("$.results.notifications[0].relatedData").value(1));
	}

	@Test
	@DisplayName("알림 읽음 처리 API 테스트")
	void markNotificationReadTest() throws Exception {
		// given
		Long notificationId = 1L;

		// when & then
		mvc.perform(patch("/api/user/notifications/{notificationsId}/read", notificationId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		verify(notificationService, times(1)).notificationRead(notificationId);
	}
}