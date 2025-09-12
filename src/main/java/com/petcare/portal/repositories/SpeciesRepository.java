package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.Species;

public interface SpeciesRepository extends JpaRepository<Species, Long> {
  
}
