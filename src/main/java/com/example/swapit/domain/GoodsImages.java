package com.example.swapit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class GoodsImages {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "goods_images_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "goods_id", nullable = false)
	private Goods good;

	@Column(columnDefinition = "VARCHAR(255)", nullable = false)
	private String fileName;

	@Column(columnDefinition = "VARCHAR(50)", nullable = false)
	private String contentType;
}