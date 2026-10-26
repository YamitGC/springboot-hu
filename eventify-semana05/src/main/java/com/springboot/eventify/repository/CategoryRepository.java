package com.springboot.eventify.repository;

import com.springboot.eventify.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByNombreAsc();
}
