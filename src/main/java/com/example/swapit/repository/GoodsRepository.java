package com.example.swapit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Goods;
import com.example.swapit.domain.Users;

public interface GoodsRepository extends JpaRepository<Goods, Long>, CustomGoodsRepository {

	List<Goods> findByUserOrderByCreatedAtDesc(Users user);
}