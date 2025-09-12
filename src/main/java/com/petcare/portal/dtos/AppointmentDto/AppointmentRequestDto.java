package com.petcare.portal.dtos.AppointmentDto;

import java.time.LocalDateTime;

//package com.petcare.portal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequestDto {
 private Long petId;
 private Long vetId;
 private LocalDateTime appointmentTime;
 private String reason;
 private String notes;
}
