package com.example.swapit.docs;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.swapit.controller.NotificationController;
import com.example.swapit.domain.dto.NotificationDto;
import com.example.swapit.domain.dto.NotificationListDto;
import com.example.swapit.service.notification.NotificationService;

public class NotificationControllerDocsTest extends RestDocsTest {

	private final NotificationService notificationService = mock(NotificationService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected Object initController() {
		return new NotificationController(notificationService);
	}

	@Test
	@DisplayName("알림 목록 조회 API 문서화")
	void getMyNotificationsTest() throws Exception {
		// Given
		List<NotificationDto> notifications = List.of(
			new NotificationDto(1L, "CHAT", "Title 1", "Body 1", "deeplink1", LocalDateTime.now()),
			new NotificationDto(2L, "REQUESTED", "Title 2", "Body 2", "deeplink2", LocalDateTime.now())
		);
		NotificationListDto notificationListDto = new NotificationListDto(notifications);

		when(notificationService.getMyNotifications()).thenReturn(notificationListDto);

		// When & Then
		mockMvc.perform(get("/api/user/notifications")
				.header("Authorization", "Bearer valid_token")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("get-my-notifications",
				preprocessRequest(prettyPrint()),
				preprocessResponse(new CustomDatePreprocessor()),
				resource(ResourceSnippetParameters.builder()
					.tag("Notification")
					.description("사용자의 알림 목록을 조회하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터"),
						fieldWithPath("results.notifications").type(JsonFieldType.ARRAY).description("알림 목록"),
						fieldWithPath("results.notifications[].notificationsId").type(JsonFieldType.NUMBER)
							.description("알림 ID"),
						fieldWithPath("results.notifications[].type").type(JsonFieldType.STRING).description("알림 타입"),
						fieldWithPath("results.notifications[].title").type(JsonFieldType.STRING).description("알림 제목"),
						fieldWithPath("results.notifications[].body").type(JsonFieldType.STRING).description("알림 내용"),
						fieldWithPath("results.notifications[].deeplink").type(JsonFieldType.STRING)
							.description("알림 클릭 시 이동 URL (app deeplink)"),
						fieldWithPath("results.notifications[].createdAt").type(JsonFieldType.STRING)
							.description("알림 생성 시간")
					)
					.responseSchema(Schema.schema("NotificationListDto"))
					.build()
				)
			));
	}

	@Test
	@DisplayName("알림 읽음 처리 API 문서화")
	void markNotificationReadTest() throws Exception {
		// Given
		Long notificationId = 1L;
		doNothing().when(notificationService).notificationRead(notificationId);

		// When & Then
		mockMvc.perform(patch("/api/user/notifications/{notificationsId}/read", notificationId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andDo(document("mark-notification-read",
				resource(ResourceSnippetParameters.builder()
					.tag("Notification")
					.description("알림을 읽음 처리하는 API")
					.pathParameters(
						parameterWithName("notificationsId").description("읽음 처리할 알림 ID")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results").type(JsonFieldType.OBJECT).description("응답 결과 데이터").optional()
					)
					.responseSchema(Schema.schema("ApiResponse<Void>"))
					.build()
				)
			));
	}
}