package com.example.swapit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.config.security.jwt.JwtService;
import com.example.swapit.domain.Tokens;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.dto.TokenDTO;
import com.example.swapit.domain.dto.UserResponseDTO;
import com.example.swapit.repository.ChatRoomsRepository;
import com.example.swapit.repository.ChatsRepository;
import com.example.swapit.repository.FcmTokenRepository;
import com.example.swapit.repository.GoodsImagesRepository;
import com.example.swapit.repository.NotificationRepository;
import com.example.swapit.repository.ReviewRepository;
import com.example.swapit.repository.TokensRepository;
import com.example.swapit.repository.UsersRepository;
import com.example.swapit.repository.WithdrawReasonsRepository;
import com.example.swapit.repository.good.GoodsRepository;
import com.example.swapit.repository.trade.TradesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthServiceTest {

	@Spy
	@InjectMocks
	private AuthServiceImpl authService;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private JwtService jwtService;

	@Mock
	private UsersRepository usersRepository;

	@Mock
	private TokensRepository tokensRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private FcmTokenRepository fcmTokenRepository;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private ChatsRepository chatsRepository;

	@Mock
	private ChatRoomsRepository chatRoomsRepository;

	@Mock
	private TradesRepository tradesRepository;

	@Mock
	private GoodsRepository goodsRepository;

	@Mock
	private GoodsImagesRepository goodsImagesRepository;

	@Mock
	private WithdrawReasonsRepository withdrawReasonsRepository;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	@Mock
	private AwsS3Service awsS3Service;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(authService, "restTemplate", restTemplate);
	}

	@Test
	@DisplayName("토큰 재발급 성공")
	void refreshTokenSuccess() {
		// Given
		String validRefreshToken = "valid_refresh_token";
		String email = "test@example.com";
		Users user = new Users();
		TokenDTO newToken = new TokenDTO("new_access_token", "new_refresh_token", email);

		when(jwtProvider.getEmailFromRefreshToken(validRefreshToken)).thenReturn(email);
		when(jwtService.validateRefreshToken(email, validRefreshToken)).thenReturn(true);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(jwtProvider.createToken(email)).thenReturn(newToken);

		// When
		TokenDTO tokenDTO = authService.refresh("Bearer " + validRefreshToken);

		// Then
		assertEquals("new_access_token", tokenDTO.getAccessToken());
		assertEquals("new_refresh_token", tokenDTO.getRefreshToken());
		assertEquals(email, tokenDTO.getKey());

		verify(jwtProvider, times(1)).getEmailFromRefreshToken(validRefreshToken);
		verify(jwtService, times(1)).validateRefreshToken(email, validRefreshToken);
		verify(usersRepository, times(1)).findByEmail(email);
		verify(jwtProvider, times(1)).createToken(email);
		verify(jwtService, times(1)).updateRefreshToken(eq(user), eq(newToken.getRefreshToken()));
	}

	@Test
	@DisplayName("유효하지 않은 리프레시 토큰으로 토큰 재발급 실패")
	void refreshTokenInvalidToken() {
		// Given
		String invalidRefreshToken = "invalid_refresh_token";
		String bearerToken = "Bearer " + invalidRefreshToken;
		String email = "test@example.com";

		when(jwtProvider.getEmailFromRefreshToken(invalidRefreshToken)).thenReturn(email);
		when(jwtService.validateRefreshToken(email, invalidRefreshToken)).thenReturn(false);

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> authService.refresh(bearerToken));
		assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
	}

	@Test
	@DisplayName("사용자를 찾을 수 없는 경우로 토큰 재발급 실패")
	void refreshTokenUserNotFound() {
		// Given
		String validRefreshToken = "valid_refresh_token";
		String bearerToken = "Bearer " + validRefreshToken;
		String email = "test@example.com";

		when(jwtProvider.getEmailFromRefreshToken(validRefreshToken)).thenReturn(email);
		when(jwtService.validateRefreshToken(email, validRefreshToken)).thenReturn(true);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> authService.refresh(bearerToken));
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("토큰 검증 중 예외 발생으로 토큰 재발급 실패")
	void refreshTokenExceptionThrown() {
		// Given
		String refreshToken = "some_refresh_token";
		String bearerToken = "Bearer " + refreshToken;

		when(jwtProvider.getEmailFromRefreshToken(refreshToken)).thenThrow(new RuntimeException("예외 발생"));

		// When & Then
		CustomException exception = assertThrows(CustomException.class, () -> authService.refresh(bearerToken));
		assertEquals(ErrorCode.NEW_REFRESH_TOKEN_FAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("사용자 정보 조회 성공")
	void getUserInfoSuccess() {
		// Given
		String validAccessToken = "valid_access_token";
		String email = "test@example.com";
		String imageUrl = "http://presigned-url.com/image.jpg";

		Users user = Users.builder()
			.nickname("nickname")
			.email(email)
			.profileImageUrl(imageUrl)
			.loginInfo("google")
			.build();

		when(jwtProvider.validateToken(validAccessToken)).thenReturn(true);
		when(jwtProvider.getEmailFromToken(validAccessToken)).thenReturn(email);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.of(user));

		// When
		UserResponseDTO userResponseDTO = authService.getUserInfo("Bearer " + validAccessToken);

		// Then
		assertEquals("nickname", userResponseDTO.getNickname());
		assertEquals(email, userResponseDTO.getEmail());
		assertEquals(imageUrl, userResponseDTO.getProfileImgUrl());
		assertEquals("google", userResponseDTO.getLoginInfo());

		verify(jwtProvider, times(1)).validateToken(validAccessToken);
		verify(jwtProvider, times(1)).getEmailFromToken(validAccessToken);
		verify(usersRepository, times(1)).findByEmail(email);
	}

	@Test
	@DisplayName("유효하지 않은 토큰으로 사용자 정보 조회 실패")
	void getUserInfoInvalidToken() {
		// Given
		String invalidAccessToken = "invalid_access_token";

		when(jwtProvider.validateToken(invalidAccessToken)).thenReturn(false);

		// When
		CustomException exception = assertThrows(CustomException.class,
			() -> authService.getUserInfo(invalidAccessToken));
		// Then
		assertEquals(ErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
	}

	@Test
	@DisplayName("사용자를 찾을 수 없는 경우로 사용자 정보 조회 실패")
	void getUserInfoUserNotFound() {
		// Given
		String validAccessToken = "valid_access_token";
		String email = "test@example.com";

		when(jwtProvider.validateToken(validAccessToken)).thenReturn(true);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.empty()); // 사용자 없음

		// When
		CustomException exception = assertThrows(CustomException.class,
			() -> authService.getUserInfo(validAccessToken));
		// Then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("사용자 정보 조회 실패")
	void getUserInfoFailed() {
		// Given
		String validAccessToken = "valid_access_token";

		when(jwtProvider.validateToken(validAccessToken)).thenThrow(new RuntimeException("예외 발생"));

		// When
		CustomException exception = assertThrows(CustomException.class,
			() -> authService.getUserInfo(validAccessToken));
		// Then
		assertEquals(ErrorCode.GET_USER_INFO_FAIL, exception.getErrorCode());
	}

	@Test
	@DisplayName("구글 로그인 성공")
	void googleLoginSuccess() {
		// Given
		String validAccessToken = "valid_access_token";
		String email = "test@example.com";

		Users user = Users.builder()
			.nickname("nickname")
			.email(email)
			.profileImageUrl("http://example.com/image.jpg")
			.loginInfo("google")
			.build();

		TokenDTO tokenDTO = new TokenDTO("access_token", "refresh_token", email);

		doReturn(user).when(authService).getGoogleUserInfo(any());
		when(jwtProvider.createToken(email)).thenReturn(tokenDTO);
		doNothing().when(jwtService).saveRefreshToken(user, tokenDTO.getRefreshToken());

		// When
		TokenDTO result = authService.googleLogin(validAccessToken);

		// Then
		assertNotNull(result);
		assertEquals("access_token", result.getAccessToken());
		assertEquals("refresh_token", result.getRefreshToken());
		assertEquals(email, result.getKey());

		verify(jwtProvider, times(1)).createToken(email);
		verify(jwtService, times(1)).saveRefreshToken(eq(user), eq(tokenDTO.getRefreshToken()));
	}

	@Test
	@DisplayName("유효하지 않은 토큰으로 구글 로그인 실패")
	void googleLoginInvalidToken() {
		// Given
		String invalidAccessToken = "invalid_access_token";

		CustomException exception = new CustomException(ErrorCode.LOGIN_FAIL);
		doThrow(exception).when(jwtProvider).validateToken(invalidAccessToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authService.googleLogin(invalidAccessToken);
		});
		assertEquals(ErrorCode.LOGIN_FAIL, thrown.getErrorCode());
	}

	@Test
	@DisplayName("Google 사용자 정보 조회 - 신규 사용자 저장")
	void getGoogleUserInfoNewUser() throws Exception {
		// Given
		String accessToken = "Bearer test_google_token";
		String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		String email = "test@example.com";
		String nickname = "Test User";
		String profileImageUrl = "https://test.com/profile.jpg";
		String role = "ROLE_USER";

		String responseJson = objectMapper.writeValueAsString(
			Map.of("email", email, "name", nickname, "picture", profileImageUrl));
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);

		when(restTemplate.exchange(eq(googleUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class),
			eq(String.class))).thenReturn(responseEntity);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Users user = authService.getGoogleUserInfo(accessToken);

		// Then
		assertNotNull(user);
		assertEquals(email, user.getEmail());
		assertEquals(nickname, user.getNickname());
		assertEquals(profileImageUrl, user.getProfileImageUrl());
		assertEquals("google", user.getLoginInfo());
		assertEquals(role, user.getRole());

		verify(usersRepository, times(1)).findByEmail(email);
		verify(usersRepository, times(1)).save(any(Users.class));
	}

	@Test
	@DisplayName("Google 사용자 정보 조회 - 기존 사용자 반환")
	void getGoogleUserInfoExistingUser() throws Exception {
		// Given
		String accessToken = "Bearer test_google_token";
		String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		String email = "existing@example.com";

		Users existingUser = Users.builder()
			.email(email)
			.nickname("Existing User")
			.profileImageUrl("https://test.com/existing.jpg")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();

		String responseJson = objectMapper.writeValueAsString(Map.of("email", email));
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);

		when(restTemplate.exchange(eq(googleUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class),
			eq(String.class))).thenReturn(responseEntity);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

		// When
		Users user = authService.getGoogleUserInfo(accessToken);

		// Then
		assertNotNull(user);
		assertEquals(existingUser, user);

		verify(usersRepository, times(1)).findByEmail(email);
		verify(usersRepository, never()).save(any(Users.class));
	}

	@Test
	@DisplayName("카카오 로그인 성공")
	void kakaoLoginSuccess() {
		// Given
		String validAccessToken = "valid_access_token";
		String email = "test@example.com";

		Users user = Users.builder()
			.nickname("nickname")
			.email(email)
			.profileImageUrl("http://example.com/image.jpg")
			.loginInfo("google")
			.build();

		TokenDTO tokenDTO = new TokenDTO("access_token", "refresh_token", email);

		doReturn(user).when(authService).getKakaoUserInfo(any());
		when(jwtProvider.createToken(email)).thenReturn(tokenDTO);
		doNothing().when(jwtService).saveRefreshToken(user, tokenDTO.getRefreshToken());

		// When
		TokenDTO result = authService.kakaoLogin(validAccessToken);

		// Then
		assertNotNull(result);
		assertEquals("access_token", result.getAccessToken());
		assertEquals("refresh_token", result.getRefreshToken());
		assertEquals(email, result.getKey());

		verify(jwtProvider, times(1)).createToken(email);
		verify(jwtService, times(1)).saveRefreshToken(eq(user), eq(tokenDTO.getRefreshToken()));
	}

	@Test
	@DisplayName("유효하지 않은 토큰으로 카카오 로그인 실패")
	void kakaoLoginInvalidToken() {
		// Given
		String invalidAccessToken = "invalid_access_token";

		CustomException exception = new CustomException(ErrorCode.LOGIN_FAIL);
		doThrow(exception).when(jwtProvider).validateToken(invalidAccessToken);

		// When & Then
		CustomException thrown = assertThrows(CustomException.class, () -> {
			authService.kakaoLogin(invalidAccessToken);
		});
		assertEquals(ErrorCode.LOGIN_FAIL, thrown.getErrorCode());
	}

	@Test
	@DisplayName("카카오 사용자 정보 조회 - 신규 사용자 저장")
	void getKakaoUserInfo_NewUser() throws Exception {
		// Given
		String accessToken = "Bearer test_kakao_token";
		String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";
		String email = "test@example.com";
		String nickname = "Test User";
		String profileImageUrl = "https://test.com/profile.jpg";
		String role = "ROLE_USER";

		String responseJson = objectMapper.writeValueAsString(
			Map.of(
				"kakao_account", Map.of("email", email),
				"properties", Map.of("nickname", nickname, "profile_image", profileImageUrl)
			)
		);
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);

		when(restTemplate.exchange(eq(kakaoUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class),
			eq(String.class))).thenReturn(responseEntity);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());
		when(usersRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Users user = authService.getKakaoUserInfo(accessToken);

		// Then
		assertNotNull(user);
		assertEquals(email, user.getEmail());
		assertEquals(nickname, user.getNickname());
		assertEquals(profileImageUrl, user.getProfileImageUrl());
		assertEquals("kakao", user.getLoginInfo());
		assertEquals(role, user.getRole());

		verify(usersRepository, times(1)).findByEmail(email);
		verify(usersRepository, times(1)).save(any(Users.class));
	}

	@Test
	@DisplayName("Google 사용자 정보 조회 - 기존 사용자 반환")
	void getGoogleUserInfo_ExistingUser() throws Exception {
		// Given
		String accessToken = "Bearer test_google_token";
		String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
		String email = "existing@example.com";

		Users existingUser = Users.builder()
			.email(email)
			.nickname("Existing User")
			.profileImageUrl("https://test.com/existing.jpg")
			.loginInfo("google")
			.role("ROLE_USER")
			.build();

		String responseJson = objectMapper.writeValueAsString(Map.of("email", email));
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseJson, HttpStatus.OK);

		when(restTemplate.exchange(eq(googleUserInfoUrl), eq(HttpMethod.GET), any(HttpEntity.class),
			eq(String.class))).thenReturn(responseEntity);
		when(usersRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

		// When
		Users user = authService.getGoogleUserInfo(accessToken);

		// Then
		assertNotNull(user);
		assertEquals(existingUser, user);

		// Then
		verify(usersRepository, times(1)).findByEmail(email);
		verify(usersRepository, never()).save(any(Users.class));
	}

	@Test
	@DisplayName("리프레시 토큰 삭제 성공 - 존재하는 토큰")
	void deleteRefreshTokenSuccess() {
		// Given
		String Token = "valid_refresh_token";
		String bearerToken = "Bearer " + Token;
		Tokens tokenEntity = new Tokens();

		when(tokensRepository.findByRefreshToken(Token)).thenReturn(Optional.of(tokenEntity));

		// When
		authService.deleteRefreshToken(bearerToken);

		// Then
		verify(tokensRepository, times(1)).findByRefreshToken(Token);
		verify(tokensRepository, times(1)).delete(tokenEntity);
	}

	@Test
	@DisplayName("리프레시 토큰 삭제 - 존재하지 않는 토큰")
	void deleteRefreshToken_NotFound() {
		// Given
		String Token = "non_existent_token";
		String bearerToken = "Bearer " + Token;

		when(tokensRepository.findByRefreshToken(Token)).thenReturn(Optional.empty());

		// When
		authService.deleteRefreshToken(bearerToken);

		// Then
		verify(tokensRepository, times(1)).findByRefreshToken(Token);
		verify(tokensRepository, never()).delete(any());
	}

	@Test
	@DisplayName("회원정보 삭제 성공 테스트")
	void testUserWithdraw() {
		Users testUser = Users.builder()
			.usersId(1L)
			.nickname("testuser")
			.email("test@example.com")
			.profileImageUrl("http://example.com/profile.jpg")
			.loginInfo("GOOGLE")
			.role("USER")
			.build();

		when(currentUserService.getCurrentUser()).thenReturn(testUser);

		// 물건, 채팅방, 거래 관련 리포지토리는 빈 리스트를 반환하도록 설정
		when(goodsRepository.findByUserOrderByCreatedAtDesc(testUser))
			.thenReturn(Collections.emptyList());
		when(goodsImagesRepository.findByGoodIn(Collections.emptyList()))
			.thenReturn(Collections.emptyList());
		when(chatRoomsRepository.findMyChatRooms(testUser.getUsersId()))
			.thenReturn(Collections.emptyList());
		when(tradesRepository.findAllByUser(testUser.getUsersId()))
			.thenReturn(Collections.emptyList());

		String reason = "테스트 탈퇴 사유";

		// userWithdraw() 메서드 실행 (내부에서 S3 삭제 메서드도 호출됨)
		authService.userWithdraw(reason);

		// FCM 토큰, 리프레시 토큰, 알림 삭제 검증
		verify(fcmTokenRepository).deleteByUser(testUser);
		verify(tokensRepository).deleteByUser(testUser);
		verify(notificationRepository).deleteAllByUser(testUser);

		// 후기, 채팅 메시지 삭제 검증
		verify(reviewRepository).deleteAllByWriterOrReviewee(testUser, testUser);
		verify(chatsRepository).deleteAllBySender(testUser);

		// 채팅방 관련 삭제 검증
		verify(chatRoomsRepository).findMyChatRooms(testUser.getUsersId());
		verify(chatRoomsRepository).deleteAll(anyList());

		// 거래 관련 삭제 검증
		verify(tradesRepository).findAllByUser(testUser.getUsersId());
		verify(tradesRepository).deleteAll(anyList());

		// 물건 및 물건 사진 관련 검증
		verify(goodsRepository).findByUserOrderByCreatedAtDesc(testUser);
		verify(goodsImagesRepository).findByGoodIn(anyList());
		verify(goodsImagesRepository).deleteAll(anyList());
		verify(goodsRepository).deleteAll(anyList());

		// 탈퇴 사유 저장시, userId가 올바르게 전달되었는지 검증
		verify(withdrawReasonsRepository).save(argThat(wr ->
			reason.equals(wr.getReason()) &&
				testUser.getUsersId().equals(wr.getUsersId())
		));

		// 사용자 삭제 검증
		verify(usersRepository).delete(testUser);

		// DB 트랜잭션 이후 S3 삭제 수행 검증
		verify(applicationEventPublisher).publishEvent(argThat(event ->
			event instanceof UserWithdrawCompletedEvent &&
				((UserWithdrawCompletedEvent)event).getImages().equals(Collections.emptyList()) &&
				((UserWithdrawCompletedEvent)event).getUser().equals(testUser)
		));
	}
}