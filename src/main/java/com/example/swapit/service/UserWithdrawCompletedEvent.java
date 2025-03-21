package com.example.swapit.service;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import com.example.swapit.domain.GoodsImages;
import com.example.swapit.domain.Users;

import lombok.Getter;

@Getter
public class UserWithdrawCompletedEvent extends ApplicationEvent {
	private final List<GoodsImages> images;
	private final Users user;

	public UserWithdrawCompletedEvent(Object source, List<GoodsImages> images, Users user) {
		super(source);
		this.images = images;
		this.user = user;
	}
}
