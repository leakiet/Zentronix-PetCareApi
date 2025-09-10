package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.api.client.util.DateTime;
import com.petcare.portal.enums.AdoptionStatus;
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
  private String species;
  private String breed;
  private String age;
  private String description;
  private String image;

  @Enumerated(EnumType.STRING)
  private PetHealthStatus status;

  @Enumerated(EnumType.STRING)
  private AdoptionStatus adoptionStatus = AdoptionStatus.PENDING;
}
