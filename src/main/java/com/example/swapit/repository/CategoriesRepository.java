package com.example.swapit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.swapit.domain.Categories;

public interface CategoriesRepository extends JpaRepository<Categories, Long> {
}
