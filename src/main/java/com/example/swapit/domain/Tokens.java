package com.example.swapit.domain;

import java.sql.Timestamp;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name = "tokens")
@EntityListeners(AuditingEntityListener.class)
public class Tokens {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tokens_id")
	private long id;

	@OneToOne
	@JoinColumn(name = "users_id", columnDefinition = "BIGINT", nullable = false)
	private Users user;

	@Setter
	@Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
	private String refreshToken;

	@CreatedDate
	@Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false)
	private Timestamp createdAt;

	@Setter
	@Column(name = "expires_at", columnDefinition = "TIMESTAMP", nullable = false)
	private Timestamp expiresAt;

	public Tokens(Users user, String refreshToken, Timestamp expiresAt) {
		this.user = user;
		this.refreshToken = refreshToken;
		this.expiresAt = expiresAt;
	}
}
