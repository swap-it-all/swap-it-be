package com.example.swapit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.service.AuthService;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

	MockMvc mockMvc;

	@Mock
	private AuthService authService;

	@InjectMocks
	private AuthController authController;

	@BeforeEach
	void setUp() {
		// MockMvc 초기화
		mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
	}

	@Test
	@DisplayName("새로운 토큰 발급 성공 테스트")
	void testSuccessfulTokenRefresh() throws Exception {
		// Given
		String oldRefreshToken = "old_refresh_token";
		String bearerToken = "Bearer " + oldRefreshToken;

		TokenDTO newToken = Mockito.mock(TokenDTO.class);
		when(newToken.getAccessToken()).thenReturn("new_access_token");
		when(newToken.getRefreshToken()).thenReturn("new_refresh_token");

		doReturn(newToken).when(authService).refresh(bearerToken);

		// When & Then
		mockMvc.perform(
				post("/api/user/auth/refresh").header("Authorization", bearerToken).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.refreshToken").value("new_refresh_token"))
			.andExpect(jsonPath("$.results.accessToken").value("new_access_token"))
			.andDo(print());
	}

	@Test
	@DisplayName("잘못된 리프레시 토큰일 경우 테스트")
	void testInvalidRefreshToken() {
		// Given
		String invalidRefreshToken = "invalid_refresh_token";
		String bearerToken = "Bearer " + invalidRefreshToken;

		// When
		CustomException exception = new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		doThrow(exception).when(authService).refresh(bearerToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authController.refresh(bearerToken);
		});
		assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, thrown.getErrorCode());
	}

	@Test
	@DisplayName("사용자를 찾을 수 없을 경우 테스트")
	void testUserNotFound() {
		// Given
		String refreshToken = "refresh_token";
		String bearerToken = "Bearer " + refreshToken;

		// When
		CustomException exception = new CustomException(ErrorCode.USER_NOT_FOUND);
		doThrow(exception).when(authService).refresh(bearerToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authController.refresh(bearerToken);
		});
		assertEquals(ErrorCode.USER_NOT_FOUND, thrown.getErrorCode());
	}

	@Test
	@DisplayName("사용자 정보 조회 성공 테스트")
	void getUserInfoSuccess() throws Exception {
		// Given
		String token = "Bearer valid_token";

		UserResponseDTO responseDTO = UserResponseDTO.builder()
			.nickname("testUser")
			.email("test@example.com")
			.profileImgUrl("http://example.com/image.jpg")
			.loginInfo("KAKAO")
			.build();

		doReturn(responseDTO).when(authService).getUserInfo(any());

		// When & Then
		mockMvc.perform(
				get("/api/user/auth/my").header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.nickname").value("testUser"))
			.andExpect(jsonPath("$.results.email").value("test@example.com"))
			.andExpect(jsonPath("$.results.loginInfo").value("KAKAO"))
			.andDo(print());
	}

	@Test
	@DisplayName("잘못된 토큰으로 사용자 정보 조회 실패 테스트")
	void getUserInfoInvalidToken() {
		// Given
		String invalidToken = "Bearer invalid_token";

		// When
		CustomException exception = new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
		doThrow(exception).when(authService).getUserInfo(invalidToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authController.getUserInfo(invalidToken);
		});
		assertEquals(ErrorCode.INVALID_ACCESS_TOKEN, thrown.getErrorCode());
	}

	@Test
	@DisplayName("구글 로그인 성공 테스트")
	void googleLoginSuccess() throws Exception {
		// Given
		String token = "Bearer valid_token";

		TokenDTO tokenDTO = Mockito.mock(TokenDTO.class);
		when(tokenDTO.getAccessToken()).thenReturn("test_access_token");
		when(tokenDTO.getRefreshToken()).thenReturn("test_refresh_token");
		when(tokenDTO.getKey()).thenReturn("example@google.com");

		doReturn(tokenDTO).when(authService).googleLogin(any());

		// When & Then
		mockMvc.perform(
				get("/api/all/auth/login/google").header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.refreshToken").value("test_refresh_token"))
			.andExpect(jsonPath("$.results.accessToken").value("test_access_token"))
			.andExpect(jsonPath("$.results.key").value("example@google.com"))
			.andDo(print());
	}

	@Test
	@DisplayName("잘못된 토큰으로 사용자 정보 조회 실패 테스트")
	void googleLoginInvalidToken() {
		// Given
		String invalidToken = "Bearer invalid_token";

		// When
		CustomException exception = new CustomException(ErrorCode.LOGIN_FAIL);
		doThrow(exception).when(authService).googleLogin(invalidToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authController.googleLogin(invalidToken);
		});
		assertEquals(ErrorCode.LOGIN_FAIL, thrown.getErrorCode());
	}

	@Test
	@DisplayName("카카오 로그인 성공 테스트")
	void kakaoLoginSuccess() throws Exception {
		// Given
		String token = "Bearer valid_token";

		TokenDTO tokenDTO = Mockito.mock(TokenDTO.class);
		when(tokenDTO.getAccessToken()).thenReturn("test_access_token");
		when(tokenDTO.getRefreshToken()).thenReturn("test_refresh_token");
		when(tokenDTO.getKey()).thenReturn("example@kakao.com");

		doReturn(tokenDTO).when(authService).kakaoLogin(any());

		// When & Then
		mockMvc.perform(
				get("/api/all/auth/login/kakao").header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andExpect(jsonPath("$.results.refreshToken").value("test_refresh_token"))
			.andExpect(jsonPath("$.results.accessToken").value("test_access_token"))
			.andExpect(jsonPath("$.results.key").value("example@kakao.com"))
			.andDo(print());
	}

	@Test
	@DisplayName("잘못된 토큰으로 사용자 정보 조회 실패 테스트")
	void kakaoLoginInvalidToken() {
		// Given
		String invalidToken = "Bearer invalid_token";

		// When
		CustomException exception = new CustomException(ErrorCode.LOGIN_FAIL);
		doThrow(exception).when(authService).kakaoLogin(invalidToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authController.kakaoLogin(invalidToken);
		});
		assertEquals(ErrorCode.LOGIN_FAIL, thrown.getErrorCode());
	}

	@Test
	@DisplayName("로그아웃 성공 테스트")
	void testLogout() throws Exception {
		// Given
		String token = "Bearer valid_token";

		doNothing().when(authService).deleteRefreshToken(any());

		// When & Then
		mockMvc.perform(post("/api/user/auth/logout")
				.header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
			.andDo(print());
	}
}
