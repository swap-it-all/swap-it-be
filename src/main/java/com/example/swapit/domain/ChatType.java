package com.example.swapit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatType {
	TALK(""),
	REQUEST("스왑을 요청했어요!"),
	CANCEL("스왑을 취소했어요."),
	ACCEPT("스왑을 수락했어요!"),
	REJECT("스왑을 거절했어요."),
	COMPLETE("스왑을 완료했어요!");

	private final String message;
}
