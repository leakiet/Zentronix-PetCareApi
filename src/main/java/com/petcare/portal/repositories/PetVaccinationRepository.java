package com.petcare.portal.repositories;

import com.petcare.portal.entities.PetVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetVaccinationRepository extends JpaRepository<PetVaccination, Long> {

    @Query("SELECT pv FROM PetVaccination pv WHERE pv.pet.id = :petId AND pv.isDeleted = false")
    List<PetVaccination> findByPetIdAndNotDeleted(@Param("petId") Long petId);
}
