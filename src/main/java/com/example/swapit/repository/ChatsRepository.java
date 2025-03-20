package com.example.swapit.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.swapit.domain.Chats;
import com.example.swapit.domain.Users;

public interface ChatsRepository extends JpaRepository<Chats, Long> {

	Chats findTopByChatRoomsIdOrderByCreatedAtDesc(Long chatRoomsId);

	@Query("SELECT c FROM Chats c "
		   + "WHERE c.chatRooms.id = :chatRoomsId "
		   + "AND (:createdAt IS NULL OR c.createdAt < :createdAt OR (c.createdAt = :createdAt AND c.id < :cursorId)) "
		   + "ORDER BY c.createdAt DESC")
	List<Chats> findAllByChatRoomsId(@Param("chatRoomsId") Long chatRoomsId,
		@Param("cursorId") Long cursorId,
		@Param("createdAt") LocalDateTime createdAt);

	long countByChatRoomsId(Long chatroomId);

	@Query("SELECT COUNT(*) "
		   + "FROM Chats c "
		   + "WHERE c.chatRooms.id = :chatRoomsId "
		   + "AND c.id > :lastReadId")
	long findUnreadChatCount(long chatRoomsId, long lastReadId);

	@Modifying
	void deleteAllBySender(Users sender);
}
