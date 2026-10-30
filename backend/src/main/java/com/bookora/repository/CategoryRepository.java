package com.bookora.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookora.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findAllByOrderByIdAsc();

}
