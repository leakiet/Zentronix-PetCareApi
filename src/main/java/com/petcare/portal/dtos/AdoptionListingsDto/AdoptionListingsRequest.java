package com.petcare.portal.dtos.AdoptionListingsDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionListingsRequest {
  private String image;
  private String shelterId;
  private String petName;
  private String description;
  private String gender;
  private Long breedId;
  private String species;
  private String status;
  private String adoptionStatus; 
  private int age;
}
