package com.petcare.portal.entities;

import com.petcare.portal.enums.Specialization;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clinic_info")
public class ClinicInfo extends AbstractEntity {
  private String clinicName;
  private String address;
  private String openingHours;
  private String yearOfExp;
  @Enumerated(EnumType.STRING)
  private Specialization specialization;
  private String servicesOffered;
  @OneToOne
  private User veterinarians;
}
