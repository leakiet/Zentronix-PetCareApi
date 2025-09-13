package com.petcare.portal.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.enums.Species;

public interface AdoptionListingsRepository extends JpaRepository<AdoptionListing, Long> {
  List<AdoptionListing> findByShelterId(Long shelterId);

  @Query("SELECT a FROM AdoptionListing a WHERE " +
      "(:species IS NULL OR a.species = :species) AND " +
      "(:breedId IS NULL OR a.breed.id = :breedId) AND " +
      "(:gender IS NULL OR a.gender = :gender) AND " +
      "(:minAge IS NULL OR a.age >= :minAge) AND " +
      "(:maxAge IS NULL OR a.age <= :maxAge) AND " +
      "a.adoptionStatus = 'PENDING'")
  Page<AdoptionListing> findFilteredAdoptionListings(
      @Param("species") Species species,
      @Param("breedId") Long breedId,
      @Param("gender") Gender gender,
      @Param("minAge") Integer minAge,
      @Param("maxAge") Integer maxAge,
      Pageable pageable);
}
