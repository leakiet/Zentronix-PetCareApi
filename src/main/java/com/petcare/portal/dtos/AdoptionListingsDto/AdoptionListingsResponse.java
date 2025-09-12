package com.petcare.portal.dtos.AdoptionListingsDto;

import com.petcare.portal.entities.Breed;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionListingsResponse {
  private Long id;
  private String image;
  private ShelterAdoptionResponse shelter;
  private String petName;
  private String description;
  private String gender;
  private Breed breed;
  private String species;
  private String status;
  private String adoptionStatus;
  private int age;
}
