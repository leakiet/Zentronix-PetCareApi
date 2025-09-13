package com.petcare.portal.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petcare.portal.dtos.ChatRequest;
import com.petcare.portal.dtos.ChatResponse;
import com.petcare.portal.entities.ChatMessage;
import com.petcare.portal.entities.Conversation;
import com.petcare.portal.repositories.ChatMessageRepository;
import com.petcare.portal.repositories.ConversationRepository;
import com.petcare.portal.services.ChatService;
import com.petcare.portal.services.impl.ChatServiceImpl;

import org.springframework.data.domain.Page;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/apis/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private static final Logger log =LoggerFactory.getLogger(ChatController.class);

    /**
     * Endpoint to send message to AI.
     * @param userEmail User's email (null for guest users).
     * @param request Chat request containing message and conversation details.
     * @return AI response.
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestBody ChatRequest request) {
        ChatResponse response = chatService.sendMessage(userEmail, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để lấy danh sách tin nhắn trong một cuộc trò chuyện.
     * @param conversationId ID của cuộc trò chu	yện.
     * @return Danh sách tin nhắn.
     */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatResponse>> getMessages(
            @RequestParam("conversationId") Long conversationId) {
        List<ChatResponse> messages = chatService.getMessagesByConversation(conversationId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Endpoint để lấy tin nhắn phân trang.
     * @param conversationId ID của cuộc trò chuyện.
     * @param page Trang (bắt đầu từ 0).
     * @param size Số tin nhắn mỗi trang.
     * @return Page chứa danh sách tin nhắn.
     */
    @GetMapping("/messages-paged")
    public ResponseEntity<Page<ChatResponse>> getMessagesPaged(
            @RequestParam("conversationId") Long conversationId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        try {
            // Import Page và PageRequest
            org.springframework.data.domain.Page<ChatMessage> messagePage = chatMessageRepository
                .findByConversationOrderByTimestampDesc(
                    conversationRepository.findById(conversationId).orElse(null),
                    org.springframework.data.domain.PageRequest.of(page, size)
                );

            org.springframework.data.domain.Page<ChatResponse> responsePage = messagePage.map(this::convertToChatResponse);
            return ResponseEntity.ok(responsePage);

        } catch (Exception e) {
            log.error("Error fetching paged messages for conversation: " + conversationId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint để lấy trạng thái conversation.
     * @param conversationId ID của cuộc trò chuyện.
     * @return Trạng thái conversation ("AI", "EMP", "WAITING_EMP").
     */
    @GetMapping("/status")
    public ResponseEntity<String> getConversationStatus(@RequestParam("conversationId") Long conversationId) {
        try {
            // Verify conversation exists
            conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

            // Logic để xác định trạng thái conversation
            // Có thể dựa trên các yếu tố như có employee đang chat không, v.v.
            String status = "AI"; // Default to AI

            // Nếu có logic phức tạp hơn, có thể implement sau
            // Ví dụ: kiểm tra xem có employee đang chat với conversation này không

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error fetching conversation status for conversation: " + conversationId, e);
            return ResponseEntity.badRequest().body("AI");
        }
    }

    /**
     * Convert ChatMessage to ChatResponse
     */
    private ChatResponse convertToChatResponse(ChatMessage message) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(message.getConversation().getId());
        response.setSender(message.getSenderName());
        response.setMessage(message.getContent()); // Map content to message
        response.setIsFromAI(message.getIsFromAI());
        response.setStatus("SENT");
        response.setMessageId(message.getId());
        response.setTimestamp(message.getTimestamp());
        response.setConversationStatus("AI"); // Default status
        response.setIsTyping(false);
        response.setTypingMessage(null);
        return response;
    }

    /**
     * Endpoint to get conversation IDs for a user by email.
     * @param userEmail User's email.
     * @return List of conversation IDs.
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Long>> getConversations(
            @RequestParam("userEmail") String userEmail) {
        List<Long> conversationIds = chatService.getConversationsByEmail(userEmail);
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
            String cleanedText = chatServiceImpl.cleanMarkdown(markdownText);

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
    
    /**
     * Test endpoint to verify transaction rollback fix
     */
    @PostMapping("/test-transaction")
    public ResponseEntity<Map<String, Object>> testTransactionFix(
            @RequestParam String userEmail,
            @RequestParam String testMessage) {

        Map<String, Object> result = new HashMap<>();
        result.put("testStarted", true);
        result.put("userEmail", userEmail);
        result.put("testMessage", testMessage);
        result.put("timestamp", java.time.LocalDateTime.now());

        try {
            // Test the actual sendMessage method
            ChatRequest testRequest = new ChatRequest();
            testRequest.setMessage(testMessage);
            testRequest.setConversationId(null);

            ChatResponse response = chatService.sendMessage(userEmail, testRequest);

            result.put("success", true);
            result.put("response", response);
            result.put("message", "Transaction completed successfully without rollback");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("message", "Transaction test failed");

            // Check if it's a rollback exception
            if (e.getMessage() != null && e.getMessage().contains("rollback")) {
                result.put("isRollbackError", true);
                result.put("rollbackFixStatus", "FAILED - Transaction still rolling back");
            } else {
                result.put("isRollbackError", false);
                result.put("rollbackFixStatus", "UNKNOWN - Different error type");
            }

            log.error("Transaction test failed", e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Test endpoint để kiểm tra function calling hoạt động
     */
    @PostMapping("/test-function-calling")
    public ResponseEntity<Map<String, Object>> testFunctionCalling(
            @RequestParam String userEmail,
            @RequestParam String testMessage) {

        Map<String, Object> result = new HashMap<>();
        result.put("testType", "Function Calling Test");
        result.put("testMessage", testMessage);
        result.put("userEmail", userEmail);
        result.put("timestamp", java.time.LocalDateTime.now());

        try {
            // Test với các message khác nhau để kiểm tra function calling
            ChatRequest testRequest = new ChatRequest();
            testRequest.setMessage(testMessage);
            testRequest.setConversationId(null);
            testRequest.setSenderRole("PET_OWNER");

            log.info("=== TESTING FUNCTION CALLING ===");
            log.info("Test message: {}", testMessage);

            ChatResponse response = chatService.sendMessage(userEmail, testRequest);

            result.put("success", true);
            result.put("response", response);
            result.put("hasAdoptionData", response.getAdoptionData() != null);
            result.put("adoptionData", response.getAdoptionData());

            // Analyze results
            if (response.getAdoptionData() != null) {
                result.put("functionCallingStatus", "SUCCESS - AI called tools");
                result.put("petsReturned", response.getAdoptionData().getAdoption() != null ?
                          response.getAdoptionData().getAdoption().size() : 0);
                result.put("message", response.getAdoptionData().getMessage());
            } else {
                result.put("functionCallingStatus", "NO TOOLS CALLED - AI answered from knowledge");
                result.put("message", "AI không gọi tools, trả lời từ kiến thức có sẵn");
            }

            log.info("Function calling test result: {}", result.get("functionCallingStatus"));
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("functionCallingStatus", "ERROR - Function calling failed");

            log.error("Function calling test failed", e);
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get function calling statistics
     */
    @GetMapping("/function-calling-stats")
    public ResponseEntity<Map<String, Object>> getFunctionCallingStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("functionCallingEnabled", true);
        stats.put("availableTools", List.of(
            "searchAdoptionListings - Tìm kiếm thú cưng theo tiêu chí",
            "getAdoptionListingDetails - Lấy chi tiết thú cưng cụ thể",
            "getAdoptionStatistics - Thống kê số lượng thú cưng",
            "findMatchingPets - Tìm thú cưng phù hợp sở thích"
        ));
        stats.put("springAiVersion", "1.0.0-M6");
        stats.put("entityExtraction", "AdoptionListingsAiResponse.class");
        stats.put("debugLoggingEnabled", true);
        stats.put("testEndpoint", "/apis/v1/chat/test-function-calling");

        return ResponseEntity.ok(stats);
    }

    /**
     * Create sample adoption listings for testing
     */
    @PostMapping("/create-sample-data")
    public ResponseEntity<Map<String, Object>> createSampleData() {
        Map<String, Object> result = new HashMap<>();
        result.put("action", "Create Sample Adoption Data");
        result.put("timestamp", java.time.LocalDateTime.now());

        try {
            // Note: This would need AdoptionListingsService to be injected
            // For now, return message to create data manually or via SQL

            result.put("success", false);
            result.put("message", "Please create sample data manually via database or API calls");
            result.put("instructions", "Use POST /apis/v1/adoption-listings to create pets with adoptionStatus='PENDING'");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
