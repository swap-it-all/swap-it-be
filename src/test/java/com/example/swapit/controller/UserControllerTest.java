package com.example.swapit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
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