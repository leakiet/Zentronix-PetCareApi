// ChatResponse.java
package com.petcare.portal.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatResponse {
    private Long    conversationId;  // null nếu guest
    private String  sender;
    private String  message;
    private Boolean isFromAI;
}
