package com.petcare.portal.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.Breed;
import com.petcare.portal.entities.Species;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.AdoptionStatus;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.repositories.AdoptionListingsRepository;
import com.petcare.portal.repositories.BreedRepository;
import com.petcare.portal.repositories.SpeciesRepository;
import com.petcare.portal.repositories.UserRepository;
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
  private UserRepository userRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public AdoptionListingsResponse getAdoptionListingById(Long id) {
    try {
      AdoptionListing adoptionListing = adoptionListingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoption listing not found"));
      modelMapper.typeMap(AdoptionListing.class, AdoptionListingsResponse.class).addMappings(mapper -> {
        mapper.map(src -> src.getShelter().getId().toString(), AdoptionListingsResponse::setShelterId);
      });
      AdoptionListingsResponse response = modelMapper.map(adoptionListing, AdoptionListingsResponse.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving adoption listing", e);
    }
  }

  @Override
  public AdoptionListingsResponse createAdoptionListing(AdoptionListingsRequest adoptionListingsRequest) {
    try {
      AdoptionListing adoptionListing = new AdoptionListing();
      adoptionListing.setPetName(adoptionListingsRequest.getPetName());
      adoptionListing.setDescription(adoptionListingsRequest.getDescription());
      adoptionListing.setAge(adoptionListingsRequest.getAge());
      adoptionListing.setGender(Gender.valueOf(adoptionListingsRequest.getGender()));
      adoptionListing.setImage(adoptionListingsRequest.getImage());
      adoptionListing.setDescription(adoptionListingsRequest.getDescription());
      adoptionListing.setAdoptionStatus(AdoptionStatus.valueOf(adoptionListingsRequest.getStatus()));
      User user = userRepository.findById(Long.valueOf(adoptionListingsRequest.getShelterId())).orElseThrow(() -> new RuntimeException("User not found"));
      adoptionListing.setShelter(user);
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId()).orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListing.setBreed(breed);
      Species species = speciesRepository.findById(adoptionListingsRequest.getSpeciesId()).orElseThrow(() -> new RuntimeException("Species not found"));
      adoptionListing.setSpecies(species);
      AdoptionListing savedAdoptionListing = adoptionListingsRepository.save(adoptionListing);
      modelMapper.typeMap(AdoptionListing.class, AdoptionListingsResponse.class).addMappings(mapper -> {
        mapper.map(src -> src.getShelter().getId().toString(), AdoptionListingsResponse::setShelterId);
      });
      AdoptionListingsResponse response = modelMapper.map(savedAdoptionListing, AdoptionListingsResponse.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error creating adoption listing", e);
    }
  }

  @Override	
  public AdoptionListingsResponse updateAdoptionListing(Long id, AdoptionListingsRequest adoptionListingsRequest) {
    try {
      AdoptionListing adoptionListing = adoptionListingsRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoption listing not found"));
      adoptionListing.setPetName(adoptionListingsRequest.getPetName());
      adoptionListing.setDescription(adoptionListingsRequest.getDescription());
      adoptionListing.setAge(adoptionListingsRequest.getAge());
      adoptionListing.setGender(Gender.valueOf(adoptionListingsRequest.getGender()));
      adoptionListing.setImage(adoptionListingsRequest.getImage());
      adoptionListing.setDescription(adoptionListingsRequest.getDescription());
      adoptionListing.setAdoptionStatus(AdoptionStatus.valueOf(adoptionListingsRequest.getStatus()));
      User user = userRepository.findById(Long.valueOf(adoptionListingsRequest.getShelterId())).orElseThrow(() -> new RuntimeException("User not found"));
      adoptionListing.setShelter(user);
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId()).orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListing.setBreed(breed);
      Species species = speciesRepository.findById(adoptionListingsRequest.getSpeciesId()).orElseThrow(() -> new RuntimeException("Species not found"));
      adoptionListing.setSpecies(species);
      AdoptionListing updatedAdoptionListing = adoptionListingsRepository.save(adoptionListing);
      modelMapper.typeMap(AdoptionListing.class, AdoptionListingsResponse.class).addMappings(mapper -> {
        mapper.map(src -> src.getShelter().getId().toString(), AdoptionListingsResponse::setShelterId);
      });
      AdoptionListingsResponse response = modelMapper.map(updatedAdoptionListing, AdoptionListingsResponse.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error updating adoption listing", e);
    }
  }

  @Override
  public List<AdoptionListingsResponse> getAllAdoptionListings() {
    try {
      List<AdoptionListing> adoptionListingList = adoptionListingsRepository.findAll();
      modelMapper.typeMap(AdoptionListing.class, AdoptionListingsResponse.class).addMappings(mapper -> {
        mapper.map(src -> src.getShelter().getId().toString(), AdoptionListingsResponse::setShelterId);
      });
      return adoptionListingList.stream()
          .map(adoptionListing -> modelMapper.map(adoptionListing, AdoptionListingsResponse.class))
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
