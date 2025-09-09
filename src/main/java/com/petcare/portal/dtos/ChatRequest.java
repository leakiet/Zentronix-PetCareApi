// ChatRequest.java
package com.petcare.portal.dtos;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatRequest {
    private Long   conversationId;   // null nếu guest
    private String message;
}
