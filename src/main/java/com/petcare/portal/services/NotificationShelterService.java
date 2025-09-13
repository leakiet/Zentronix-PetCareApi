package com.petcare.portal.services;

public interface NotificationShelterService {
  void sendAdoptionRequestNotification(Long shelterId, String message);
}
