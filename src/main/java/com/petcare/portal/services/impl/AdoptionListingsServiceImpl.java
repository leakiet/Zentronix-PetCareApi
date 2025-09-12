package com.petcare.portal.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.Breed;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.AdoptionStatus;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.enums.PetHealthStatus;
import com.petcare.portal.enums.Species;
import com.petcare.portal.repositories.AdoptionListingsRepository;
import com.petcare.portal.repositories.BreedRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.AdoptionListingsService;

@Service
public class AdoptionListingsServiceImpl implements AdoptionListingsService {

  @Autowired
  private AdoptionListingsRepository adoptionListingsRepository;

  @Autowired
  private BreedRepository breedRepository;

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
      adoptionListing.setAdoptionStatus(AdoptionStatus.valueOf(adoptionListingsRequest.getAdoptionStatus()));
      adoptionListing.setStatus(PetHealthStatus.valueOf(adoptionListingsRequest.getStatus()));
      User user = userRepository.findById(Long.valueOf(adoptionListingsRequest.getShelterId())).orElseThrow(() -> new RuntimeException("User not found"));
      adoptionListing.setShelter(user);
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId()).orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListing.setBreed(breed);
      adoptionListing.setSpecies(Species.valueOf(adoptionListingsRequest.getSpecies()));
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
      adoptionListing.setAdoptionStatus(AdoptionStatus.valueOf(adoptionListingsRequest.getAdoptionStatus()));
      adoptionListing.setStatus(PetHealthStatus.valueOf(adoptionListingsRequest.getStatus()));
      User user = userRepository.findById(Long.valueOf(adoptionListingsRequest.getShelterId())).orElseThrow(() -> new RuntimeException("User not found"));
      adoptionListing.setShelter(user);
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId()).orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListing.setBreed(breed);
      adoptionListing.setSpecies(Species.valueOf(adoptionListingsRequest.getSpecies()));
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
    public Page<AdoptionListingsResponse> getAllAdoptionListings(Pageable pageable, String species, Long breedId, String gender, Integer minAge, Integer maxAge) {
      try {
        System.out.println("Debug: species=" + species + ", breedId=" + breedId + ", gender=" + gender + ", minAge=" + minAge + ", maxAge=" + maxAge);
        Species speciesEnum = species != null ? Species.valueOf(species.toUpperCase()) : null;
        Gender genderEnum = null;
        if (gender != null) {
          try {
            genderEnum = Gender.valueOf(gender.toUpperCase());
          } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid gender value: " + gender, e);
          }
        }
        Page<AdoptionListing> adoptionListingPage = adoptionListingsRepository.findFilteredAdoptionListings(speciesEnum, breedId, genderEnum, minAge, maxAge, pageable);
        modelMapper.typeMap(AdoptionListing.class, AdoptionListingsResponse.class).addMappings(mapper -> {
          mapper.map(src -> src.getShelter().getId().toString(), AdoptionListingsResponse::setShelterId);
        });
        return adoptionListingPage.map(adoptionListing -> modelMapper.map(adoptionListing, AdoptionListingsResponse.class));
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
