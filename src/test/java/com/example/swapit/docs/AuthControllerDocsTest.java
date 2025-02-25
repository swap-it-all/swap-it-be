package com.example.swapit.docs;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.*;
import static com.epages.restdocs.apispec.ResourceDocumentation.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.swapit.controller.AuthController;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.service.AuthServiceImpl;

public class AuthControllerDocsTest extends RestDocsTest {

	private final AuthServiceImpl authService = mock(AuthServiceImpl.class);

	@Override
	protected Object initController() {
		return new AuthController(authService);
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
		mockMvc.perform(get("/api/all/auth/login/google")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("google-login",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Auth")
					.description("구글 로그인을 수행하여 액세스 토큰, 리프레시 토큰, 사용자 이메일 정보를 반환하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results.accessToken").type(JsonFieldType.STRING).description("발급된 액세스 토큰"),
						fieldWithPath("results.refreshToken").type(JsonFieldType.STRING).description("발급된 리프레시 토큰"),
						fieldWithPath("results.key").type(JsonFieldType.STRING).description("사용자 이메일")
					)
					.build()
				)
			));
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
		mockMvc.perform(get("/api/all/auth/login/kakao")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("kakao-login",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Auth")
					.description("카카오 로그인을 수행하여 액세스 토큰, 리프레시 토큰, 사용자 이메일 정보를 반환하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results.accessToken").type(JsonFieldType.STRING).description("발급된 액세스 토큰"),
						fieldWithPath("results.refreshToken").type(JsonFieldType.STRING).description("발급된 리프레시 토큰"),
						fieldWithPath("results.key").type(JsonFieldType.STRING).description("사용자 이메일")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("새로운 토큰 발급 성공 테스트")
	void tokenRefreshSuccess() throws Exception {
		// Given
		String token = "Bearer old_refresh_token";
		TokenDTO newToken = Mockito.mock(TokenDTO.class);
		when(newToken.getAccessToken()).thenReturn("new_access_token");
		when(newToken.getRefreshToken()).thenReturn("new_refresh_token");
		when(newToken.getKey()).thenReturn("example@kakao.com");

		doReturn(newToken).when(authService).refresh(token);

		// When & Then
		mockMvc.perform(post("/api/user/auth/refresh")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("token-refresh",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Auth")
					.description("토큰이 만료되었을 때 리프레시 토큰을 헤더로 받아 토큰 갱신 후 액세스 토큰, 리프레시 토큰, 사용자 이메일 정보를 반환하는 API")
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results.accessToken").type(JsonFieldType.STRING).description("발급된 액세스 토큰"),
						fieldWithPath("results.refreshToken").type(JsonFieldType.STRING).description("발급된 리프레시 토큰"),
						fieldWithPath("results.key").type(JsonFieldType.STRING).description("사용자 이메일")
					)
					.build()
				)
			));
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logoutSuccess() throws Exception {
		// Given
		String token = "Bearer valid_token";
		doNothing().when(authService).deleteRefreshToken(any());

		// When & Then
		mockMvc.perform(post("/api/user/auth/logout")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("logout",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Auth")
					.description("리프레시 토큰을 헤더로 받아 로그아웃하는 API")
					.build()
				)
			));
	}
}
