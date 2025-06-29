// ChatService.java
package com.greenkitchen.portal.services;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import java.util.List;

public interface ChatService {
    ChatResponse           sendMessage(Long customerId, ChatRequest request);
    List<ChatResponse>     getMessagesByConversation(Long conversationId);
    List<Long>             getConversationsByCustomer(Long customerId);
}
