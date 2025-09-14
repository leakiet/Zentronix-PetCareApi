package com.petcare.portal.dtos.ClinicInfoDtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicInfoRequest {
  private String clinicName;
  private String address;
  private String openingHours;
  private String yearOfExp;
  private String specialization;
  private String servicesOffered;
  private Long veterinarianId;
}
