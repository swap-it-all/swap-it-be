package com.example.swapit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.swapit.domain.ChatRooms;
import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Trades;
import com.example.swapit.domain.Users;

public interface ChatRoomsRepository extends JpaRepository<ChatRooms, Long> {
	// List<ChatRooms> findAllByOwnerUsersIdAndRequesterUsersId(Long ownerId, Long requesterId);

	@Query("SELECT c from ChatRooms c WHERE c.inviter.id = :usersId OR c.goods.user.id = :usersId")
	List<ChatRooms> findByUsersId(@Param("usersId") Long usersId);

	@Query("""
		SELECT c 
		FROM ChatRooms c
		  LEFT JOIN c.trade t
		  LEFT JOIN t.requestedGoods rg
		  LEFT JOIN t.targetGoods tg
		WHERE 
		  (
		    c.trade IS NULL 
		    AND c.inviter.id = :myId
		  )
		  OR
		  (
		    c.trade IS NOT NULL 
		    AND (
		      rg.user.id = :myId
		      OR tg.user.id = :myId
		    )
		  )
		""")
	List<ChatRooms> findMyChatRooms(@Param("myId") Long myId);

	Optional<ChatRooms> findByTrade(Trades trade);

	Optional<ChatRooms> findByGoodsAndInviter(Goods goods, Users inviter);
}