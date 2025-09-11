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
     * Handle typing status updates
     */
    @PostMapping("/typing")
    public ResponseEntity<Map<String, Object>> updateTypingStatus(
            @RequestParam Long conversationId,
            @RequestParam String clientId,
            @RequestParam Boolean isTyping,
            @RequestParam(required = false) Long customerId) {

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", conversationId);
            result.put("clientId", clientId);
            result.put("isTyping", isTyping);
            result.put("timestamp", java.time.LocalDateTime.now());

            if (isTyping) {
                result.put("message", "AI đang tư vấn về thú cưng của bạn...");
                result.put("typingMessage", getTypingMessageForContext(conversationId));
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorInfo);
        }
    }

    /**
     * Get typing status for a conversation
     */
    @GetMapping("/typing/{conversationId}")
    public ResponseEntity<Map<String, Object>> getTypingStatus(@PathVariable Long conversationId) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", conversationId);
            result.put("isTyping", false); // For now, return false as we don't track real-time typing
            result.put("typingMessage", null);
            result.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorInfo);
        }
    }

    /**
     * Get appropriate typing message based on conversation context
     */
    private String getTypingMessageForContext(Long conversationId) {
        try {
            // Get recent messages to understand context
            var messages = chatMessageRepository.findByConversation(
                conversationRepository.findById(conversationId).orElse(null));

            if (messages.isEmpty()) {
                return "AI đang chuẩn bị tư vấn...";
            }

            String lastMessage = messages.get(messages.size() - 1).getContent().toLowerCase();

            // Determine typing message based on content
            if (lastMessage.contains("bệnh") || lastMessage.contains("ốm") || lastMessage.contains("triệu chứng")) {
                return "AI đang phân tích triệu chứng và chuẩn bị tư vấn...";
            } else if (lastMessage.contains("thuốc") || lastMessage.contains("liệu trình")) {
                return "AI đang nghiên cứu phương pháp điều trị phù hợp...";
            } else if (lastMessage.contains("ăn") || lastMessage.contains("uống") || lastMessage.contains("dinh dưỡng")) {
                return "AI đang tư vấn về chế độ dinh dưỡng cho thú cưng...";
            } else if (lastMessage.contains("huấn luyện") || lastMessage.contains("hành vi")) {
                return "AI đang chuẩn bị lời khuyên về huấn luyện thú cưng...";
            } else {
                return "AI đang tư vấn về chăm sóc thú cưng của bạn...";
            }

        } catch (Exception e) {
            return "AI đang xử lý câu hỏi của bạn...";
        }
    }

    /**
     * Simulate different typing indicator approaches
     */
    @GetMapping("/demo-typing/{approach}")
    public ResponseEntity<Map<String, Object>> demoTypingIndicator(@PathVariable String approach) {
        Map<String, Object> demo = new HashMap<>();

        switch (approach.toLowerCase()) {
            case "simple":
                demo.put("approach", "Simple Status Field");
                demo.put("description", "Thêm field status vào ChatResponse");
                demo.put("pros", List.of("Dễ implement", "Không cần WebSocket", "Backward compatible"));
                demo.put("cons", List.of("Không real-time", "Phụ thuộc vào polling", "Lag khi update"));
                demo.put("useCase", "Basic chat apps, không cần real-time");
                break;

            case "websocket":
                demo.put("approach", "WebSocket Events");
                demo.put("description", "Gửi typing events qua WebSocket");
                demo.put("pros", List.of("Real-time", "Responsive", "Scalable"));
                demo.put("cons", List.of("Phức tạp setup", "Cần WebSocket server", "Network overhead"));
                demo.put("useCase", "Real-time chat apps, gaming, collaboration tools");
                break;

            case "polling":
                demo.put("approach", "HTTP Polling");
                demo.put("description", "Client poll server định kỳ để check typing status");
                demo.put("pros", List.of("Dễ implement", "Không cần WebSocket", "Reliable"));
                demo.put("cons", List.of("Network overhead", "Lag", "Server load"));
                demo.put("useCase", "Simple apps, low-frequency updates");
                break;

            case "sse":
                demo.put("approach", "Server-Sent Events (SSE)");
                demo.put("description", "Server push events to client");
                demo.put("pros", List.of("Real-time", "Simple setup", "Low latency"));
                demo.put("cons", List.of("One-way communication", "Browser support", "Connection limits"));
                demo.put("useCase", "Notifications, live updates, typing indicators");
                break;

            case "current":
                demo.put("approach", "Current Implementation");
                demo.put("description", "Status field + simulated typing");
                demo.put("status", Map.of(
                    "isTyping", true,
                    "typingMessage", "AI đang tư vấn về triệu chứng của thú cưng...",
                    "conversationStatus", "AI"
                ));
                demo.put("pros", List.of("Dễ test", "UX tốt", "Backward compatible"));
                demo.put("cons", List.of("Không real-time", "Simulated delay"));
                break;

            default:
                demo.put("error", "Approach not found. Available: simple, websocket, polling, sse, current");
        }

        return ResponseEntity.ok(demo);
    }

    /**
     * Test markdown cleaning functionality
     */
    @PostMapping("/test-markdown-clean")
    public ResponseEntity<Map<String, Object>> testMarkdownClean(@RequestBody Map<String, String> request) {
        try {
            String markdownText = request.get("text");
            if (markdownText == null) {
                markdownText = """
                    # Header Example

                    Đây là **text in đậm** và *text in nghiêng*.

                    ## Danh sách:
                    1. Item số 1
                    2. Item số 2
                    - Bullet point 1
                    - Bullet point 2

                    ### Code:
                    `inline code` và ```code block```

                    [Link example](https://example.com)

                    ---
                    """;
            }

            if (!(chatService instanceof ChatServiceImpl)) {
                throw new IllegalStateException("ChatService is not ChatServiceImpl");
            }

            ChatServiceImpl chatServiceImpl = (ChatServiceImpl) chatService;
            String cleanedText = chatServiceImpl.cleanMarkdownForTesting(markdownText);

            Map<String, Object> result = new HashMap<>();
            result.put("original", markdownText);
            result.put("cleaned", cleanedText);
            result.put("originalLength", markdownText.length());
            result.put("cleanedLength", cleanedText.length());
            result.put("success", true);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("success", false);
            return ResponseEntity.badRequest().body(errorInfo);
        }
    }
}
