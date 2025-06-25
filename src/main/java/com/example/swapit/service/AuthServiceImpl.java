package com.example.swapit.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

import com.example.swapit.common.exception.CustomException;
import com.example.swapit.common.exception.ErrorCode;
import com.example.swapit.config.security.jwt.JwtProvider;
import com.example.swapit.config.security.jwt.JwtService;
import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;
import com.example.swapit.domain.WithdrawReasons;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
	private final JwtProvider jwtProvider;
	private final JwtService jwtService;
	private final CurrentUserService currentUserService;
	private final AwsS3Service awsS3Service;

	private final UsersRepository usersRepository;
	private final TokensRepository tokensRepository;
	private final WithdrawReasonsRepository withdrawReasonsRepository;
	private final FcmTokenRepository fcmTokenRepository;
	private final NotificationRepository notificationRepository;
	private final ReviewRepository reviewRepository;
	private final ChatsRepository chatsRepository;
	private final ChatRoomsRepository chatRoomsRepository;
	private final TradesRepository tradesRepository;
	private final GoodsRepository goodsRepository;
	private final GoodsImagesRepository goodsImagesRepository;

	private final RestTemplate restTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	private final GoogleIdTokenVerifier verifier;

	private static final String GOOGLE_LOGIN_INFO = "google";
	private static final String KAKAO_LOGIN_INFO = "kakao";

	@Override
	public synchronized TokenDTO refresh(String token) {
		log.debug("[리프레시 토큰 재발급 시작] token={}", token);

		if (token == null || token.isBlank()) {
			throw new CustomException(ErrorCode.TOKEN_IS_BLANK);
		}

		String refreshToken = token.replace("Bearer ", "");
		String userId = jwtProvider.getIdFromRefreshToken(refreshToken);

		if (!jwtService.validateRefreshToken(userId, refreshToken)) {
			log.warn("[실패 : 리프레시 토큰 불일치] email={}, token={}", userId, refreshToken);
			throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
		}

		Users user = usersRepository.findById(Long.valueOf(userId))
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		TokenDTO newToken = jwtProvider.createToken(userId);
		jwtService.updateRefreshToken(user, newToken.getRefreshToken());

		log.debug("[리프레스 토큰 발급 성공 OK] refresh token={}", newToken);
		return newToken;
	}

	@Override
	public UserResponseDTO getUserInfo(String token) {
		try {
			String accessToken = token.replace("Bearer ", "");

			if (!jwtProvider.validateToken(accessToken)) {
				throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
			}

			String userId = jwtProvider.getIdFromToken(accessToken);
			Users user = usersRepository.findById(Long.valueOf(userId))
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

			return UserResponseDTO.builder()
				.nickname(user.getNickname())
				.email(user.getEmail())
				.profileImgUrl(user.getProfileImageUrl())
				.loginInfo(user.getLoginInfo())
				.build();
		} catch (CustomException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
		}
	}

	@Override
	public TokenDTO googleLogin(String idTokenString) {
		try {
			Users user = getGoogleUserInfo(idTokenString);
			TokenDTO jwtToken = jwtProvider.createToken(user.getUsersId().toString());
			jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());
			return jwtToken;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.LOGIN_FAIL);
		}
	}

	@Override
	public TokenDTO kakaoLogin(String kakaoAccessToken) {
		try {
			Users user = getKakaoUserInfo(kakaoAccessToken);
			TokenDTO jwtToken = jwtProvider.createToken(user.getUsersId().toString());
			jwtService.saveRefreshToken(user, jwtToken.getRefreshToken());
			return jwtToken;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.LOGIN_FAIL);
		}
	}

	@Override
	public Users getGoogleUserInfo(String idTokenString) {
		try {
			String token = idTokenString.startsWith("Bearer ")
				? idTokenString.substring(7)
				: idTokenString;

			GoogleIdToken idToken = verifier.verify(token);
			if (idToken == null) {
				throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
			}
			GoogleIdToken.Payload payload = idToken.getPayload();
			String email = payload.getEmail();
			String nickname = (String)payload.get("name");
			String profileImageUrl = (String)payload.get("picture");
			String role = "ROLE_USER";

			return usersRepository.findByEmail(email)
				.orElseGet(() -> {
					Users newUser = Users.builder()
						.nickname(nickname)
						.email(email)
						.profileImageUrl(profileImageUrl)
						.loginInfo(GOOGLE_LOGIN_INFO)
						.role(role)
						.build();
					return usersRepository.save(newUser);
				});
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
		}
	}

	@Override
	public Users getKakaoUserInfo(String accessToken) {
		String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		try {
			ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

			JsonNode responseJson = new ObjectMapper().readTree(response.getBody());
			String email = responseJson.get("kakao_account").get("email").asText();
			String nickname = responseJson.get("properties").get("nickname").asText();
			String profileImageUrl = responseJson.get("properties").get("profile_image").asText();
			String role = "ROLE_USER";

			return usersRepository.findByEmail(email)
				.orElseGet(() -> {
					Users newUser = Users.builder()
						.nickname(nickname)
						.email(email)
						.profileImageUrl(profileImageUrl)
						.loginInfo(KAKAO_LOGIN_INFO)
						.role(role)
						.build();
					return usersRepository.save(newUser);
				});
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GET_USER_INFO_FAIL);
		}
	}

	@Override
	public void deleteRefreshToken(String token) {
		tokensRepository.findByRefreshToken(token.replace("Bearer ", ""))
			.ifPresent(tokensRepository::delete);
	}

	@Override
	@Transactional
	public void withdrawGoogleUser(String googleToken, String reason) {
		Users user = currentUserService.getCurrentUser();

		// 거래중인 스왑 있으면 탈퇴 불가능 처리
		if (tradesRepository.existsInProgressTradeByUser(user)) {
			throw new CustomException(ErrorCode.EXIST_INPROGRESS_TRADE);
		}

		// callGoogleRevoke(googleToken);
		userWithdraw(reason, user);
	}

	@Override
	@Transactional
	public void withdrawKakaoUser(String kakaoToken, String reason) {
		Users user = currentUserService.getCurrentUser();

		// 거래중인 스왑 있으면 탈퇴 불가능 처리
		if (tradesRepository.existsInProgressTradeByUser(user)) {
			throw new CustomException(ErrorCode.EXIST_INPROGRESS_TRADE);
		}

		userWithdraw(reason, user);
		callKakaoRevoke(kakaoToken);
	}

	public void callGoogleRevoke(String googleToken) {
		RestTemplate restTemplate = new RestTemplate();
		String revokeUrl = "https://oauth2.googleapis.com/revoke?token=" + googleToken;

		ResponseEntity<String> response = restTemplate.postForEntity(revokeUrl, null, String.class);

		if (response.getStatusCode() != HttpStatus.OK) {
			log.error("구글 연결 해제 실패");
			throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
		}

		log.info("구글 연결 해제 성공");
	}

	public void callKakaoRevoke(String kakaoToken) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(kakaoToken);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		String unlinkUrl = "https://kapi.kakao.com/v1/user/unlink";
		ResponseEntity<String> response = restTemplate.exchange(unlinkUrl, HttpMethod.POST, entity, String.class);

		if (response.getStatusCode() != HttpStatus.OK) {
			log.error("카카오 연결 해제 실패");
			throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
		}

		log.info("카카오 연결 해제 성공");
	}

	@Transactional
	public void userWithdraw(String reason, Users user) {
		log.info("[회원탈퇴 시작] 사용자 ID: {}", user.getUsersId());

		try {
			// FCM 토큰, 리프레시 토큰, 알림
			log.info("[회원탈퇴] FCM 토큰 삭제 시작");
			fcmTokenRepository.deleteByUser(user);
			log.info("[회원탈퇴] FCM 토큰 삭제 완료");

			log.info("[회원탈퇴] 리프레시 토큰 삭제 시작");
			tokensRepository.deleteByUser(user);
			log.info("[회원탈퇴] 리프레시 토큰 삭제 완료");

			log.info("[회원탈퇴] 알림 삭제 시작");
			notificationRepository.deleteAllByUser(user);
			log.info("[회원탈퇴] 알림 삭제 완료");

			// 후기, 채팅 메시지
			log.info("[회원탈퇴] 후기 삭제 시작");
			reviewRepository.deleteAllByWriterOrReviewee(user, user);
			log.info("[회원탈퇴] 후기 삭제 완료");

			log.info("[회원탈퇴] 채팅 메시지 삭제 시작");
			chatsRepository.deleteAllBySender(user);
			log.info("[회원탈퇴] 채팅 메시지 삭제 완료");

			// 채팅방
			log.info("[회원탈퇴] 채팅방 조회 시작");
			List<ChatRooms> myChatRooms = chatRoomsRepository.findMyChatRooms(user.getUsersId());
			log.info("[회원탈퇴] 채팅방 조회 완료 - 개수: {}", myChatRooms.size());

			log.info("[회원탈퇴] 채팅방 삭제 시작");
			chatRoomsRepository.deleteAll(myChatRooms);
			log.info("[회원탈퇴] 채팅방 삭제 완료");

			// 거래
			log.info("[회원탈퇴] 거래 삭제 시작");
			tradesRepository.deleteAll(tradesRepository.findAllByUser(user.getUsersId()));
			log.info("[회원탈퇴] 거래 삭제 완료");

			// 물건 및 물건 사진
			log.info("[회원탈퇴] 물건 조회 시작");
			List<Goods> goodsList = goodsRepository.findByUserOrderByCreatedAtDesc(user);
			List<GoodsImages> goodsImagesList = goodsImagesRepository.findByGoodIn(goodsList);
			log.info("[회원탈퇴] 물건 조회 완료 - 물건 개수: {}, 이미지 개수: {}", goodsList.size(), goodsImagesList.size());

			log.info("[회원탈퇴] 물건 이미지 삭제 시작");
			goodsImagesRepository.deleteAll(goodsImagesList);
			log.info("[회원탈퇴] 물건 이미지 삭제 완료");

			log.info("[회원탈퇴] 물건 삭제 시작");
			goodsRepository.deleteAll(goodsList);
			log.info("[회원탈퇴] 물건 삭제 완료");

			// 사유 저장
			log.info("[회원탈퇴] 탈퇴 사유 저장 시작");
			withdrawReasonsRepository.save(new WithdrawReasons(reason, user.getUsersId()));
			log.info("[회원탈퇴] 탈퇴 사유 저장 완료");

			// 사용자 삭제
			log.info("[회원탈퇴] 사용자 삭제 시작");
			usersRepository.delete(user);
			log.info("[회원탈퇴] 사용자 삭제 완료");

			// DB 트랜잭션 이후 S3 삭제 수행
			log.info("[회원탈퇴] S3 삭제 이벤트 발행 시작");
			applicationEventPublisher.publishEvent(
				new UserWithdrawCompletedEvent(this, goodsImagesList, user));
			log.info("[회원탈퇴] S3 삭제 이벤트 발행 완료");

		} catch (Exception e) {
			log.error("[회원탈퇴 실패] 사용자 ID: {}, 오류: {}", user.getUsersId(), e.getMessage(), e);
			throw e;
		}

		log.info("[회원탈퇴 완료] 사용자 ID: {}", user.getUsersId());
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleUserWithdrawCompletedEvent(UserWithdrawCompletedEvent event) {
		try {
			awsS3Service.deleteFilesFromS3(event.getImages());
			awsS3Service.deleteFileFromS3(event.getUser().getProfileImageUrl());
		} catch (Exception e) {
			log.error("S3 이미지 삭제 실패, 추후 재처리 필요", e);
		}
	}
}
