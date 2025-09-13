package com.petcare.portal.dtos.AdoptionRequestDtos;

import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionRequestResponse {
  private Long id;
  private OwnerAdoptionRequest owner;
  private AdoptionListingsResponse adoptionListing;
  private String status;
  private String message;
  private String distance;
}
