package com.petcare.portal.dtos.AdoptionRequestDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionRequestResponse {
  private Long id;
  private OwnerAdoptionRequest owner;
  private Long adoptionListingId;
  private String status;
  private String message;
  private String distance;
}
