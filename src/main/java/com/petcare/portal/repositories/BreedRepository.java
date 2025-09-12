package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.Breed;

public interface BreedRepository extends JpaRepository<Breed, Long> {
}
