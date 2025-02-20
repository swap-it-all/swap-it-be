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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chatrooms")
public class ChatRooms {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chatrooms_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "goods_id", nullable = false)
	private Goods goods;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Users owner;

	@ManyToOne
	@JoinColumn(name = "requester_id", nullable = false)
	private Users requester;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public ChatRooms(Goods goods, Users owner, Users requester) {
		this.goods = goods;
		this.owner = owner;
		this.requester = requester;
	}
}
