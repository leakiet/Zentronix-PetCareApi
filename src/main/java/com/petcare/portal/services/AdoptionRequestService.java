package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.AdoptionRequestDtos.AdoptionRequestResponse;
import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.enums.RequestStatus;

public interface AdoptionRequestService {
  AdoptionRequest createAdoptionRequest(Long ownerId, Long adoptionListingId, String message, String distance);
  AdoptionRequestResponse getAdoptionRequestById(Long id);
  List<AdoptionRequest> findByAdoptionListingAndStatus(AdoptionListing listing, RequestStatus status);
  AdoptionRequestResponse updateAdoptionRequestStatus(Long id, String status);
  void deleteAdoptionRequest(Long id);
  List<AdoptionRequest> getAllAdoptionRequests();
  List<AdoptionRequest> getRequestsByAdoptionListingId(Long adoptionListingId);
}
