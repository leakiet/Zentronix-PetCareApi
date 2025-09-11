package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.petcare.portal.enums.AdoptionStatus;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.enums.PetHealthStatus;

import jakarta.persistence.Column;
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
  private User shelter;

  private String petName;
  private int age;
  private String image;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  private String description;

  @Enumerated(EnumType.STRING)
  private PetHealthStatus status;

  @Enumerated(EnumType.STRING)
  private AdoptionStatus adoptionStatus;

  @ManyToOne
  @JoinColumn(name = "breed_id")
  private Breed breed;

  @ManyToOne
  @JoinColumn(name = "species_id")
  private Species species;

}
