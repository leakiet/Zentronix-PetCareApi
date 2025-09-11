package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.api.client.util.DateTime;

import jakarta.persistence.Entity;
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
@Table(name = "health_records")
public class HealthRecord extends AbstractEntity {
  @ManyToOne
  @JoinColumn(name = "pet_id", nullable = false)
  @JsonBackReference
  private Pet pet;

  @ManyToOne
  @JoinColumn(name = "vet_id", nullable = false)
  @JsonBackReference
  private User vet;

  private DateTime visitDate;

  private String diagnosis;
  private String treatment;
  private String notes;
}
