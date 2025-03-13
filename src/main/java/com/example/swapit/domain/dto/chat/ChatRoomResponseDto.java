package com.example.swapit.domain.dto.chat;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRoomResponseDto {
	private String profileImageUrl;
	private String nickname;
	private String recentChat;
	private LocalDateTime recentChatTime;
	private long unReadChatCount;
}
