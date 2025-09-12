package com.petcare.portal.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {
	 List<Pet> findByOwnerId(Long ownerId);
	 List<Pet> findBySpecies(String species);
	 List<Pet> findByBreed(String breed);
	 List<Pet> findByAge(String age);
	 List<Pet> findByGender(com.petcare.portal.enums.Gender gender);
}
