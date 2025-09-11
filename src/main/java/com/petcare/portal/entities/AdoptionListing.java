package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.petcare.portal.enums.AdoptionStatus;
import com.petcare.portal.enums.GenderPet;
import com.petcare.portal.enums.PetHealthStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "adoption_listings")
public class AdoptionListing extends AbstractEntity {
  @ManyToOne
  @JoinColumn(name = "shelter_id", nullable = false)
  @JsonBackReference
  private Customer shelter;

  private String petName;

  @Enumerated(EnumType.STRING)
  private PetHealthStatus status;

  @Enumerated(EnumType.STRING)
  private AdoptionStatus adoptionStatus = AdoptionStatus.PENDING;

  private String image;

  private String shelterId;

  private String description;

  @Enumerated(EnumType.STRING)
  private GenderPet genderPet;

  @ManyToOne
  private Breed breed;

  @ManyToOne
  private Species species;

  private String location;

  private int age;
}
