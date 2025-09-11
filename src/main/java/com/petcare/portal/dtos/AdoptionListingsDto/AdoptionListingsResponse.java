package com.petcare.portal.dtos.AdoptionListingsDto;

import com.petcare.portal.entities.Breed;
import com.petcare.portal.entities.Species;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionListingsResponse {
  private Long id;
  private String image;
  private String shelterId;
  private String name;
  private String description;
  private String genderPet;
  private Breed breed;
  private Species species;
  private String status;
  private int age;
}
