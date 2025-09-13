package com.petcare.portal.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationMessage {
  private String type;
  private String message;
  private Long requestId;
}
