package com.petcare.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import com.petcare.portal.dtos.AdoptionRequestDtos.AdoptionRequestResponse;
import com.petcare.portal.dtos.AdoptionRequestDtos.OwnerAdoptionRequest;
import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.RequestStatus;
import com.petcare.portal.repositories.AdoptionListingsRepository;
import com.petcare.portal.repositories.AdoptionRequestRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.AdoptionListingsService;
import com.petcare.portal.services.AdoptionRequestService;
import com.petcare.portal.services.NotificationShelterService;

@Service
public class AdoptionRequestServiceImpl implements AdoptionRequestService {

  @Autowired
  private AdoptionRequestRepository adoptionRequestRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AdoptionListingsRepository adoptionListingsRepository;

  @Autowired
  private AdoptionListingsService adoptionListingsService; // Add this

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private NotificationShelterService notificationService;

  @Override
  public List<AdoptionRequest> getRequestsByAdoptionListingId(Long adoptionListingId) {
    try {
      return adoptionRequestRepository.findByAdoptionListingId(adoptionListingId);
    } catch (Exception e) {
      throw new RuntimeException("Error fetching requests for listing: " + e.getMessage());
    }
  }

  @Override
  public AdoptionRequest createAdoptionRequest(Long ownerId, Long adoptionListingId, Long shelterId, String message,
      String distance) {
    try {
      User owner = userRepository.findById(ownerId).orElseThrow(() -> new RuntimeException("Owner not found"));
      AdoptionListing listing = adoptionListingsRepository.findById(adoptionListingId)
          .orElseThrow(() -> new RuntimeException("AdoptionListing not found"));

      AdoptionRequest existingRequest = adoptionRequestRepository.findByUserIdAndAdoptionListingId(ownerId,
          adoptionListingId);
      if (existingRequest != null) {
        throw new RuntimeException("You have already sent a request for this listing");
      }

      User shelter = userRepository.findById(shelterId)
          .orElseThrow(() -> new RuntimeException("Shelter not found"));

      AdoptionRequest request = new AdoptionRequest();
      request.setUser(owner);
      request.setShelter(shelter);
      request.setAdoptionListing(listing);
      request.setMessage(message);
      request.setDistance(distance);
      request.setStatus(RequestStatus.PENDING);

      AdoptionRequest savedRequest = adoptionRequestRepository.save(request);

      notificationService.sendAdoptionRequestNotification(shelter.getId(), savedRequest);

      return savedRequest;
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

  @Override
  public List<AdoptionRequest> getRequestsByShelterId(Long shelterId) {
    try {
      User shelter = userRepository.findById(shelterId)
          .orElseThrow(() -> new RuntimeException("Shelter not found"));
      return adoptionRequestRepository.findByShelter(shelter);
    } catch (Exception e) {
      throw new RuntimeException("Error fetching requests by shelter ID: " + e.getMessage());
    }
  }

  @Override
  public List<AdoptionRequest> getRequestsByOwnerId(Long ownerId, LocalDateTime updatedAfter) {
    try {
      User owner = userRepository.findById(ownerId)
          .orElseThrow(() -> new RuntimeException("Owner not found"));
      List<AdoptionRequest> requests;
      if (updatedAfter != null) {
        requests = adoptionRequestRepository.findByUserAndUpdatedAtAfter(owner, updatedAfter);
      } else {
        requests = adoptionRequestRepository.findByUser(owner);
      }
      return requests.stream()
          .filter(r -> r.getStatus() == RequestStatus.APPROVED || r.getStatus() == RequestStatus.REJECTED)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Error fetching requests by owner ID: " + e.getMessage());
    }
  }

  @Override
  @Transactional
  public AdoptionListingsResponse approveRequestAndRejectOthers(Long requestId, Long ownerId) {
    try {
      AdoptionRequest requestToApprove = adoptionRequestRepository.findById(requestId)
          .orElseThrow(() -> new RuntimeException("Adoption request not found"));

      Long listingId = requestToApprove.getAdoptionListing().getId();

      requestToApprove.setStatus(RequestStatus.APPROVED);
      adoptionRequestRepository.save(requestToApprove);

      notificationService.sendAdoptionStatusNotification(requestToApprove.getUser().getId(), "APPROVED",
          requestToApprove);

      List<AdoptionRequest> pendingRequests = adoptionRequestRepository.findByAdoptionListingIdAndStatus(listingId,
          RequestStatus.PENDING);
      for (AdoptionRequest req : pendingRequests) {
        if (!req.getId().equals(requestId)) {
          req.setStatus(RequestStatus.REJECTED);
          adoptionRequestRepository.save(req);

          notificationService.sendAdoptionStatusNotification(req.getUser().getId(), "REJECTED", req);
        }
      }

      return adoptionListingsService.approveAdoptionRequest(listingId, requestId, ownerId);
    } catch (Exception e) {
      throw new RuntimeException("Error approving request and rejecting others: " + e.getMessage(), e);
    }
  }
}
