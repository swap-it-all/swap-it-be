package com.example.swapit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Goods;

public interface GoodsRepository extends JpaRepository<Goods, Long>, CustomGoodsRepository {
}