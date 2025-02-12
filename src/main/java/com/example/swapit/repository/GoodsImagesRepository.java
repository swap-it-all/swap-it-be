package com.example.swapit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.GoodsImages;

public interface GoodsImagesRepository extends JpaRepository<GoodsImages, Long> {
	List<GoodsImages> findByGood(Goods good);
}