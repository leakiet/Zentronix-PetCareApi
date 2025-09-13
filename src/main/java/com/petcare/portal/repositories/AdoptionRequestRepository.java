package com.petcare.portal.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.petcare.portal.entities.AdoptionListing;
import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.RequestStatus;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest, Long> {
  List<AdoptionRequest> findByAdoptionListingAndStatus(AdoptionListing listing, RequestStatus status);

  List<AdoptionRequest> findByUserId(Long userId);

  List<AdoptionRequest> findByAdoptionListingId(Long adoptionListingId);

  AdoptionRequest findByUserIdAndAdoptionListingId(Long userId, Long adoptionListingId);

  List<AdoptionRequest> findByShelter(User shelter);

  List<AdoptionRequest> findByUser(User user);

  List<AdoptionRequest> findByUserAndUpdatedAtAfter(User user, LocalDateTime updatedAfter);

  List<AdoptionRequest> findByAdoptionListingIdAndStatus(Long adoptionListingId, RequestStatus status);
}
