package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  
}
