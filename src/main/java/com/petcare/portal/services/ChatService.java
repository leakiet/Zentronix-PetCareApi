// ChatService.java
package com.petcare.portal.services;

import com.petcare.portal.dtos.ChatRequest;
import com.petcare.portal.dtos.ChatResponse;
import java.util.List;

public interface ChatService {
    ChatResponse           sendMessage(Long userId, ChatRequest request);
    List<ChatResponse>     getMessagesByConversation(Long conversationId);
    List<Long>             getConversationsByUser(Long userId);
}
