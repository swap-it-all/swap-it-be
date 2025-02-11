package com.example.swapit.domain;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE trades SET is_deleted = true WHERE trades_id = ?")
@Table(name = "trades")
public class Trades extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trades_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TradeStatus status;

	@ManyToOne
	@JoinColumn(name = "requested_goods_id", nullable = false)
	private Goods requestedGoods;

	@ManyToOne
	@JoinColumn(name = "target_goods_id", nullable = false)
	private Goods targetGoods;

	@ManyToOne
	@JoinColumn(name = "requester_id", nullable = false)
	private Users requester;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private Users owner;

	public Trades(Goods requestedGoods, Goods targetGoods) {
		this.status = TradeStatus.PENDING;
		this.requestedGoods = requestedGoods;
		this.targetGoods = targetGoods;
		this.requester = requestedGoods.getUser();
		this.owner = targetGoods.getUser();
	}
}
