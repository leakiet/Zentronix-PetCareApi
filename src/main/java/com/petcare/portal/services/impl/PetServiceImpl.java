package com.petcare.portal.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;
import com.petcare.portal.entities.Breed;
import com.petcare.portal.services.PetService;
import com.petcare.portal.repositories.PetRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.repositories.BreedRepository;
import com.petcare.portal.dtos.petDto.createRequest;
import com.petcare.portal.dtos.petDto.updateRequest;
import com.petcare.portal.enums.Species;
import com.petcare.portal.enums.Gender;

@Service
public class PetServiceImpl implements PetService {

  @Autowired
  private PetRepository petRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BreedRepository breedRepository;

  @Override
  public Pet createPet(createRequest request) {
    try {
      // Find the user (owner)
      User owner = userRepository.findById(request.getUserId())
          .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
      
      // Find the breed
      Breed breed = breedRepository.findById(request.getBreedId())
          .orElseThrow(() -> new RuntimeException("Breed not found with id: " + request.getBreedId()));
      
      // Create new pet
      Pet pet = new Pet();
      pet.setPetName(request.getPetName());
      pet.setSpecies(Species.valueOf(request.getSpecies()));
      pet.setBreed(breed);
      pet.setAge(request.getAge().toString());
      pet.setWeight(request.getWeight().toString());
      pet.setColor(request.getColor());
      pet.setGender(Gender.valueOf(request.getGender()));
      pet.setOwner(owner);
      
      return petRepository.save(pet);
    } catch (Exception e) {
      throw new RuntimeException("Error creating pet: " + e.getMessage(), e);
    }
  }

  @Override
  public Pet savePet(Pet pet) {
    // Here you would typically save the pet to a database
    Pet savedPet = petRepository.save(pet); // Assuming petRepository is defined and injected
    return savedPet;
  }

  @Override
  public List<Pet> getPetsByCustomerId(Long userId) {
    try {
      User owner = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
      return petRepository.findByOwner(owner);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid user ID format: " + userId, e);
    }
  }

  @Override
  public Pet getPetById(Long id) {
    return petRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
  }

  @Override
  public Pet updatePet(Long petId, updateRequest request) {
    try {
      // Check if the pet exists
      Pet existingPet = getPetById(petId);
      
      // Find the breed if breedId is provided
      if (request.getBreedId() != null) {
        Breed breed = breedRepository.findById(request.getBreedId())
            .orElseThrow(() -> new RuntimeException("Breed not found with id: " + request.getBreedId()));
        existingPet.setBreed(breed);
      }
      
      // Update the pet details
      if (request.getPetName() != null) {
        existingPet.setPetName(request.getPetName());
      }
      if (request.getSpecies() != null) {
        existingPet.setSpecies(Species.valueOf(request.getSpecies()));
      }
      if (request.getAge() != null) {
        existingPet.setAge(request.getAge().toString());
      }
      if (request.getWeight() != null) {
        existingPet.setWeight(request.getWeight().toString());
      }
      if (request.getColor() != null) {
        existingPet.setColor(request.getColor());
      }
      if (request.getGender() != null) {
        existingPet.setGender(Gender.valueOf(request.getGender()));
      }
      
      // Save the updated pet
      return petRepository.save(existingPet);
    } catch (Exception e) {
      throw new RuntimeException("Error updating pet: " + e.getMessage(), e);
    }
  }

}

