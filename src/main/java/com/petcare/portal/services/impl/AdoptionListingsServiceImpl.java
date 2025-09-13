package com.petcare.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.dtos.AdoptionListingsDto.ShelterAdoptionResponse;
import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.entities.Breed;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.AdoptionStatus;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.enums.PetHealthStatus;
import com.petcare.portal.enums.Species;
import com.petcare.portal.repositories.AdoptionListingsRepository;
import com.petcare.portal.repositories.AdoptionRequestRepository;
import com.petcare.portal.repositories.BreedRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.AdoptionListingsService;

@Service
public class AdoptionListingsServiceImpl implements AdoptionListingsService {

  @Autowired
  private AdoptionListingsRepository adoptionListingsRepository;

  @Autowired
  private AdoptionRequestRepository adoptionRequestRepository;

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

  @Override
  public List<AdoptionListingsResponse> getAllAdoptionByShelterId(Long shelterId) {
    try {
      List<AdoptionListing> adoptionListings = adoptionListingsRepository.findByShelterId(shelterId);
      return adoptionListings.stream().map(adoptionListing -> {
        AdoptionListingsResponse response = new AdoptionListingsResponse();
        response.setId(adoptionListing.getId());
        response.setImage(adoptionListing.getImage());
        response.setPetName(adoptionListing.getPetName());
        response.setDescription(adoptionListing.getDescription());
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
        }
        response.setShelter(shelterDto);
        return response;
      }).collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error retrieving adoption listings by shelter ID: " + e.getMessage(), e);
    }
  }

  @Override
  public AdoptionListingsResponse approveAdoptionRequest(Long listingId, Long requestId, Long ownerId) {
    try {
      AdoptionListing adoptionListing = adoptionListingsRepository.findById(listingId)
          .orElseThrow(() -> new RuntimeException("Adoption listing not found"));

      AdoptionRequest adoptionRequest = adoptionRequestRepository.findById(requestId)
          .orElseThrow(() -> new RuntimeException("Adoption request not found"));

      User owner = userRepository.findById(ownerId)
          .orElseThrow(() -> new RuntimeException("Owner not found"));

      adoptionListing.setAdoptedBy(owner);
      adoptionListing.setAdoptedAt(LocalDateTime.now());
      adoptionListing.setApprovedRequest(adoptionRequest);
      adoptionListing.setAdoptionStatus(AdoptionStatus.COMPLETED); 

      AdoptionListing updatedListing = adoptionListingsRepository.save(adoptionListing);

      AdoptionListingsResponse response = new AdoptionListingsResponse();
      response.setId(updatedListing.getId());
      response.setImage(updatedListing.getImage());
      response.setPetName(updatedListing.getPetName());
      response.setDescription(updatedListing.getDescription());
      response.setGender(updatedListing.getGender() != null ? updatedListing.getGender().toString() : null);
      response.setBreed(updatedListing.getBreed());
      response.setSpecies(updatedListing.getSpecies() != null ? updatedListing.getSpecies().toString() : null);
      response.setStatus(updatedListing.getStatus() != null ? updatedListing.getStatus().toString() : null);
      response.setAdoptionStatus(updatedListing.getAdoptionStatus() != null ? updatedListing.getAdoptionStatus().toString() : null);
      response.setAge(updatedListing.getAge());

      ShelterAdoptionResponse shelterDto = new ShelterAdoptionResponse();
      if (updatedListing.getShelter() != null) {
        shelterDto.setId(updatedListing.getShelter().getId());
        shelterDto.setFirstName(updatedListing.getShelter().getFirstName());
        shelterDto.setLastName(updatedListing.getShelter().getLastName());
        shelterDto.setCompanyName(updatedListing.getShelter().getCompanyName());
        shelterDto.setPhone(updatedListing.getShelter().getPhone());
        shelterDto.setEmail(updatedListing.getShelter().getEmail());
        shelterDto.setGender(updatedListing.getShelter().getGender() != null ? updatedListing.getShelter().getGender().toString() : null);
        shelterDto.setAddress(updatedListing.getShelter().getAddress());
      }
      response.setShelter(shelterDto);

      return response;
    } catch (Exception e) {
      throw new RuntimeException("Error approving adoption request: " + e.getMessage(), e);
    }
  }

}
