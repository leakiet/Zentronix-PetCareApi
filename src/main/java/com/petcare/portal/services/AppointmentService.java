package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.UserResponseDto;
import com.petcare.portal.dtos.AppointmentDto.AppointmentRequestDto;
import com.petcare.portal.dtos.AppointmentDto.AppointmentResponseDto;
import com.petcare.portal.entities.User;

public interface AppointmentService {
	 AppointmentResponseDto createAppointment(AppointmentRequestDto request, User currentUser);
	 List<AppointmentResponseDto> getAppointmentsForOwner(User currentUser);
	 List<AppointmentResponseDto> getAppointmentsForVet(User currentUser);
	 AppointmentResponseDto updateAppointmentStatus(Long id, String status, User currentUser);
	 List<UserResponseDto> suggestVets(Long petId, String reason, User currentUser);
}
