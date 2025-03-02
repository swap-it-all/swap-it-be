package com.example.swapit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.Trades;

public interface ChatRoomsRepository extends JpaRepository<ChatRooms, Long> {
	List<ChatRooms> findAllByOwnerUsersIdAndRequesterUsersId(Long ownerId, Long requesterId);

	@Query("SELECT c from ChatRooms c WHERE c.owner.id = :usersId OR c.requester.id = :usersId")
	List<ChatRooms> findByUsersId(@Param("usersId") Long usersId);

	Optional<ChatRooms> findByTrade(Trades trade);
}