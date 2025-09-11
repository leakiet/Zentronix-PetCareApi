package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.BreedDto.BreedRequest;
import com.petcare.portal.dtos.BreedDto.BreedResponse;
import com.petcare.portal.entities.Breed;
import com.petcare.portal.enums.Species;
import com.petcare.portal.repositories.BreedRepository;
import com.petcare.portal.services.BreedService;

@Service
public class BreedServiceImpl implements BreedService {

  @Autowired
  private BreedRepository breedRepository;

  @Override
  public BreedResponse getBreedById(Long id) {
    try {
      Breed breed = breedRepository.findById(id).orElseThrow(() -> new RuntimeException("Breed not found"));
      BreedResponse response = new BreedResponse();
      response.setId(breed.getId());
      response.setName(breed.getName());
      response.setSpecies(breed.getSpecies().name());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving breed", e);
    }
  }

  @Override
  public BreedResponse createBreed(BreedRequest breedRequest) {
    try {
      Breed breed = new Breed();
      breed.setName(breedRequest.getName());
      breed.setSpecies(Species.valueOf(breedRequest.getSpecies()));
      Breed savedBreed = breedRepository.save(breed);
      BreedResponse response = new BreedResponse();
      response.setId(savedBreed.getId());
      response.setName(savedBreed.getName());
      response.setSpecies(savedBreed.getSpecies().name());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error creating breed", e);
    }
  }

  @Override
  public BreedResponse updateBreed(Long id, BreedRequest breedRequest) {
    try {
      Breed breed = breedRepository.findById(id).orElseThrow(() -> new RuntimeException("Breed not found"));
      breed.setName(breedRequest.getName());
      breed.setSpecies(Species.valueOf(breedRequest.getSpecies()));
      Breed updatedBreed = breedRepository.save(breed);
      BreedResponse response = new BreedResponse();
      response.setId(updatedBreed.getId());
      response.setName(updatedBreed.getName());
      response.setSpecies(updatedBreed.getSpecies().name());
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error updating breed", e);
    }
  }

  @Override
  public List<BreedResponse> getAllBreeds() {
    try {
      List<Breed> breedList = breedRepository.findAll();
      return breedList.stream()
          .map(breed -> {
            BreedResponse response = new BreedResponse();
            response.setId(breed.getId());
            response.setName(breed.getName());
            response.setSpecies(breed.getSpecies().name());
            return response;
          })
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving breeds", e);
    }
  }

  @Override
  public void deleteBreed(Long id) {
    try {
      if (!breedRepository.existsById(id)) {
        throw new RuntimeException("Breed not found");
      }
      breedRepository.deleteById(id);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting breed", e);
    }
  }
}
