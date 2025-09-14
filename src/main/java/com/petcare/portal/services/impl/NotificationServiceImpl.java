package com.petcare.portal.services.impl;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.entities.Appointment;
import com.petcare.portal.entities.NotificationMessage;
import com.petcare.portal.services.NotificationShelterService;

@Service
public class NotificationServiceImpl implements NotificationShelterService {
  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  public void sendAdoptionRequestNotification(Long shelterId, AdoptionRequest request) {
    messagingTemplate.convertAndSend("/topic/notifications/" + shelterId, request);
  }

  public void sendAdoptionStatusNotification(Long ownerId, String type, AdoptionRequest request) {
    NotificationMessage message = new NotificationMessage();
    message.setType(type);
    message.setMessage("Your adoption request for " + request.getAdoptionListing().getPetName() + " has been "
        + type.toLowerCase() + ".");
    message.setRequestId(request.getId());
    messagingTemplate.convertAndSend("/topic/notifications/" + ownerId, message);
  }

  @Override
  public void sendAppointmentRescheduleNotification(Appointment appointment) {
    NotificationMessage message = new NotificationMessage();
    message.setType("APPOINTMENT_RESCHEDULED");
    message.setMessage(String.format("Your appointment for %s has been rescheduled to %s by Dr. %s",
        appointment.getPet().getPetName(),
        appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
        appointment.getVet().getFullName()));
    message.setAppointmentId(appointment.getId());
    messagingTemplate.convertAndSend("/topic/notifications/" + appointment.getOwner().getId(), message);
  }

  @Override
  public void sendAppointmentStatusNotification(Appointment appointment, String status) {
    NotificationMessage message = new NotificationMessage();
    message.setType("APPOINTMENT_STATUS_UPDATE");
    message.setMessage(String.format("Your appointment for %s has been %s",
        appointment.getPet().getPetName(),
        status.toLowerCase()));
    message.setAppointmentId(appointment.getId());
    messagingTemplate.convertAndSend("/topic/notifications/" + appointment.getOwner().getId(), message);
  }

  @Override
  public void sendAppointmentCompletedNotification(Appointment appointment) {
    NotificationMessage message = new NotificationMessage();
    message.setType("APPOINTMENT_COMPLETED");
    message.setMessage(String.format("Your appointment for %s has been completed. Health record has been created.",
        appointment.getPet().getPetName()));
    message.setAppointmentId(appointment.getId());
    messagingTemplate.convertAndSend("/topic/notifications/" + appointment.getOwner().getId(), message);
  }

  @Override
  public void sendNewAppointmentNotification(Appointment appointment) {
    NotificationMessage message = new NotificationMessage();
    message.setType("NEW_APPOINTMENT");
    message.setMessage(String.format("New appointment scheduled with %s for %s on %s",
        appointment.getOwner().getFullName(),
        appointment.getPet().getPetName(),
        appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
    message.setAppointmentId(appointment.getId());
    messagingTemplate.convertAndSend("/topic/notifications/" + appointment.getVet().getId(), message);
  }

}
