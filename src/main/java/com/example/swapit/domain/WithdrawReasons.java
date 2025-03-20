package com.example.swapit.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "withdraw_reasons")
@EntityListeners(AuditingEntityListener.class)
public class WithdrawReasons {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reasons_id", columnDefinition = "BIGINT", nullable = false)
	private Long id;

	@Column(name = "reason", columnDefinition = "VARCHAR(200)", nullable = false)
	private String reason;

	@Column(name = "users_id", columnDefinition = "BIGINT", nullable = false)
	private Long usersId;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public WithdrawReasons(String reason, Long usersId) {
		this.reason = reason;
		this.usersId = usersId;
	}
}
