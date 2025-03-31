package com.example.swapit.domain;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE trades SET is_deleted = true WHERE trades_id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "trades",
	indexes = {
		@Index(name = "idx_target_requested_deleted", columnList = "requested_goods_id, target_goods_id, is_deleted")
	}
)
public class Trades extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "trades_id")
	private Long id;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TradeStatus status;

	@ManyToOne
	@JoinColumn(name = "requested_goods_id", nullable = false)
	private Goods requestedGoods;

	@ManyToOne
	@JoinColumn(name = "target_goods_id", nullable = false)
	private Goods targetGoods;

	public Trades(Goods requestedGoods, Goods targetGoods) {
		this.status = TradeStatus.PENDING;
		this.requestedGoods = requestedGoods;
		this.targetGoods = targetGoods;
	}
}