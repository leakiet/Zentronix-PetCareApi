package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.petcare.portal.enums.AdoptionStatus;
import com.petcare.portal.enums.Gender;
import com.petcare.portal.enums.PetHealthStatus;
import com.petcare.portal.enums.Species;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
  private String description;
  private int age;
  private String image;

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @ManyToOne
  @JoinColumn(name = "breed_id")
  private Breed breed;

  @Enumerated(EnumType.STRING)
  private Species species;

  @Enumerated(EnumType.STRING)
  private AdoptionStatus adoptionStatus;

  @Enumerated(EnumType.STRING)
  private PetHealthStatus status;

  @ManyToOne
  @JoinColumn(name = "adopted_by_owner_id")
  private User adoptedBy;

  private LocalDateTime adoptedAt;

  @OneToOne
  @JoinColumn(name = "approved_request_id")
  private AdoptionRequest approvedRequest;
}
