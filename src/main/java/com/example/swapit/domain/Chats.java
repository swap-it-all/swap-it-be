package com.example.swapit.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chats")
public class Chats {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chats_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "chatrooms_id", nullable = false)
	private ChatRooms chatRooms;

	@ManyToOne
	@JoinColumn(name = "sender_id", nullable = false)
	private Users sender;

	@Enumerated(EnumType.STRING)
	@Column(name = "chat_type", nullable = false)
	private ChatType chatType;

	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "goods_id")
	private Long goodsId;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Chats(ChatRooms chatRooms, Users sender, ChatType chatType, String content, Long goodsId) {
		this.chatRooms = chatRooms;
		this.sender = sender;
		this.chatType = chatType;
		this.content = content;
		this.goodsId = goodsId;
	}
}
