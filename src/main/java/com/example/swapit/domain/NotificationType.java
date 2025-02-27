package com.example.swapit.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationType {
	REQUESTED("새로운 스왑 요청 도착", "새로운 스왑 요청이 도착했습니다! 확인해보세요.", "/api/user/swap/applications"),
	ACCEPTED("스왑 요청 수락", "스왑 요청이 수락되었습니다! 거래를 진행해보세요.", "/api/all/goods/%s"),
	REJECTED("스왑 요청 거절", "스왑 요청이 거절되었습니다. 다른 거래를 찾아보세요.", "/api/all/goods/%s"),
	COMPLETED("스왑 완료", "스왑이 성공적으로 완료되었습니다 🎉! 리뷰를 작성해주세요.", "//todo-chatting/%s"),
	REVIEW("리뷰 도착", "새로운 리뷰가 작성되었습니다! 확인해보세요.", "/api/user/auth/my"),
	CHAT("새로운 채팅 메세지 도착", "새로운 채팅 메세지가 도착했습니다! 지금 확인하세요.", "/api/user/chatroom/%s");

	private final String title;
	private final String body;
	private final String url;

	public String getUrl(Object... params) {
		return params.length > 0 ? String.format(this.url, params) : this.url;
	}
}