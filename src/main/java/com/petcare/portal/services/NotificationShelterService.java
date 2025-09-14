package com.petcare.portal.services;

import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.entities.Appointment;

public interface NotificationShelterService {
  void sendAdoptionRequestNotification(Long shelterId, AdoptionRequest request);

  void sendAdoptionStatusNotification(Long ownerId, String type, AdoptionRequest request);

  void sendAppointmentRescheduleNotification(Appointment appointment);

  void sendAppointmentStatusNotification(Appointment appointment, String status);

  void sendAppointmentCompletedNotification(Appointment appointment);

  void sendNewAppointmentNotification(Appointment appointment);
}
