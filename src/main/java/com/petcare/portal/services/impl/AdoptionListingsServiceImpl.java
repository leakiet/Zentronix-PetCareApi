package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.entities.AdoptionListings;
import com.petcare.portal.entities.Breed;
import com.petcare.portal.entities.Species;
import com.petcare.portal.enums.GenderPet;
import com.petcare.portal.enums.StatusAdoptionListings;
import com.petcare.portal.repositories.AdoptionListingsRepository;
import com.petcare.portal.repositories.BreedRepository;
import com.petcare.portal.repositories.SpeciesRepository;
import com.petcare.portal.services.AdoptionListingsService;

@Service
public class AdoptionListingsServiceImpl implements AdoptionListingsService {

  @Autowired
  private AdoptionListingsRepository adoptionListingsRepository;

  @Autowired
  private BreedRepository breedRepository;

  @Autowired
  private SpeciesRepository speciesRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public AdoptionListingsResponse getAdoptionListingById(Long id) {
    try {
      AdoptionListings adoptionListings = adoptionListingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoption listing not found"));
      AdoptionListingsResponse response = modelMapper.map(adoptionListings, AdoptionListingsResponse.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving adoption listing", e);
    }
  }

  @Override
  public AdoptionListingsResponse createAdoptionListing(AdoptionListingsRequest adoptionListingsRequest) {
    try {
      AdoptionListings adoptionListings = new AdoptionListings();
      adoptionListings.setName(adoptionListingsRequest.getName());
      adoptionListings.setDescription(adoptionListingsRequest.getDescription());
      adoptionListings.setAge(adoptionListingsRequest.getAge());
      adoptionListings.setGenderPet(GenderPet.valueOf(adoptionListingsRequest.getGenderPet()));
      adoptionListings.setImage(adoptionListingsRequest.getImage());
      adoptionListings.setShelterId(adoptionListingsRequest.getShelterId());
      adoptionListings.setStatus(StatusAdoptionListings.valueOf(adoptionListingsRequest.getStatus()));
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId()).orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListings.setBreed(breed);
      Species species = speciesRepository.findById(adoptionListingsRequest.getSpeciesId()).orElseThrow(() -> new RuntimeException("Species not found"));
      adoptionListings.setSpecies(species);
      AdoptionListings savedAdoptionListings = adoptionListingsRepository.save(adoptionListings);
      AdoptionListingsResponse response = modelMapper.map(savedAdoptionListings, AdoptionListingsResponse.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error creating adoption listing", e);
    }
  }

  @Override
  public AdoptionListingsResponse updateAdoptionListing(Long id, AdoptionListingsRequest adoptionListingsRequest) {
    try {
      AdoptionListings adoptionListings = adoptionListingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoption listing not found"));
      adoptionListings.setName(adoptionListingsRequest.getName());
      adoptionListings.setDescription(adoptionListingsRequest.getDescription());
      adoptionListings.setAge(adoptionListingsRequest.getAge());
      adoptionListings.setGenderPet(GenderPet.valueOf(adoptionListingsRequest.getGenderPet()));
      adoptionListings.setImage(adoptionListingsRequest.getImage());
      adoptionListings.setShelterId(adoptionListingsRequest.getShelterId());
      adoptionListings.setStatus(StatusAdoptionListings.valueOf(adoptionListingsRequest.getStatus()));
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId()).orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListings.setBreed(breed);
      Species species = speciesRepository.findById(adoptionListingsRequest.getSpeciesId()).orElseThrow(() -> new RuntimeException("Species not found"));
      adoptionListings.setSpecies(species);
      AdoptionListings updatedAdoptionListings = adoptionListingsRepository.save(adoptionListings);
      AdoptionListingsResponse response = modelMapper.map(updatedAdoptionListings, AdoptionListingsResponse.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error updating adoption listing", e);
    }
  }

  @Override
  public List<AdoptionListingsResponse> getAllAdoptionListings() {
    try {
      List<AdoptionListings> adoptionListingsList = adoptionListingsRepository.findAll();
      return adoptionListingsList.stream()
          .map(adoptionListings -> modelMapper.map(adoptionListings, AdoptionListingsResponse.class))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving adoption listings", e);
    }
  }

  @Override
  public void deleteAdoptionListing(Long id) {
    try {
      if (!adoptionListingsRepository.existsById(id)) {
        throw new RuntimeException("Adoption listing not found");
      }
      adoptionListingsRepository.deleteById(id);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting adoption listing", e);
    }
  }
  
}
