package com.petcare.portal.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.petcare.portal.services.NotificationShelterService;

@Service
public class NotificationShelterServiceImpl implements NotificationShelterService {
  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  public void sendAdoptionRequestNotification(Long shelterId, String message) {
    messagingTemplate.convertAndSend("/topic/notifications/" + shelterId, message);
  }
}
