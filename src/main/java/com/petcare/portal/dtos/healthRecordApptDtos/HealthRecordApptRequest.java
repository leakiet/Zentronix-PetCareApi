package com.petcare.portal.dtos.healthRecordApptDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthRecordApptRequest {
  private Long petId;
  private Long vetId;
  private Long appointmentId;
  private String diagnosis;
  private String treatment;
  private String notes;
  private String vetName;
}
