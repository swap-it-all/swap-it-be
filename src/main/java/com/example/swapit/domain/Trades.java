package com.example.swapit.domain;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
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
	@GeneratedValue
	@Column(name = "trades_id")
	private long id;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TradeStatus status = TradeStatus.PENDING;

	@ManyToOne
	@JoinColumn(name = "requested_goods_id", nullable = false)
	private Goods requestedGood;

	@ManyToOne
	@JoinColumn(name = "target_goods_id", nullable = false)
	private Goods targetGood;
}
