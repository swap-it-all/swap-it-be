package com.example.swapit.domain.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRoomResponseDto {
	private Long usersId;
	private String profileImageUrl;
	private String nickname;
	private String recentChat;
	private LocalDateTime createdAt;
}
