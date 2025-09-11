package com.petcare.portal.entities;

import com.petcare.portal.enums.GenderPet;
import com.petcare.portal.enums.StatusAdoptionListings;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "adoption_listings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdoptionListings extends AbstractEntity {

  private String image;

  private String shelterId;

  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  private GenderPet genderPet;

  @ManyToOne
  private Breed breed;

  @Enumerated(EnumType.STRING)
  private StatusAdoptionListings status;

  @ManyToOne
  private Species species;

  private int age;
}
