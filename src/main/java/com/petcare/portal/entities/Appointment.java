package com.petcare.portal.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.api.client.util.DateTime;
import com.petcare.portal.enums.AppointmentStatus;

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
@Table(name = "appointments")
public class Appointment extends AbstractEntity {
  @ManyToOne
  @JoinColumn(name = "pet_id", nullable = false)
  @JsonBackReference
  private Pet pet;

  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  @JsonBackReference
  private Customer owner;

  @ManyToOne
  @JoinColumn(name = "vet_id", nullable = false)
  @JsonBackReference
  private Customer vet;

  private DateTime appointmentTime;
  private String reason;
  private String notes;

  @Enumerated(EnumType.STRING)
  private AppointmentStatus status = AppointmentStatus.SCHEDULED;
}
