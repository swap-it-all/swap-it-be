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
@SQLDelete(sql = "UPDATE goods SET is_deleted = true WHERE goods_id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "goods")
public class Goods extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "goods_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "users_id", nullable = false)
	private Users user;

	@ManyToOne
	@JoinColumn(name = "categories_id", nullable = false)
	private Categories category;

	@Column(columnDefinition = "VARCHAR(20)", nullable = false)
	private String title;

	@Column(nullable = false)
	private long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GoodsQuality quality;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GoodsTradeStatus goodsTradeStatus = GoodsTradeStatus.SOLD_OUT;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;

	@Column(columnDefinition = "VARCHAR(100)", nullable = false)
	private String placeName;

	@Builder.Default
	@Column(nullable = false)
	private long viewCount = 0;
}
