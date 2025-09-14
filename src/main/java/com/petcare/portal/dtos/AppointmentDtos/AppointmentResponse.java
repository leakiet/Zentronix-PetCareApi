package com.petcare.portal.dtos.AppointmentDtos;

import java.time.LocalDateTime;

import com.petcare.portal.entities.Pet;
import com.petcare.portal.entities.User;
import com.petcare.portal.enums.AppointmentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentResponse {
    private Long id;
    private Pet pet;
    private User owner;
    private User vet;
    private LocalDateTime appointmentTime;
    private String reason;
    private String notes;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
