package com.example.swapit.domain;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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
import jakarta.persistence.UniqueConstraint;
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
	uniqueConstraints = @UniqueConstraint(columnNames = {"target_goods_id, requested_goods_id"})
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

	@Formula("(SELECT g.users_id FROM goods g WHERE g.goods_id = requested_goods_id)")
	private Long requesterId;

	@Formula("(SELECT g.users_id FROM goods g WHERE g.goods_id = target_goods_id)")
	private Long ownerId;

	public Trades(Goods requestedGoods, Goods targetGoods) {
		this.status = TradeStatus.PENDING;
		this.requestedGoods = requestedGoods;
		this.targetGoods = targetGoods;
	}
}