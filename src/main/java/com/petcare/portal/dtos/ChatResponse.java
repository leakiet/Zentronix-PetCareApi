// ChatResponse.java
package com.petcare.portal.dtos;

import lombok.*;
import java.time.LocalDateTime;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsAiResponse;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatResponse {
    private Long    conversationId;  // null nếu guest
    private String  sender;
    private String  message;
    private Boolean isFromAI;

    // Status fields for typing indicator
    private String  status;          // "SENT", "TYPING", "COMPLETED", "ERROR"
    private Long    messageId;       // ID của message trong DB
    private LocalDateTime timestamp; // Thời gian gửi
    private String  conversationStatus; // "AI", "EMP", "WAITING_EMP"

    // Typing indicator specific
    private Boolean isTyping;        // true khi AI đang trả lời
    private String  typingMessage;   // "AI đang tư vấn về triệu chứng của thú cưng..."

    // Adoption data for structured pet listings
    private AdoptionListingsAiResponse adoptionData; // Structured adoption information
}
