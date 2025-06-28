package com.greenkitchen.portal.controllers;

import com.greenkitchen.portal.dtos.ChatRequest;
import com.greenkitchen.portal.dtos.ChatResponse;
import com.greenkitchen.portal.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/apis/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

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
}
