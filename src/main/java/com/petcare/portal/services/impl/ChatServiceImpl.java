package com.petcare.portal.services.impl;

import com.petcare.portal.dtos.ChatRequest;
import com.petcare.portal.dtos.ChatResponse;
import com.petcare.portal.entities.ChatMessage;
import com.petcare.portal.entities.Conversation;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.ChatMessageRepository;
import com.petcare.portal.repositories.ConversationRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository customerRepository;
    private final ChatClient chatClient;
    private final ResourceLoader resourceLoader;
    private final ModelMapper modelMapper;

    @Value("${petcare.ai.max-messages-before-summary:20}")
    private int maxMessagesBeforeSummary;

    @Value("${petcare.ai.system-prompt-path:classpath:prompts/PetCareSystemPrompt.md}")
    private String systemPromptPath;

    @Value("${petcare.ai.context-max-messages:10}")
    private int contextMaxMessages;

    @Override
    @Transactional
    public ChatResponse sendMessage(Long customerId, ChatRequest request) {
        try {
            // Tìm hoặc tạo conversation
            Conversation conversation = findOrCreateConversation(customerId, request.getConversationId());

            // Tạo tin nhắn từ user
            ChatMessage userMessage = createUserMessage(conversation, request.getMessage(), customerId);
            chatMessageRepository.save(userMessage);

            // Lấy số lượng tin nhắn để quyết định strategy
            long messageCount = chatMessageRepository.countByConversation(conversation);
            boolean needsSummary = messageCount > maxMessagesBeforeSummary;

            List<ChatMessage> contextMessages;
            if (needsSummary) {
                // Chỉ lấy messages gần nhất cho context
                Pageable pageable = PageRequest.of(0, contextMaxMessages,
                    Sort.by(Sort.Direction.DESC, "timestamp"));
                Page<ChatMessage> messagePage = chatMessageRepository
                    .findByConversationOrderByTimestampDesc(conversation, pageable);
                contextMessages = messagePage.getContent();
                // Reverse để có thứ tự đúng
                java.util.Collections.reverse(contextMessages);
            } else {
                // Lấy tất cả messages khi chưa cần summary
                contextMessages = chatMessageRepository.findByConversation(conversation);
            }

            String aiResponse;
            if (needsSummary && conversation.getSummary() == null) {
                // Tạo tóm tắt từ tất cả messages và lưu vào database
                List<ChatMessage> allMessages = chatMessageRepository.findByConversation(conversation);
                String summary = generateSummary(allMessages);
                conversation.setSummary(summary);
                conversationRepository.save(conversation);

                // Tạo context với tóm tắt
                aiResponse = generateAIResponseWithSummary(conversation, userMessage, summary);
            } else if (needsSummary && conversation.getSummary() != null) {
                // Sử dụng tóm tắt đã có
                aiResponse = generateAIResponseWithSummary(conversation, userMessage, conversation.getSummary());
            } else {
                // Không cần tóm tắt, sử dụng context messages
                aiResponse = generateAIResponse(conversation, contextMessages);
            }

            // Lưu phản hồi từ AI
            ChatMessage aiMessage = createAIMessage(conversation, aiResponse, customerId);
            chatMessageRepository.save(aiMessage);

            return new ChatResponse(
                conversation.getId(),
                "PetCare AI",
                aiResponse,
                true,
                "COMPLETED", // status
                aiMessage.getId(), // messageId
                aiMessage.getTimestamp(), // timestamp
                "AI", // conversationStatus
                false, // isTyping
                null // typingMessage
            );

        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return new ChatResponse(
                request.getConversationId(),
                "PetCare AI",
                "Xin lỗi, có lỗi xảy ra khi xử lý tin nhắn của bạn. Vui lòng thử lại sau.",
                true,
                "ERROR", // status
                null, // messageId
                LocalDateTime.now(), // timestamp
                "AI", // conversationStatus
                false, // isTyping
                null // typingMessage
            );
        }
    }

    @Override
    public List<ChatResponse> getMessagesByConversation(Long conversationId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatMessage> messages = chatMessageRepository.findByConversation(conversationOpt.get());
        return messages.stream()
                .map(this::convertToChatResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getConversationsByCustomer(Long customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }

        User customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        List<Conversation> conversations = conversationRepository.findByCustomer(customer);
        return conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());
    }

    private Conversation findOrCreateConversation(Long customerId, Long conversationId) {
        if (conversationId != null) {
            Optional<Conversation> existingConversation = conversationRepository.findById(conversationId);
            if (existingConversation.isPresent()) {
                return existingConversation.get();
            }
        }

        // Tạo conversation mới
        Conversation newConversation = new Conversation();
        newConversation.setTitle("Cuộc trò chuyện mới");
        newConversation.setStartTime(LocalDateTime.now());

        if (customerId != null) {
            User customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
            newConversation.setCustomer(customer);
        }

        return conversationRepository.save(newConversation);
    }

    private ChatMessage createUserMessage(Conversation conversation, String message, Long customerId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setContent(message);
        chatMessage.setSenderName("User");
        chatMessage.setIsFromAI(false);
        chatMessage.setTimestamp(LocalDateTime.now());

        if (customerId != null) {
            User customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
            chatMessage.setCustomer(customer);
            chatMessage.setSenderName(customer.getFirstName() + " " + customer.getLastName());
        }

        return chatMessage;
    }

    private ChatMessage createAIMessage(Conversation conversation, String message, Long customerId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setContent(message);
        chatMessage.setSenderName("PetCare AI");
        chatMessage.setIsFromAI(true);
        chatMessage.setTimestamp(LocalDateTime.now());

        if (customerId != null) {
            User customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
            chatMessage.setCustomer(customer);
        }

        return chatMessage;
    }

    private String generateSummary(List<ChatMessage> messages) {
        try {
            log.info("Generating summary for conversation with {} messages", messages.size());

            // Build conversation context với format tốt hơn
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("=== CUỘC TRÒ CHUYỆN VỀ CHĂM SÓC THÚ CƯNG ===\n\n");

            for (ChatMessage msg : messages) {
                String sender = msg.getIsFromAI() ? "🤖 AI: " : "👤 User: ";
                promptBuilder.append(sender).append(msg.getContent()).append("\n");
            }

            // Enhanced summary prompt - NO MARKDOWN
            String summaryPrompt = """
                Nhiệm vụ: Tóm tắt cuộc trò chuyện về chăm sóc thú cưng một cách CHI TIẾT và HỮU ÍCH.

                Yêu cầu tóm tắt PHẢI bao gồm:
                1. Thông tin thú cưng: Loài, tuổi, giống, triệu chứng (nếu có)
                2. Vấn đề chính: Những vấn đề đã được thảo luận
                3. Lời khuyên đã đưa ra: Các giải pháp và hướng dẫn cụ thể
                4. Trạng thái hiện tại: Tình hình sức khỏe sau các lời khuyên
                5. Hành động tiếp theo: Những gì cần làm tiếp theo

                Hướng dẫn:
                - Giữ lại thông tin quan trọng, loại bỏ phần lặp lại
                - Tập trung vào vấn đề sức khỏe và giải pháp
                - Viết bằng tiếng Việt, rõ ràng, logic
                - Độ dài: 200-400 từ
                - KHÔNG sử dụng markdown formatting (không dùng *, **, -, 1. 2. 3., etc.)

                Tóm tắt:
                """;

            String fullPrompt = summaryPrompt + "\n\n" + promptBuilder.toString();

            String summary = chatClient.prompt()
                .system("Bạn là chuyên gia tóm tắt thông tin y tế thú cưng.")
                .user(fullPrompt)
                .call()
                .content();

            log.info("Generated summary with length: {}", summary.length());
            return summary;

        } catch (Exception e) {
            log.error("Error generating summary", e);
            return "Không thể tạo tóm tắt. Cuộc trò chuyện về chăm sóc thú cưng với các vấn đề sức khỏe và lời khuyên.";
        }
    }

    private String generateAIResponseWithSummary(Conversation conversation, ChatMessage userMessage, String summary) {
        try {
            log.debug("Generating AI response with summary for conversation: {}", conversation.getId());

            String systemPrompt = loadSystemPrompt();

            // Enhanced context with summary
            String contextPrompt = String.format("""
                === THÔNG TIN LỊCH SỬ CUỘC TRÒ CHUYỆN ===
                %s

                === CÂU HỎI MỚI CỦA NGƯỜI DÙNG ===
                %s

                === HƯỚNG DẪN TRẢ LỜI ===
                - Sử dụng thông tin từ lịch sử để trả lời phù hợp
                - Tham chiếu lại các vấn đề đã thảo luận trước đây
                - Đảm bảo tính liên tục và logic trong lời khuyên
                - Nếu cần, đề cập lại các triệu chứng hoặc vấn đề đã được nói đến
                - Trả lời bằng tiếng Việt một cách thân thiện và chuyên nghiệp
                """, summary, userMessage.getContent());

            String fullPrompt = systemPrompt + "\n\n" + contextPrompt;

            log.debug("Full prompt length: {}", fullPrompt.length());

            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(contextPrompt)
                .call()
                .content();

            log.debug("Generated response length: {}", response.length());

            // Clean markdown from AI response
            String cleanedResponse = cleanMarkdown(response);
            log.debug("Cleaned response length: {}", cleanedResponse.length());

            return cleanedResponse;

        } catch (Exception e) {
            log.error("Error generating AI response with summary", e);
            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.";
        }
    }

    private String generateAIResponse(Conversation conversation, List<ChatMessage> messages) {
        try {
            log.debug("Generating AI response for conversation {} with {} messages",
                     conversation.getId(), messages.size());

            String systemPrompt = loadSystemPrompt();

            // Smart context building - ưu tiên tin nhắn quan trọng
            String context = buildSmartContext(messages);

            String fullPrompt = systemPrompt + "\n\n=== LỊCH SỬ CUỘC TRÒ CHUYỆN ===\n" + context +
                              "\n\n=== CÂU HỎI MỚI ===";

            log.debug("Context length: {}, Full prompt length: {}", context.length(), fullPrompt.length());

            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(fullPrompt)
                .call()
                .content();

            log.debug("Generated response length: {}", response.length());

            // Clean markdown from AI response
            String cleanedResponse = cleanMarkdown(response);
            log.debug("Cleaned response length: {}", cleanedResponse.length());

            return cleanedResponse;

        } catch (Exception e) {
            log.error("Error generating AI response", e);
            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.";
        }
    }

    /**
     * Xây dựng context thông minh - ưu tiên tin nhắn quan trọng
     */
    private String buildSmartContext(List<ChatMessage> messages) {
        if (messages.isEmpty()) return "";

        StringBuilder context = new StringBuilder();
        List<String> importantKeywords = List.of(
            "bệnh", "ốm", "triệu chứng", "thuốc", "bác sĩ", "khám", "tiêm",
            "ăn", "uống", "đau", "sốt", "ói", "tiêu chảy", "ho", "hắt hơi"
        );

        // Lấy tất cả messages gần nhất
        int startIndex = Math.max(0, messages.size() - contextMaxMessages);

        for (int i = startIndex; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            String sender = msg.getIsFromAI() ? "🤖 AI: " : "👤 User: ";

            // Đánh dấu tin nhắn quan trọng
            boolean isImportant = importantKeywords.stream()
                .anyMatch(keyword -> msg.getContent().toLowerCase().contains(keyword));

            if (isImportant) {
                sender = "🚨 " + sender; // Đánh dấu tin nhắn quan trọng
            }

            context.append(sender).append(msg.getContent()).append("\n");
        }

        return context.toString();
    }

    private ChatResponse convertToChatResponse(ChatMessage message) {
        ChatResponse response = modelMapper.map(message, ChatResponse.class);
        response.setConversationId(message.getConversation().getId());
        response.setMessageId(message.getId());
        response.setTimestamp(message.getTimestamp());
        response.setStatus("SENT");
        response.setConversationStatus("AI");
        response.setIsTyping(false);
        response.setTypingMessage(null);
        return response;
    }

    // Alternative manual conversion if ModelMapper has issues
    private ChatResponse convertToChatResponseManual(ChatMessage message) {
        return new ChatResponse(
            message.getConversation().getId(),
            message.getSenderName(),
            message.getContent(),
            message.getIsFromAI(),
            "SENT", // status
            message.getId(), // messageId
            message.getTimestamp(), // timestamp
            "AI", // conversationStatus
            false, // isTyping
            null // typingMessage
        );
    }

    /**
     * Update summary khi conversation có thêm nhiều messages
     */
    @Transactional
    public void updateConversationSummaryIfNeeded(Long conversationId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

            long messageCount = chatMessageRepository.countByConversation(conversation);

            // Update summary nếu có thêm 10 messages mới
            if (conversation.getSummary() != null && messageCount % 10 == 0) {
                log.info("Updating summary for conversation {} with {} messages", conversationId, messageCount);

                List<ChatMessage> allMessages = chatMessageRepository.findByConversation(conversation);
                String newSummary = generateSummary(allMessages);

                conversation.setSummary(newSummary);
                conversationRepository.save(conversation);

                log.info("Updated summary for conversation {}", conversationId);
            }
        } catch (Exception e) {
            log.error("Error updating conversation summary", e);
        }
    }

    /**
     * Force regenerate summary for debugging
     */
    @Transactional
    public String regenerateSummary(Long conversationId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

            List<ChatMessage> allMessages = chatMessageRepository.findByConversation(conversation);
            String newSummary = generateSummary(allMessages);

            conversation.setSummary(newSummary);
            conversationRepository.save(conversation);

            return newSummary;
        } catch (Exception e) {
            log.error("Error regenerating summary", e);
            return "Error regenerating summary: " + e.getMessage();
        }
    }

    /**
     * Public method to test markdown cleaning (for testing purposes)
     */
    public String cleanMarkdownForTesting(String text) {
        return cleanMarkdown(text);
    }

    /**
     * Clean markdown formatting from AI response
     */
    private String cleanMarkdown(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        try {
            // Remove bold/italic markdown
            text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // **bold**
            text = text.replaceAll("\\*(.*?)\\*", "$1");       // *italic*
            text = text.replaceAll("__(.*?)__", "$1");         // __underline__
            text = text.replaceAll("_(.*?)_", "$1");           // _italic_

            // Remove headers
            text = text.replaceAll("^#{1,6}\\s+", "");         // # ## ### headers
            text = text.replaceAll("\n#{1,6}\\s+", "\n");      // headers on new lines

            // Remove code blocks
            text = text.replaceAll("```[\\s\\S]*?```", "");    // ```code blocks```
            text = text.replaceAll("`([^`]*)`", "$1");         // `inline code`

            // Remove links but keep text
            text = text.replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1"); // [text](url)

            // Clean up excessive newlines
            text = text.replaceAll("\n{3,}", "\n\n");          // max 2 consecutive newlines

            // Remove list markers but keep content
            text = text.replaceAll("^[-*+]\\s+", "• ");        // unordered lists
            text = text.replaceAll("^\\d+\\.\\s+", "");        // ordered lists

            // Remove horizontal rules
            text = text.replaceAll("^[-*_]{3,}$", "");         // --- or *** or ___

            // Clean up extra whitespace
            text = text.trim();
            text = text.replaceAll("\\s+", " ");              // multiple spaces to single
            text = text.replaceAll("\\s*\n\\s*", "\n");       // clean line breaks

            log.debug("Markdown cleaned successfully");
            return text;

        } catch (Exception e) {
            log.error("Error cleaning markdown", e);
            return text; // Return original text if cleaning fails
        }
    }

    private String loadSystemPrompt() {
        try {
            Resource resource = resourceLoader.getResource(systemPromptPath);
            if (resource.exists()) {
                return new String(java.nio.file.Files.readAllBytes(resource.getFile().toPath()));
            }
        } catch (Exception e) {
            log.warn("Could not load system prompt from {}, using default", systemPromptPath, e);
        }
        return getDefaultSystemPrompt();
    }

    private String loadSystemPromptForSummary() {
        return "Bạn là trợ lý AI chuyên về chăm sóc thú cưng. Hãy tóm tắt cuộc trò chuyện một cách chính xác và hữu ích.";
    }

    private String getDefaultSystemPrompt() {
        return """
            Bạn là trợ lý AI chuyên về chăm sóc thú cưng cho PetCare Portal.

            Nhiệm vụ của bạn:
            - Cung cấp thông tin chính xác về chăm sóc thú cưng
            - Hướng dẫn chủ nuôi cách chăm sóc thú cưng đúng cách
            - Tư vấn về dinh dưỡng, sức khỏe, và hành vi của thú cưng
            - Khuyến khích phòng ngừa bệnh tật và kiểm tra sức khỏe định kỳ
            - Hỗ trợ giải đáp thắc mắc về các vấn đề thường gặp

            Nguyên tắc hoạt động:
            - Luôn trả lời bằng tiếng Việt một cách thân thiện và dễ hiểu
            - Không đưa ra chẩn đoán bệnh cụ thể
            - Luôn khuyến khích tham khảo ý kiến bác sĩ thú y khi cần thiết
            - Tập trung vào việc giáo dục và hướng dẫn chủ nuôi
            - Sử dụng ngôn ngữ tích cực và khuyến khích

            Khi trả lời:
            - Lắng nghe và thấu hiểu lo lắng của chủ nuôi
            - Cung cấp thông tin dựa trên kiến thức chuyên môn
            - Đưa ra lời khuyên thực tế và khả thi
            - Khuyến khích sự tương tác tích cực với thú cưng
            - Hướng dẫn chủ nuôi nhận biết dấu hiệu bất thường

            Định dạng văn bản:
            - KHÔNG sử dụng markdown formatting (**bold**, *italic*, headers, lists, etc.)
            - Sử dụng plain text thuần túy để dễ đọc trên chat apps
            - Viết tự nhiên như đang trò chuyện với chủ nuôi
            """;
    }
}
