package com.example.swapit.config.websocket;

import java.security.Principal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StompPrincipal implements Principal {
	private final String id;

	@Override
	public String getName() {
		return id;
	}
}