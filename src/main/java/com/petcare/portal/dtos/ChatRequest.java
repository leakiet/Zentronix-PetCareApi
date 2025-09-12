// ChatRequest.java
package com.petcare.portal.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatRequest {
    private Long   conversationId;   // null nếu guest
    private String message;
    private String senderRole;       // "PET_OWNER", "EMP", etc.

    // Status fields for typing indicator
    private String  status;          // "SENT", "TYPING_START", "TYPING_END"
    private String  clientId;        // ID của client để track typing
    private Boolean isTyping;        // true khi user đang gõ
}
