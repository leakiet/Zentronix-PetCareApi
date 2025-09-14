package com.petcare.portal.dtos.AppointmentDtos;

import java.time.LocalDateTime;

import com.petcare.portal.enums.AppointmentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequest {
    private Long petId;
    private Long ownerId;
    private Long vetId;
    private LocalDateTime appointmentTime;
    private String reason;
    private String notes;
    private AppointmentStatus status;
}
