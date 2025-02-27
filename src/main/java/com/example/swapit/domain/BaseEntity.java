package com.example.swapit.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // Auditing 활성화
public abstract class BaseEntity {

	@CreatedDate // 생성 시 자동으로 값 설정
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate // 수정 시 자동으로 값 갱신
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Column(nullable = false)
	private boolean isDeleted = false;
}