package com.petcare.portal.services;

import java.util.List;

import com.petcare.portal.dtos.AppointmentDtos.AppointmentRequest;
import com.petcare.portal.dtos.AppointmentDtos.AppointmentResponse;
import com.petcare.portal.enums.AppointmentStatus;

public interface AppointmentService {
  AppointmentResponse createAppointment(AppointmentRequest request);

  AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

  AppointmentResponse getAppointmentById(Long id);

  List<AppointmentResponse> getAppointmentsByVetId(Long vetId);

  List<AppointmentResponse> getUpcomingAppointmentsByVetId(Long vetId);

  List<AppointmentResponse> getAppointmentsByPetId(Long petId);

  List<AppointmentResponse> getAppointmentsByOwnerId(Long ownerId);

  void updateAppointmentStatus(Long id, AppointmentStatus status);

  void rescheduleAppointment(Long id, String newAppointmentTime);

  void deleteAppointment(Long id);
}