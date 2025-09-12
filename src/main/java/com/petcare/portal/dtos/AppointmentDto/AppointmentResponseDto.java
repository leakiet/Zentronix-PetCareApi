package com.petcare.portal.dtos.AppointmentDto;

import java.time.LocalDateTime;

//package com.petcare.portal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentResponseDto {
 private Long id;
 private LocalDateTime appointmentTime;
 private String reason;
 private String notes;
 private String status;
 private Long petId;
 private String petName;
 private Long ownerId;
 private String ownerName;
 private Long vetId;
 private String vetName;
}
