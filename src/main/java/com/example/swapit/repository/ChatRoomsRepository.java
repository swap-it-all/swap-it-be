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

	@Query("""
		SELECT c
		FROM ChatRooms c
		  LEFT JOIN c.goods g
		  LEFT JOIN c.trade t
		  LEFT JOIN t.requestedGoods rg
		  LEFT JOIN t.targetGoods tg
		  LEFT JOIN Chats ch ON ch.chatRooms.id = c.id
		WHERE
		  (
		    c.trade IS NULL
		    AND (
		      c.inviter.id = :myId
		      OR g.user.id = :myId
		    )
		  )
		  OR
		  (
		    c.trade IS NOT NULL
		    AND (
		      rg.user.id = :myId
		      OR tg.user.id = :myId
			)
		  )
		GROUP BY c
		ORDER BY MAX(ch.createdAt) DESC
		""")
	List<ChatRooms> findMyChatRooms(@Param("myId") Long myId);

	Optional<ChatRooms> findByTrade(Trades trade);

	Optional<ChatRooms> findByGoodsAndInviter(Goods goods, Users inviter);
}
