package com.petcare.portal.controllers;

import com.petcare.portal.dtos.ChatRequest;
import com.petcare.portal.dtos.ChatResponse;
import com.petcare.portal.entities.Conversation;
import com.petcare.portal.repositories.ChatMessageRepository;
import com.petcare.portal.repositories.ConversationRepository;
import com.petcare.portal.services.ChatService;
import com.petcare.portal.services.impl.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apis/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final com.petcare.portal.controllers.WebSocketController webSocketController;

    /**
     * Endpoint để gửi tin nhắn đến AI.
     * @param customerId ID của khách hàng (có thể null nếu là khách vãng lai).
     * @param request Nội dung tin nhắn.
     * @return Phản hồi từ AI.
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestBody ChatRequest request) {
        ChatResponse response = chatService.sendMessage(customerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để lấy danh sách tin nhắn trong một cuộc trò chuyện.
     * @param conversationId ID của cuộc trò chuyện.
     * @return Danh sách tin nhắn.
     */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatResponse>> getMessages(
            @RequestParam("conversationId") Long conversationId) {
        List<ChatResponse> messages = chatService.getMessagesByConversation(conversationId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Endpoint để lấy danh sách ID các cuộc trò chuyện của một khách hàng.
     * @param customerId ID của khách hàng.
     * @return Danh sách ID cuộc trò chuyện.
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Long>> getConversations(
            @RequestParam("customerId") Long customerId) {
        List<Long> conversationIds = chatService.getConversationsByCustomer(customerId);
        return ResponseEntity.ok(conversationIds);
    }

    /**
     * Debug endpoint để kiểm tra memory và summary
     */
    @GetMapping("/debug/{conversationId}")
    public ResponseEntity<Map<String, Object>> debugConversation(@PathVariable Long conversationId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

            long messageCount = chatMessageRepository.countByConversation(conversation);

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("conversationId", conversationId);
            debugInfo.put("messageCount", messageCount);
            debugInfo.put("hasSummary", conversation.getSummary() != null);
            debugInfo.put("summaryLength", conversation.getSummary() != null ? conversation.getSummary().length() : 0);
            debugInfo.put("needsSummary", messageCount > 25);
            debugInfo.put("createdAt", conversation.getCreatedAt());
            debugInfo.put("title", conversation.getTitle());

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorInfo);
        }
    }

    /**
     * Force regenerate summary for debugging
     */
    @PostMapping("/regenerate-summary/{conversationId}")
    public ResponseEntity<Map<String, Object>> regenerateSummary(@PathVariable Long conversationId) {
        try {
            if (!(chatService instanceof ChatServiceImpl)) {
                throw new IllegalStateException("ChatService is not ChatServiceImpl");
            }

            ChatServiceImpl chatServiceImpl = (ChatServiceImpl) chatService;
            String newSummary = chatServiceImpl.regenerateSummary(conversationId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("summary", newSummary);
            result.put("length", newSummary.length());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorInfo);
        }
    }

    /**
     * Endpoint để simulate typing indicator (cho testing)
     */
    @PostMapping("/typing/{conversationId}")
    public ResponseEntity<Map<String, Object>> simulateTyping(@PathVariable Long conversationId) {
        try {
            // Send typing start via WebSocket
            webSocketController.sendConversationStatus(conversationId, "AI_PROCESSING");

            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", conversationId);
            result.put("status", "TYPING");
            result.put("message", "AI is typing...");

            // Simulate typing delay and completion
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // 3 seconds typing simulation
                    webSocketController.sendConversationStatus(conversationId, "COMPLETED");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorInfo);
        }
    }

    /**
     * Test endpoint cho typing indicator
     */
    @GetMapping("/test-typing/{conversationId}")
    public ResponseEntity<Map<String, Object>> testTyping(@PathVariable Long conversationId) {
        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversationId);
        result.put("message", "Typing indicator test endpoint");
        result.put("instructions", "Use POST /typing/{id} to simulate AI typing");

        return ResponseEntity.ok(result);
    }
}
