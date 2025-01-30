package com.example.swapit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "users_id", columnDefinition = "BIGINT", nullable = false)
	private Long usersId;

	@Column(name = "nickname", columnDefinition = "VARCHAR(20)", nullable = false)
	private String nickname;

	@Column(name = "profile_image_url", columnDefinition = "TEXT", nullable = false)
	private String profileImageUrl;

	@Column(name = "email", columnDefinition = "VARCHAR(255)", nullable = false)
	private String email;

	@Column(name = "login_info", columnDefinition = "VARCHAR(20)", nullable = false)
	private String provider;

}
