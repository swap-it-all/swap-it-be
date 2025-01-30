package com.example.swapit.domain;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@SQLRestriction("is_read = false")
@Table(name = "notifications")
public class Notifications extends BaseEntity {

	@Id
	@GeneratedValue
	@Column(name = "notifications_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "users_id", nullable = false)
	private Users user;

	@Column(nullable = false, columnDefinition = "VARCHAR(255)")
	private String message;

	@Builder.Default
	@Column(nullable = false)
	private boolean isRead = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private NotificationType type;
}
