package com.petcare.portal.services.impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petcare.portal.dtos.AdoptionRequestDtos.AdoptionRequestResponse;
import com.petcare.portal.dtos.AdoptionRequestDtos.OwnerAdoptionRequest;
import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.RequestStatus;
import com.petcare.portal.repositories.AdoptionListingsRepository;
import com.petcare.portal.repositories.AdoptionRequestRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.AdoptionRequestService;

@Service
public class AdoptionRequestServiceImpl implements AdoptionRequestService {

  @Autowired
  private AdoptionRequestRepository adoptionRequestRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AdoptionListingsRepository adoptionListingsRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public List<AdoptionRequest> getRequestsByAdoptionListingId(Long adoptionListingId) {
    try {
      return adoptionRequestRepository.findByAdoptionListingId(adoptionListingId);
    } catch (Exception e) {
      throw new RuntimeException("Error fetching requests for listing: " + e.getMessage());
    }
  }

  @Override
  public AdoptionRequest createAdoptionRequest(Long ownerId, Long adoptionListingId, String message, String distance) {
    try {
      User owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));
      AdoptionListing listing = adoptionListingsRepository.findById(adoptionListingId)
          .orElseThrow(() -> new RuntimeException("AdoptionListing not found"));

      AdoptionRequest existingRequest = adoptionRequestRepository.findByUserIdAndAdoptionListingId(ownerId,
          adoptionListingId);
      if (existingRequest != null) {
        throw new RuntimeException("You have already sent a request for this listing");
      }

      AdoptionRequest request = new AdoptionRequest();
      request.setUser(owner);
      request.setAdoptionListing(listing);
      request.setMessage(message);
      request.setDistance(distance);
      request.setStatus(RequestStatus.PENDING);

      return adoptionRequestRepository.save(request);
    } catch (Exception e) {
      throw new RuntimeException("Error creating adoption request: " + e.getMessage());
    }
  }

  @Override
  public AdoptionRequestResponse getAdoptionRequestById(Long id) {
    try {
      AdoptionRequest request = adoptionRequestRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Request not found"));
      return mapToResponse(request);
    } catch (Exception e) {
      throw new RuntimeException("Error fetching request: " + e.getMessage());
    }
  }

  @Override
  public AdoptionRequestResponse updateAdoptionRequestStatus(Long id, String status) {
    try {
      AdoptionRequest request = adoptionRequestRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Request not found"));
      request.setStatus(RequestStatus.valueOf(status.toUpperCase()));
      AdoptionRequest saved = adoptionRequestRepository.save(request);
      return mapToResponse(saved);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid status: " + status);
    } catch (Exception e) {
      throw new RuntimeException("Error updating request status: " + e.getMessage());
    }
  }

  @Override
  public void deleteAdoptionRequest(Long id) {
    try {
      AdoptionRequest request = adoptionRequestRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Request not found"));
      adoptionRequestRepository.delete(request);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting request: " + e.getMessage());
    }
  }

  @Override
  public List<AdoptionRequest> findByAdoptionListingAndStatus(AdoptionListing listing, RequestStatus status) {
    try {
      return adoptionRequestRepository.findByAdoptionListingAndStatus(listing, status);
    } catch (Exception e) {
      throw new RuntimeException("Error fetching requests by status: " + e.getMessage());
    }
  }

  @Override
  public List<AdoptionRequest> getAllAdoptionRequests() {
    try {
      return adoptionRequestRepository.findAll();
    } catch (Exception e) {
      throw new RuntimeException("Error fetching all requests: " + e.getMessage());
    }
  }

  private AdoptionRequestResponse mapToResponse(AdoptionRequest request) {
    AdoptionRequestResponse response = modelMapper.map(request, AdoptionRequestResponse.class);

    OwnerAdoptionRequest ownerDto = modelMapper.map(request.getUser(), OwnerAdoptionRequest.class);
    response.setOwner(ownerDto);

    response.setStatus(request.getStatus().toString());

    return response;
  }
}
