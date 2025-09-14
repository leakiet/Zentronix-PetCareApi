package com.petcare.portal.dtos.ClinicInfoDtos;

import com.petcare.portal.entities.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicInfoResponse {
  private Long id;
  private String clinicName;
  private String address;
  private String openingHours;
  private String yearOfExp;
  private String specialization;
  private String servicesOffered;
  private User veterinarians;
}
