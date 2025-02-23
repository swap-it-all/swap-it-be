package com.example.swapit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.example.swapit.domain.dto.UserNicknameDto;
import com.example.swapit.domain.dto.UserPageDto;
import com.example.swapit.service.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private UserController userController;

	@Mock
	private UsersService usersService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	UserPageDto userPageDto;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
		userPageDto = new UserPageDto(
			1L, "testUser", "test@example.com",
			"https://profile.img", 10L, 5L, 4.5,
			List.of()  // 리뷰 리스트는 비워둠
		);
	}

	@Test
	@DisplayName("로그인된 사용자 마이페이지 조회 성공")
	void getMyPage_Success() throws Exception {
		// Given
		when(usersService.getMyPage()).thenReturn(userPageDto);

		// When & Then
		mockMvc.perform(get("/api/user/auth/info/my")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.usersId").value(1L))
			.andExpect(jsonPath("$.results.nickname").value("testUser"))
			.andExpect(jsonPath("$.results.email").value("test@example.com"))
			.andExpect(jsonPath("$.results.profileImageUrl").value("https://profile.img"))
			.andExpect(jsonPath("$.results.totalGoodsCount").value(10L))
			.andExpect(jsonPath("$.results.completedSwapCount").value(5L))
			.andExpect(jsonPath("$.results.ratingAverage").value(4.5));
	}

	@Test
	@DisplayName("다른 사용자 페이지 조회 성공")
	void getAnotherUserPage_Success() throws Exception {
		// Given
		when(usersService.getAnotherUserPage(2L)).thenReturn(userPageDto);

		// When & Then
		mockMvc.perform(get("/api/all/auth/info/{userId}", 2L)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.results.usersId").value(1L))
			.andExpect(jsonPath("$.results.nickname").value("testUser"))
			.andExpect(jsonPath("$.results.profileImageUrl").value("https://profile.img"))
			.andExpect(jsonPath("$.results.totalGoodsCount").value(10L))
			.andExpect(jsonPath("$.results.completedSwapCount").value(5L))
			.andExpect(jsonPath("$.results.ratingAverage").value(4.5));
	}

	@Test
	@DisplayName("유저 프로필 이미지 업데이트 성공")
	void updateProfileImage_Success() throws Exception {
		// GIVEN: Mock MultipartFile 생성
		MockMultipartFile mockFile = new MockMultipartFile(
			"image", "profile.jpg", "image/jpeg", new byte[] {1, 2, 3, 4}
		);

		// WHEN: API 호출
		mockMvc.perform(multipart(HttpMethod.PATCH, "/api/user/auth/profile/image")
				.file(mockFile)
				.contentType(MediaType.MULTIPART_FORM_DATA))
			.andExpect(status().isOk()) // 200 응답 확인
			.andExpect(jsonPath("$.success").value(true)); // ApiResponse.success() 검증

		// THEN: Service 메서드 호출 검증
		verify(usersService, times(1)).updateProfileImage(any(MultipartFile.class));
	}

	@Test
	@DisplayName("유저 닉네임 업데이트 성공")
	void updateNicknameSuccess() throws Exception {
		// given
		UserNicknameDto dto = new UserNicknameDto("닉네임");

		doNothing().when(usersService).updateNickname(any(UserNicknameDto.class));

		// when
		mockMvc.perform(patch("/api/user/auth/profile/nickname")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true));

		// then
		verify(usersService, times(1)).updateNickname(any(UserNicknameDto.class));
	}
}