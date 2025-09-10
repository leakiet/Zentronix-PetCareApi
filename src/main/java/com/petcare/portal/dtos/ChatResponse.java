// ChatResponse.java
package com.petcare.portal.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatResponse {
    private Long    conversationId;  // null nếu guest
    private String  sender;
    private String  message;
    private Boolean isFromAI;

    // Typing indicator status
    private String  status;  // "TYPING", "SENDING", "SENT", "ERROR"

    // Timestamp
    private LocalDateTime timestamp;

    // Constructor tiện lợi
    public ChatResponse(Long conversationId, String sender, String message, Boolean isFromAI) {
        this.conversationId = conversationId;
        this.sender = sender;
        this.message = message;
        this.isFromAI = isFromAI;
        this.status = "SENT";
        this.timestamp = LocalDateTime.now();
    }

    // Constructor cho typing indicator
    public ChatResponse(Long conversationId, String status) {
        this.conversationId = conversationId;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.isFromAI = true;
        this.sender = "PetCare AI";
        this.message = "";
    }
}
