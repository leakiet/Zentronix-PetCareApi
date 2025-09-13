package com.petcare.portal.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;

public interface AdoptionListingsService {
  AdoptionListingsResponse getAdoptionListingById(Long id);
  AdoptionListingsResponse createAdoptionListing(AdoptionListingsRequest adoptionListingsRequest);
  AdoptionListingsResponse updateAdoptionListing(Long id, AdoptionListingsRequest adoptionListingsRequest);
  Page<AdoptionListingsResponse> getAllAdoptionListings(Pageable pageable, String species, Long breedId, String gender, Integer minAge, Integer maxAge);
  void deleteAdoptionListing(Long id);
  List<AdoptionListingsResponse> getAllAdoptionByShelterId(Long shelterId);
}
