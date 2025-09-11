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
  private String genderPet;
  private Long breedId;
  private Long speciesId;
  private String status;
  private int age;
  private String location;
}
