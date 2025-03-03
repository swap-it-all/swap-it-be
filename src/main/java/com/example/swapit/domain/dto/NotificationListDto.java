package com.example.swapit.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationListDto {
	private List<NotificationDto> notifications;
}