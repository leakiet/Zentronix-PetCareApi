package com.petcare.portal.services;

import com.petcare.portal.entities.AdoptionRequest;

public interface NotificationShelterService {
  void sendAdoptionRequestNotification(Long shelterId, AdoptionRequest request);
  void sendAdoptionStatusNotification(Long ownerId, String type, AdoptionRequest request);
}
