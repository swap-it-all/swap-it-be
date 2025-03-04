package com.example.swapit.domain;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE reviews SET is_deleted = true WHERE reviews_id = ?")
@Table(
	name = "reviews",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "unique_trade_writer",
			columnNames = {"trade_id", "writer_id"}
		)
	}
)
public class Reviews extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reviews_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "writer_id", nullable = false)
	private Users writer; // 리뷰 작성자

	@ManyToOne
	@JoinColumn(name = "reviewee_id", nullable = false)
	private Users reviewee; // 리뷰 받는 사람

	@ManyToOne
	@JoinColumn(name = "trade_id", nullable = false)
	private Trades trade; // 거래 정보

	@Column(nullable = false)
	private String content;

	@Column
	private double rating;
}