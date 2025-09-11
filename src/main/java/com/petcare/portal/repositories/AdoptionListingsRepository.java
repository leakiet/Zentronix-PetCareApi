package com.petcare.portal.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.AdoptionListings;

public interface AdoptionListingsRepository extends JpaRepository<AdoptionListings, Long> {
}
