package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.entities.Pet;
import com.petcare.portal.dtos.petDto.createRequest;
import com.petcare.portal.dtos.petDto.updateRequest;

public interface PetService {
  Pet savePet(Pet pet);

  Pet createPet(createRequest request);

  List<Pet> getPetsByCustomerId(Long userId);

  Pet getPetById(Long id);

  Pet updatePet(Long petId, updateRequest request);
}
