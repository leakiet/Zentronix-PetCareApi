package com.petcare.portal.services.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.dtos.AdoptionListingsDto.ShelterAdoptionResponse;
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
      AdoptionListing adoptionListing = adoptionListingsRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Adoption listing not found"));
      AdoptionListingsResponse response = new AdoptionListingsResponse();
      response.setId(adoptionListing.getId());
      response.setImage(adoptionListing.getImage());
      response.setPetName(adoptionListing.getPetName());
      response.setDescription(adoptionListing.getDescription());
      // Null checks for enum fields
      response.setGender(adoptionListing.getGender() != null ? adoptionListing.getGender().toString() : null);
      response.setBreed(adoptionListing.getBreed());
      response.setSpecies(adoptionListing.getSpecies() != null ? adoptionListing.getSpecies().toString() : null);
      response.setStatus(adoptionListing.getStatus() != null ? adoptionListing.getStatus().toString() : null);
      response.setAdoptionStatus(
          adoptionListing.getAdoptionStatus() != null ? adoptionListing.getAdoptionStatus().toString() : null);
      response.setAge(adoptionListing.getAge());
      ShelterAdoptionResponse shelterDto = new ShelterAdoptionResponse();
      if (adoptionListing.getShelter() != null) {
        shelterDto.setId(adoptionListing.getShelter().getId());
        shelterDto.setFirstName(adoptionListing.getShelter().getFirstName());
        shelterDto.setLastName(adoptionListing.getShelter().getLastName());
        shelterDto.setCompanyName(adoptionListing.getShelter().getCompanyName());
        shelterDto.setPhone(adoptionListing.getShelter().getPhone());
        shelterDto.setEmail(adoptionListing.getShelter().getEmail());
        shelterDto.setGender(
            adoptionListing.getShelter().getGender() != null ? adoptionListing.getShelter().getGender().toString()
                : null);
        shelterDto.setAddress(adoptionListing.getShelter().getAddress());
      } else {
        shelterDto.setId(null);
        shelterDto.setFirstName(null);
        shelterDto.setLastName(null);
        shelterDto.setCompanyName(null);
        shelterDto.setPhone(null);
        shelterDto.setEmail(null);
        shelterDto.setGender(null);
        shelterDto.setAddress(null);
      }
      response.setShelter(shelterDto);
      return response;
    } catch (Exception e) {
      System.err.println("Error in getAdoptionListingById service: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Error retrieving adoption listing: " + e.getMessage(), e);
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
      User user = userRepository.findById(Long.valueOf(adoptionListingsRequest.getShelterId()))
          .orElseThrow(() -> new RuntimeException("User not found"));
      adoptionListing.setShelter(user);
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId())
          .orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListing.setBreed(breed);
      adoptionListing.setSpecies(Species.valueOf(adoptionListingsRequest.getSpecies()));
      AdoptionListing savedAdoptionListing = adoptionListingsRepository.save(adoptionListing);
      AdoptionListingsResponse response = new AdoptionListingsResponse();
      response.setId(savedAdoptionListing.getId());
      response.setImage(savedAdoptionListing.getImage());
      response.setPetName(savedAdoptionListing.getPetName());
      response.setDescription(savedAdoptionListing.getDescription());
      response.setGender(savedAdoptionListing.getGender().toString());
      response.setBreed(savedAdoptionListing.getBreed());
      response.setSpecies(savedAdoptionListing.getSpecies().toString());
      response.setStatus(savedAdoptionListing.getStatus().toString());
      response.setAdoptionStatus(savedAdoptionListing.getAdoptionStatus().toString());
      response.setAge(savedAdoptionListing.getAge());
      ShelterAdoptionResponse shelterDto = modelMapper.map(adoptionListing.getShelter(),
          ShelterAdoptionResponse.class);
      response.setShelter(shelterDto);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error creating adoption listing", e);
    }
  }

  @Override
  public AdoptionListingsResponse updateAdoptionListing(Long id, AdoptionListingsRequest adoptionListingsRequest) {
    try {
      AdoptionListing adoptionListing = adoptionListingsRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Adoption listing not found"));
      adoptionListing.setPetName(adoptionListingsRequest.getPetName());
      adoptionListing.setDescription(adoptionListingsRequest.getDescription());
      adoptionListing.setAge(adoptionListingsRequest.getAge());
      adoptionListing.setGender(Gender.valueOf(adoptionListingsRequest.getGender()));
      adoptionListing.setImage(adoptionListingsRequest.getImage());
      adoptionListing.setAdoptionStatus(AdoptionStatus.valueOf(adoptionListingsRequest.getAdoptionStatus()));
      adoptionListing.setStatus(PetHealthStatus.valueOf(adoptionListingsRequest.getStatus()));
      User user = userRepository.findById(Long.valueOf(adoptionListingsRequest.getShelterId()))
          .orElseThrow(() -> new RuntimeException("User not found"));
      adoptionListing.setShelter(user);
      Breed breed = breedRepository.findById(adoptionListingsRequest.getBreedId())
          .orElseThrow(() -> new RuntimeException("Breed not found"));
      adoptionListing.setBreed(breed);
      adoptionListing.setSpecies(Species.valueOf(adoptionListingsRequest.getSpecies()));
      AdoptionListing updatedAdoptionListing = adoptionListingsRepository.save(adoptionListing);
      AdoptionListingsResponse response = new AdoptionListingsResponse();
      response.setId(updatedAdoptionListing.getId());
      response.setImage(updatedAdoptionListing.getImage());
      response.setPetName(updatedAdoptionListing.getPetName());
      response.setDescription(updatedAdoptionListing.getDescription());
      response.setGender(updatedAdoptionListing.getGender().toString());
      response.setBreed(updatedAdoptionListing.getBreed());
      response.setSpecies(updatedAdoptionListing.getSpecies().toString());
      response.setStatus(updatedAdoptionListing.getStatus().toString());
      response.setAdoptionStatus(updatedAdoptionListing.getAdoptionStatus().toString());
      response.setAge(updatedAdoptionListing.getAge());
      ShelterAdoptionResponse shelterDto = modelMapper.map(adoptionListing.getShelter(),
          ShelterAdoptionResponse.class);
      response.setShelter(shelterDto);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error updating adoption listing", e);
    }
  }

  @Override
  public Page<AdoptionListingsResponse> getAllAdoptionListings(Pageable pageable, String species, Long breedId,
      String gender, Integer minAge, Integer maxAge) {
    try {
      Species speciesEnum = species != null ? Species.valueOf(species.toUpperCase()) : null;
      Gender genderEnum = null;
      if (gender != null) {
        try {
          genderEnum = Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
          throw new RuntimeException("Invalid gender value: " + gender, e);
        }
      }
      Page<AdoptionListing> adoptionListingPage = adoptionListingsRepository.findFilteredAdoptionListings(speciesEnum,
          breedId, genderEnum, minAge, maxAge, pageable);
      return adoptionListingPage.map(adoptionListing -> {
        AdoptionListingsResponse response = new AdoptionListingsResponse();
        response.setId(adoptionListing.getId());
        response.setImage(adoptionListing.getImage());
        response.setPetName(adoptionListing.getPetName());
        response.setDescription(adoptionListing.getDescription());
        response.setGender(adoptionListing.getGender().toString());
        response.setBreed(adoptionListing.getBreed());
        response.setSpecies(adoptionListing.getSpecies().toString());
        response.setStatus(adoptionListing.getStatus().toString());
        response.setAdoptionStatus(adoptionListing.getAdoptionStatus().toString());
        response.setAge(adoptionListing.getAge());

        ShelterAdoptionResponse shelterDto = modelMapper.map(adoptionListing.getShelter(),
            ShelterAdoptionResponse.class);
        response.setShelter(shelterDto);
        return response;
      });
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
