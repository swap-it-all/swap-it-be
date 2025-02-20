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
import com.epages.restdocs.apispec.Schema;
import com.example.swapit.controller.AuthController;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.service.AuthServiceImpl;

public class UserControllerDocsTest extends RestDocsSupport {

	private final AuthServiceImpl authService = mock(AuthServiceImpl.class);

	@Override
	protected Object initController() {
		return new AuthController(authService);
	}

	@Test
	@DisplayName("구글 로그인 성공 테스트")
	void googleLoginSuccess() throws Exception {
		// Given: REST Docs 생성을 위한 모의 데이터
		String token = "Bearer valid_token";
		TokenDTO tokenDTO = Mockito.mock(TokenDTO.class);
		when(tokenDTO.getAccessToken()).thenReturn("test_access_token");
		when(tokenDTO.getRefreshToken()).thenReturn("test_refresh_token");
		when(tokenDTO.getKey()).thenReturn("example@google.com");

		// authService.googleLogin() 호출 시, 모의 객체 반환
		doReturn(tokenDTO).when(authService).googleLogin(any());

		// When & Then: REST Docs 문서를 생성하는 테스트 실행
		mockMvc.perform(get("/api/all/auth/login/google")
				.header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("google-login",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.tag("Google Login")
					.description("구글 로그인을 수행하여 액세스 토큰, 리프레시 토큰, 사용자 이메일 정보를 반환하는 API")
					.requestHeaders(
						headerWithName("Authorization").description("Bearer 형식의 액세스 토큰")
					)
					.responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("results.accessToken").type(JsonFieldType.STRING).description("발급된 액세스 토큰"),
						fieldWithPath("results.refreshToken").type(JsonFieldType.STRING).description("발급된 리프레시 토큰"),
						fieldWithPath("results.key").type(JsonFieldType.STRING).description("사용자 이메일")
					)
					.responseSchema(Schema.schema("TokenDTO"))
					.build()
				)
			));
	}
}
