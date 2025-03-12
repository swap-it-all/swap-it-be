![wire](https://github.com/user-attachments/assets/065ab4da-3885-4a42-b4db-3bc0c11f811e)# 🔁Swap It! 물물교환 앱 서비스

## 1. 프로젝트 소개

### 1.1 프로젝트 기능 소개

### 1.2 프로젝트 기간
2025.01.17 ~

### 1.3 프로젝트 관리 페이지
[WIKI](https://round-throat-587.notion.site/Swap-It-17b1eb2dd4d18094a7d4f350ae38355c?pvs=4) | 
[이슈 관리](https://github.com/orgs/swap-it-all/projects/1/views/1)

### 1.4 팀 소개
<table width="500" align="center">
<tbody>
<tr>
<th>Pictures</th>
<td width="200" align="center">
<a href="https://github.com/HJunng">
<img src="https://avatars.githubusercontent.com/u/56528404?s=400&u=ddc1793a5c35be4bdea6b807290ec77ebea818b1&v=4" width="150" height="150">
</a>
</td>
<td width="200" align="center">
<a href="https://github.com/tmfrl99">
<img src="https://avatars.githubusercontent.com/u/93257321?v=4" width="150" height="150">
</a>
</td>
</tr>
<tr>
<th>Name</th>
<td width="100" align="center">임현정</td>
<td width="100" align="center">이슬기</td>

</tr>
<tr>
<th>Role</th>
<td width="150" align="center">
- 물건 API 개발 <br>
- 거래 API 개발 <br>
- 알림 API 개발 <br>
- 클라우드 환경 구성 <br>
- 모니터링 환경 구성 <br>
(Prometheus, Grafana) <br>
</td>
<td width="150" align="center">
- 소셜 로그인 <br>
- 거래 API 개발 <br>
- 채팅 API 개발 <br>
- 신고 API 개발 <br>
- 도메인 적용 <br>
- CI/CD(Jenkins) 도입 <br>
</td>

</tr>
<tr>
<th>GitHub</th>
<td width="150" align="center">
<a href="https://github.com/HJunng">
<img src="http://img.shields.io/badge/HJunng-green?style=social&logo=github"/>
</a>
</td>
<td width="150" align="center">
<a href="https://github.com/tmfrl99">
<img src="http://img.shields.io/badge/tmfrl99-green?style=social&logo=github"/>
</a>
</td>

</tr>
</tbody>
</table>
<br><br>

## 2. 개발환경

### 2.1 기술 스택

<div align="center">

<img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white">
<br>

<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> 
<img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
<br>

<img src="https://img.shields.io/badge/spring_boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white"> 
<img src="https://img.shields.io/badge/spring_security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"> 
<br>
<img src="https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white"> 
<img src="https://img.shields.io/badge/websoscket-ffffff?style=for-the-badge&logo=websocket&logoColor=black"> 
<img src="https://img.shields.io/badge/stomp-533313?style=for-the-badge&logo=stomp&logoColor=white">
<img src="https://img.shields.io/badge/firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=white"> 
<img src="https://img.shields.io/badge/nginx-009639?style=for-the-badge&logo=nginx&logoColor=white"> 
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> 
<br>

<img src="https://img.shields.io/badge/amazon_ec2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">
<img src="https://img.shields.io/badge/amazon_rds-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white"> 
<img src="https://img.shields.io/badge/amazon_s3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> 
<img src="https://img.shields.io/badge/amazon_elasticache-C925D1?style=for-the-badge&logo=amazonelasticache&logoColor=white"> 
<br>

<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=white">
<img src="https://img.shields.io/badge/jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white">
<img src="https://img.shields.io/badge/jacoco-800C00?style=for-the-badge&logo=jacoco&logoColor=white">
<img src="https://img.shields.io/badge/sonarqube-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white">
<img src="https://img.shields.io/badge/restdocs-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white">

</div>
<br><br>



### 2.2 시스템 아키텍처
<div align="center">
  <img src="https://github.com/user-attachments/assets/5e6f861a-d396-401c-8746-fb24866ceaf9" width="700" height="400" style="margin-bottom: 20px;">
  <br>
  <img src="https://github.com/user-attachments/assets/48382cfb-bffa-4a61-8aea-895b542d0997" width="700" height="400">
</div>

### 2.3 ERD
![erd](https://github.com/user-attachments/assets/19a5d7a1-3bd2-428f-8024-729179e05cf3)

### 2.4 파일 구조도
<details>
<summary>swapit</summary>
<div markdown="1">
📦src 
  
 ┣ 📂main   
 ┃ ┣ 📂java   
 ┃ ┃ ┗ 📂com   
 ┃ ┃ ┃ ┗ 📂example   
 ┃ ┃ ┃ ┃ ┗ 📂swapit   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂common   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂api   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ApiResponse.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ErrorResponse.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂exception   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomException.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ErrorCode.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜GlobalExceptionHandler.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂security   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂jwt   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtAuthenticationFilter.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtAuthenticationToken.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜JwtProvider.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜JwtService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomUserDetails.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomUserDetailsService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜SecurityConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂websocket   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StompHandler.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜StompPrincipal.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜WebSocketConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AppConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AwsS3Config.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FirebaseConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜QueryDslConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜RedisConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂controller   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatWebSocketController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisTestController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReportController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesController.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserController.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂domain   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dao   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TradeGoodsDao.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂dto   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂chat   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatListDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRoomAddRequestFromGoodDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRoomAddRequestFromTradeDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRoomGoodsDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRoomListResponseDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRoomResponseDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatStompRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜ChatStompResponseDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂good   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsDetailDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsImageDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsListDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜MyGoodDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂trade   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BaseTradeGoodsDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜InProgressCountDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MyGoodsDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜MyRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReceivedRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradeCountProjection.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradeMyGoodsRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesGoodsListResponseDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TradesRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Dto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmTokenDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationListDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReportDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RequesterGoodsDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Result.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewListDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewRequestDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TokenDTO.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UserNicknameDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UserPageDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UserProfileDto.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserResponseDTO.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜BaseEntity.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Categories.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRooms.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatType.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Chats.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmToken.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Goods.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsImages.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsQuality.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsTradeStatus.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationEvent.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationType.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Notifications.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Reviews.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Tokens.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradeStatus.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜Trades.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜Users.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂repository   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CategoriesRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatRoomsRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatsRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomGoodsRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomGoodsRepositoryImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmTokenRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsImagesRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TokensRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsersRepository.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂service   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂notification   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmCustomNotificationServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmNotificationService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmSimpleNotificationServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationEventListener.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationEventPublisher.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜NotificationServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AwsS3Service.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AwsS3ServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CacheService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatNotificationService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CurrentUserService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CurrentUserServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RedisServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReportService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReportServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜UsersService.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsersServiceImpl.java   
 ┃ ┃ ┃ ┃ ┃ ┗ 📜SwapitApplication.java   
 ┃ ┗ 📂resources   
 ┃ ┃ ┣ 📂static   
 ┃ ┃ ┃ ┗ 📜README.MD   
 ┃ ┃ ┣ 📜application-aws.yaml   
 ┃ ┃ ┣ 📜application-dev.yaml   
 ┃ ┃ ┣ 📜application-doc.yaml   
 ┃ ┃ ┣ 📜application-jwt.yaml   
 ┃ ┃ ┣ 📜application-mail.yaml   
 ┃ ┃ ┣ 📜application-prd.yaml   
 ┃ ┃ ┣ 📜application-test.yaml   
 ┃ ┃ ┣ 📜application.yaml   
 ┃ ┃ ┗ 📜firebase-admin.json   
 ┗ 📂test   
 ┃ ┗ 📂java   
 ┃ ┃ ┗ 📂com   
 ┃ ┃ ┃ ┗ 📂example   
 ┃ ┃ ┃ ┃ ┗ 📂swapit   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂config   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TestConfig.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂controller   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReportControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UserControllerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂docs   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthControllerDocsTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatControllerDocsTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CustomDatePreprocessor.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsControllerDocsTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationControllerDocsTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜RestDocsTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TradesControllerDocsTest.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂repository   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsRepositoryTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜TradesRepositoryTest.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂service   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📂notification   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜FcmNotificationServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationEventListenerTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜NotificationEventPublisherTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜NotificationServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AuthServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜AwsS3ServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜CacheServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ChatServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜GoodsImagesServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReportServiceImplTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜ReviewServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜TradesServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜UsersServiceTest.java   
 ┃ ┃ ┃ ┃ ┃ ┣ 📂testcontainer   
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜BaseIntegrationTest.java   
 ┃ ┃ ┃ ┃ ┃ ┗ 📜SwapitApplicationTests.java   


</div>
</details>
<br><br>

## 3. UI

### 3.1 와이어프레임
#### 물건등록
![wire](https://github.com/user-attachments/assets/5d24dd2d-8c4f-4993-9bb9-4b64669d52cf)


#### 거래
![wire2](https://github.com/user-attachments/assets/7dc85854-6044-4c60-a367-cb37c1e50edf)


#### 채팅
![wire3](https://github.com/user-attachments/assets/873fac33-a3bf-4df9-8f33-a9c92c07b3d6)



### 3.2 안드로이드 앱 화면

<br><br>


## 4. 트러블슈팅

<br><br>


## 5. 요구사항 명세서

<img width="797" alt="요구사항" src="https://github.com/user-attachments/assets/dd292020-c246-4381-80e6-d8873118004d" />

