package com.example.swapit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chatrooms",
	uniqueConstraints = @UniqueConstraint(columnNames = {"goods_id, inviter_id"})
)
public class ChatRooms {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chatrooms_id")
	private Long id;

	@Setter
	@OneToOne
	@JoinColumn(name = "trades_id")
	private Trades trade;

	@ManyToOne
	@JoinColumn(name = "goods_id", nullable = false)
	private Goods goods;

	@ManyToOne
	@JoinColumn(name = "inviter_id", nullable = false)
	private Users inviter;

	@Setter
	@Column(name = "inviter_last_read_id", nullable = false)
	private Long inviterLastReadId;

	@Setter
	@Column(name = "not_inviter_last_read_id", nullable = false)
	private Long notInviterLastReadId;

	public ChatRooms(Goods goods, Users inviter, Trades trade) {
		this.goods = goods;
		this.inviter = inviter;
		this.trade = trade;
		this.inviterLastReadId = 0L;
		this.notInviterLastReadId = 0L;
	}

	public void updateTrade(Trades trade) {
		this.trade = trade;
	}
}