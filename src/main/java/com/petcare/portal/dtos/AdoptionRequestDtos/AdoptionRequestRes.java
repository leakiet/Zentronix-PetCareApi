package com.petcare.portal.dtos.AdoptionRequestDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionRequestRes {
  private Long ownerId;
  private Long adoptionListingId;
  private String message;
  private String distance;
}
