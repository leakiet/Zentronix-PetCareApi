package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
  
}
