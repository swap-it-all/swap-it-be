package com.example.swapit.service;

import java.util.Optional;

import com.example.swapit.domain.Users;

public interface CurrentUserService {
	Users getCurrentUser();

	Optional<Users> getCurrentUserOptional();
}