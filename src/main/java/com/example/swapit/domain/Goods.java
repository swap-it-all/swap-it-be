package com.example.swapit.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.example.swapit.domain.dto.good.GoodsRequestDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	@Column(columnDefinition = "VARCHAR(20)", nullable = false)
	private String title;

	@Column(nullable = false)
	private long price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GoodsQuality quality;

	@ManyToOne
	@JoinColumn(name = "categories_id", nullable = false)
	private Categories category;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GoodsTradeStatus goodsTradeStatus;

	@Column(columnDefinition = "VARCHAR(100)", nullable = false)
	private String placeName;

	@Column(nullable = false)
	private long viewCount = 0;

	@OneToMany(mappedBy = "good", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GoodsImages> goodsImagesList;

	@Builder
	public Goods(Users user, String title, long price, GoodsQuality quality, Categories category, String content,
		String placeName) {
		this.user = user;
		this.title = title;
		this.price = price;
		this.quality = quality;
		this.category = category;
		this.content = content;
		this.placeName = placeName;
		this.viewCount = 0;
		this.goodsTradeStatus = GoodsTradeStatus.AVAILABLE;
		this.goodsImagesList = new ArrayList<>();
	}

	public void update(GoodsRequestDto request, Categories category) {
		this.title = request.getTitle();
		this.price = request.getPrice();
		this.quality = GoodsQuality.valueOf(request.getQuality().toUpperCase());
		this.category = category;
		this.content = request.getContent();
		this.placeName = request.getPlaceName();
	}

	public void incrementViewCount() {
		this.viewCount++;
	}
}