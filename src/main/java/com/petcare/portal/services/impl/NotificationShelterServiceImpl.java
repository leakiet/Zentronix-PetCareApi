package com.petcare.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.petcare.portal.entities.AdoptionRequest;
import com.petcare.portal.services.NotificationShelterService;

import lombok.Getter;
import lombok.Setter;

@Service
public class NotificationShelterServiceImpl implements NotificationShelterService {
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

  @Getter
  @Setter
  public static class NotificationMessage {
    private String type;
    private String message;
    private Long requestId;
  }
}
