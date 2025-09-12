package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsRequest;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;

public interface AdoptionListingsService {
  AdoptionListingsResponse getAdoptionListingById(Long id);
  AdoptionListingsResponse createAdoptionListing(AdoptionListingsRequest adoptionListingsRequest);
  AdoptionListingsResponse updateAdoptionListing(Long id, AdoptionListingsRequest adoptionListingsRequest);
  List<AdoptionListingsResponse> getAllAdoptionListings();
  void deleteAdoptionListing(Long id);
}
