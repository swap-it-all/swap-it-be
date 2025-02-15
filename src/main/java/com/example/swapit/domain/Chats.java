package com.example.swapit.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
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

	@Column(name = "chat_type", nullable = false)
	private ChatType chatType;

	@Column(name = "content", nullable = false)
	private String content;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
