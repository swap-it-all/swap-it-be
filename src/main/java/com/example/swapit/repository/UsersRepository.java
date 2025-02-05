package com.example.swapit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
}
