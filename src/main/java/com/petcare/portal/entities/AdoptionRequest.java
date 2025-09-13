package com.petcare.portal.entities;

import java.time.LocalDateTime;

import com.petcare.portal.enums.RequestStatus;

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
@Table(name = "adoption_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdoptionRequest extends AbstractEntity {

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "adoption_listing_id")
  private AdoptionListing adoptionListing;

  @Enumerated(EnumType.STRING)
  private RequestStatus status = RequestStatus.PENDING;

  private String message;

  private String distance;
}