package com.petcare.portal.dtos.healthRecordApptDtos;

import com.petcare.portal.entities.Appointment;
import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthRecordApptResponse {
  private Pet pet;
  private User vet;
  private Appointment appointment;
  private String diagnosis;
  private String treatment;
  private String notes;
}
