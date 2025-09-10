package com.petcare.portal.controllers;

import com.petcare.portal.dtos.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle typing start notification
     */
    @MessageMapping("/typing/start/{conversationId}")
    public void handleTypingStart(@DestinationVariable Long conversationId,
                                  @Payload Map<String, Object> payload) {
        log.debug("User started typing in conversation: {}", conversationId);

        // Send typing indicator to all subscribers
        ChatResponse typingResponse = new ChatResponse(conversationId, "TYPING");
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + conversationId + "/typing",
            typingResponse
        );
    }

    /**
     * Handle typing stop notification
     */
    @MessageMapping("/typing/stop/{conversationId}")
    public void handleTypingStop(@DestinationVariable Long conversationId,
                                 @Payload Map<String, Object> payload) {
        log.debug("User stopped typing in conversation: {}", conversationId);

        // Send stop typing indicator
        ChatResponse stopTypingResponse = new ChatResponse(conversationId, "STOP_TYPING");
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + conversationId + "/typing",
            stopTypingResponse
        );
    }

    /**
     * Handle AI typing simulation
     */
    @MessageMapping("/ai/typing/start/{conversationId}")
    public void handleAITypingStart(@DestinationVariable Long conversationId) {
        log.debug("AI started typing in conversation: {}", conversationId);

        // Simulate AI is thinking/typing
        ChatResponse aiTypingResponse = new ChatResponse(
            conversationId,
            "AI is thinking...",
            "TYPING"
        );
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + conversationId + "/ai-typing",
            aiTypingResponse
        );

        // Auto stop typing after 2-3 seconds (simulate AI processing)
        new Thread(() -> {
            try {
                Thread.sleep(2500); // 2.5 seconds
                handleAITypingStop(conversationId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Handle AI typing stop
     */
    public void handleAITypingStop(Long conversationId) {
        log.debug("AI stopped typing in conversation: {}", conversationId);

        ChatResponse stopTypingResponse = new ChatResponse(conversationId, "STOP_TYPING");
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + conversationId + "/ai-typing",
            stopTypingResponse
        );
    }

    /**
     * Send real-time message update
     */
    public void sendMessageUpdate(Long conversationId, ChatResponse message) {
        log.debug("Sending message update for conversation: {}", conversationId);
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + conversationId + "/messages",
            message
        );
    }

    /**
     * Send conversation status update
     */
    public void sendConversationStatus(Long conversationId, String status) {
        log.debug("Sending status update for conversation: {} - {}", conversationId, status);
        Map<String, Object> statusUpdate = Map.of(
            "conversationId", conversationId,
            "status", status,
            "timestamp", LocalDateTime.now()
        );
        messagingTemplate.convertAndSend(
            "/topic/conversations/" + conversationId + "/status",
            statusUpdate
        );
    }
}
