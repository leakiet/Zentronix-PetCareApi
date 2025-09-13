package com.petcare.portal.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.portal.dtos.ChatRequest;
import com.petcare.portal.dtos.ChatResponse;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsAiResponse;
import com.petcare.portal.tools.Tools;
import com.petcare.portal.entities.ChatMessage;
import com.petcare.portal.entities.Conversation;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.ChatMessageRepository;
import com.petcare.portal.repositories.ConversationRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final ResourceLoader resourceLoader;
    private final Tools tools;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${petcare.ai.max-messages-before-summary:20}")
    private int maxMessagesBeforeSummary;

    @Value("${petcare.ai.system-prompt-path:classpath:prompts/PetCareSystemPrompt.md}")
    private String systemPromptPath;

    @Value("${petcare.ai.context-max-messages:10}")
    private int contextMaxMessages;

    @Override
    @Transactional
    public ChatResponse sendMessage(String email, ChatRequest request) {
        try {
            // Validate input
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                throw new IllegalArgumentException("Message cannot be null or empty");
            }
            if (request.getSenderRole() == null || request.getSenderRole().trim().isEmpty()) {
                throw new IllegalArgumentException("SenderRole cannot be null or empty");
            }

            // Find user (null for guest users)
            User user = email != null && !email.trim().isEmpty() ?
                userRepository.findByEmail(email) : null;
            if (email != null && !email.trim().isEmpty() && user == null) {
                    throw new IllegalArgumentException("User not found with email: " + email);
            }

            Conversation conversation = findOrCreateConversation(
                user != null ? user.getId() : null, request.getConversationId());

            String cleanedUserMessage = cleanEmojiAndUnicode(request.getMessage());
            ChatMessage userMessage = createUserMessage(conversation, cleanedUserMessage,
                user != null ? user.getId() : null, request.getSenderRole());
            chatMessageRepository.save(userMessage);

            long messageCount = chatMessageRepository.countByConversation(conversation);
            boolean needsSummary = messageCount > maxMessagesBeforeSummary;

            List<ChatMessage> contextMessages = getContextMessages(conversation, needsSummary);

            ChatResponse aiResponse = generateAIResponse(conversation, contextMessages, userMessage,
                request.getMessage(), needsSummary);

            String cleanedAiResponse = cleanEmojiAndUnicode(aiResponse.getMessage());
            ChatMessage aiMessage = createAIMessage(conversation, cleanedAiResponse,
                user != null ? user.getId() : null);
            chatMessageRepository.save(aiMessage);

            if (cleanedAiResponse == null || cleanedAiResponse.trim().isEmpty()) {
                cleanedAiResponse = "Sorry, I can't respond right now. Please try again.";
                aiResponse.setMessage(cleanedAiResponse);
            }

            aiResponse.setMessageId(aiMessage.getId());
            aiResponse.setTimestamp(aiMessage.getTimestamp());
            return aiResponse;

        } catch (Exception e) {
            log.error("Error processing chat message", e);
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setConversationId(request.getConversationId());
            errorResponse.setSender("PetCare AI");
            errorResponse.setMessage("Sorry, an error occurred while processing your message. Please try again later.");
            errorResponse.setIsFromAI(true);
            errorResponse.setStatus("ERROR");
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setConversationStatus("AI");
            errorResponse.setAdoptionData(null);
            return errorResponse;
        }
    }

    private List<ChatMessage> getContextMessages(Conversation conversation, boolean needsSummary) {
        if (needsSummary) {
            Pageable pageable = PageRequest.of(0, contextMaxMessages,
                Sort.by(Sort.Direction.DESC, "timestamp"));
            Page<ChatMessage> page = chatMessageRepository
                .findByConversationOrderByTimestampDesc(conversation, pageable);
            List<ChatMessage> messages = new ArrayList<>(page.getContent());
            Collections.reverse(messages);
            return messages;
        }
        return chatMessageRepository.findByConversation(conversation);
    }

    private ChatResponse generateAIResponse(Conversation conversation, List<ChatMessage> contextMessages,
            ChatMessage userMessage, String userQuery, boolean needsSummary) {
        // Send typing start event immediately
        sendTypingStartEvent(conversation, getTypingMessageForQuery(userQuery));

        ChatResponse response;
        try {
            // Check if user query needs function calling (adoption-related)
            if (isAdoptionRelatedQuery(userQuery)) {
                // Use function calling for adoption/pet finding queries
                response = generateAIResponseWithTools(conversation, contextMessages, userQuery);
            } else {
                // Use regular AI response for general chat/care questions
                response = generateAIResponseWithoutTools(conversation, contextMessages, userQuery);
            }
        } finally {
            // Send typing stop event
            sendTypingStopEvent(conversation);
        }

        return response;
    }

    private ChatResponse createResponse(Long conversationId, String message, AdoptionListingsAiResponse adoptionData) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(conversationId);
        response.setSender("PetCare AI");
        response.setMessage(message);
        response.setIsFromAI(true);
        response.setStatus("COMPLETED");
        response.setTimestamp(LocalDateTime.now());
        response.setConversationStatus("AI");
        response.setAdoptionData(adoptionData);
        return response;
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
    public List<Long> getConversationsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ArrayList<>();
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }

        List<Conversation> conversations = conversationRepository.findByUser(user);
        return conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());
    }

    private Conversation findOrCreateConversation(Long userId, Long conversationId) {
        if (conversationId != null) {
            Optional<Conversation> existingConversation = conversationRepository.findById(conversationId);
            if (existingConversation.isPresent()) {
                return existingConversation.get();
            }
        }

        // Create new conversation
        Conversation newConversation = new Conversation();
        newConversation.setTitle("New Conversation");
        newConversation.setStartTime(LocalDateTime.now());

        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            newConversation.setUser(user);
        }

        return conversationRepository.save(newConversation);
    }

    private ChatMessage createUserMessage(Conversation conversation, String message, Long userId, String senderRole) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setContent(message);
        chatMessage.setSenderName("User");
        chatMessage.setIsFromAI(false);
        chatMessage.setTimestamp(LocalDateTime.now());

        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            chatMessage.setUser(user);
            chatMessage.setSenderName(user.getFirstName() + " " + user.getLastName());
        }

        // Can save senderRole to ChatMessage if needed, but currently no such field in entity
        // Log senderRole for debugging
        log.debug("Creating user message with senderRole: {}", senderRole);

        return chatMessage;
    }

    private ChatMessage createAIMessage(Conversation conversation, String message, Long userId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setContent(message);
        chatMessage.setSenderName("PetCare AI");
        chatMessage.setIsFromAI(true);
        chatMessage.setTimestamp(LocalDateTime.now());

        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            chatMessage.setUser(user);
        }

        return chatMessage;
    }

    private String generateSummary(List<ChatMessage> messages) {
        try {
            StringBuilder context = new StringBuilder("=== PET CARE CONVERSATION ===\n\n");
            for (ChatMessage msg : messages) {
                String sender = msg.getIsFromAI() ? "🤖 AI: " : "👤 User: ";
                context.append(sender).append(msg.getContent()).append("\n");
            }

            String prompt = """
                SUMMARIZE the pet care conversation concisely.
                FOCUS ONLY on:
                - Current pet information (if any)
                - Unresolved or ongoing health issues
                - Important advice given
                - Current status

                IMPORTANT: Keep summary SHORT (under 150 words), only ESSENTIAL information.
                DO NOT repeat unnecessary details. Write in English.
                Summary:
                """ + context;

            return chatClient.prompt()
                .system("You are an expert at summarizing pet medical information.")
                .user(prompt)
                .call()
                .content();

        } catch (Exception e) {
            log.error("Error generating summary", e);
            return "Unable to generate summary. Conversation about pet care.";
        }
    }

    private String generateAIResponseWithSummary(Conversation conversation, ChatMessage userMessage, String summary, String userQuery) {
        try {
            String systemPrompt = loadSystemPrompt();
            String contextPrompt = String.format("""
                === CONVERSATION HISTORY SUMMARY (FOR REFERENCE) ===
                %s

                === CURRENT QUESTION (PRIORITY RESPONSE) ===
                %s

                === RESPONSE GUIDELINES ===
                - PRIORITIZE answering the current question directly
                - Only use information from summary when NECESSARY for supplementation
                - DO NOT repeat entire history unless specifically asked
                - Focus on solving the user's current problem
                - If new question is different topic, answer independently from history
                - Respond in English in a friendly and professional manner
                """, summary, userMessage.getContent());

            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(contextPrompt)
                .call()
                .content();

            return cleanMarkdown(response);

        } catch (Exception e) {
            log.error("Error generating AI response with summary", e);
            return "Sorry, I cannot process your request at this time. Please try again later.";
        }
    }

    private String generateAIResponse(Conversation conversation, List<ChatMessage> messages) {
        try {
            String systemPrompt = loadSystemPrompt();
            String context = buildSmartContext(messages);

            // Get the latest user message for intent analysis
            String latestUserMessage = getLatestUserMessage(messages);

            String prompt = systemPrompt + "\n\n" +
                "=== CONVERSATION HISTORY (FOR REFERENCE) ===\n" + context + "\n\n" +
                "=== CURRENT QUESTION (PRIORITY RESPONSE) ===\n" + latestUserMessage + "\n\n" +
                "=== RESPONSE GUIDELINES ===\n" +
                "- PRIORITIZE answering the current question directly\n" +
                "- Only reference history when NECESSARY to provide additional information\n" +
                "- DO NOT repeat history unless asked\n" +
                "- Focus on solving the user's current problem\n" +
                "- If new question is completely different topic, answer independently";

            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

            return cleanMarkdown(response);

        } catch (Exception e) {
            log.error("Error generating AI response", e);
            return "Sorry, I cannot process your request at this time. Please try again later.";
        }
    }

    /**
     * Generate AI response without function calling for general chat/care questions
     */
    private ChatResponse generateAIResponseWithoutTools(Conversation conversation, List<ChatMessage> messages, String userQuery) {
        try {
            String systemPrompt = loadSystemPrompt();
            String context = buildSmartContext(messages);

            // Get the latest user message for intent analysis
            String latestUserMessage = getLatestUserMessage(messages);

            String prompt = systemPrompt + "\n\n" +
                "=== CONVERSATION HISTORY (FOR REFERENCE) ===\n" + context + "\n\n" +
                "=== CURRENT QUESTION (PRIORITY RESPONSE) ===\n" + latestUserMessage + "\n\n" +
                "=== RESPONSE GUIDELINES ===\n" +
                "- PRIORITIZE answering the current question directly\n" +
                "- Only reference history when NECESSARY to provide additional information\n" +
                "- DO NOT repeat history unless asked\n" +
                "- Focus on solving the user's current problem\n" +
                "- If new question is different topic, answer independently\n" +
                "- DO NOT use tools or function calling for this question";

            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

            String cleanedResponse = cleanMarkdown(response);

            return new ChatResponse(
                conversation.getId(),
                "PetCare AI",
                cleanedResponse,
                true,
                "COMPLETED",
                null, // messageId will be set when saved
                LocalDateTime.now(),
                "AI",
                false,
                null,
                null // No adoption data for non-adoption queries
            );

        } catch (Exception e) {
            log.error("Error generating AI response without tools", e);
            return new ChatResponse(
                conversation.getId(),
                "PetCare AI",
                "Sorry, I cannot process your request at this time. Please try again later.",
                true,
                "ERROR",
                null,
                LocalDateTime.now(),
                "AI",
                false,
                null,
                null
            );
        }
    }

    private String getLatestUserMessage(List<ChatMessage> messages) {
        // Find the most recent user message
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (!msg.getIsFromAI() && msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
                return msg.getContent();
            }
        }
        return ""; // Fallback if no user message found
    }

    private String buildSmartContext(List<ChatMessage> messages) {
        if (messages.isEmpty()) return "";

        StringBuilder context = new StringBuilder();

        // Include conversation summary if available (from conversation entity)
        // Summary will be added to context so AI understands long-term history

        String latestUserMessage = getLatestUserMessage(messages).toLowerCase();
        List<String> keywords = extractKeywordsFromLatestMessage(latestUserMessage);

        // Limit context to avoid token limits
        int startIndex = Math.max(0, messages.size() - contextMaxMessages);

        // Only include messages that are relevant to current topic
        for (int i = startIndex; i < messages.size() - 1; i++) { // Exclude latest message
            ChatMessage msg = messages.get(i);
            String content = msg.getContent();
            if (content == null || content.trim().isEmpty()) continue;

            String lowerContent = content.toLowerCase();
            boolean isRelevant = keywords.isEmpty() || // Include if no specific keywords
                keywords.stream().anyMatch(lowerContent::contains) ||
                isRecentMessage(i, messages.size()); // Always include recent messages

            if (isRelevant) {
                String sender = msg.getIsFromAI() ? "🤖 AI: " : "👤 User: ";
                context.append(sender).append(content).append("\n");
            }
        }
        return context.toString().trim();
    }

    private List<String> extractKeywordsFromLatestMessage(String message) {
        // Extract key topics from the latest user message (Vietnamese + English)
        List<String> allKeywords = List.of(
            // Vietnamese health keywords - từ khóa sức khỏe tiếng Việt
            "bệnh", "ốm", "triệu chứng", "thuốc", "bác sĩ", "khám", "tiêm",
            "ăn", "uống", "đau", "sốt", "ói", "tiêu chảy", "ho", "hắt hơi",
            "khỏe", "ốm yếu", "kiểm tra", "chữa trị",

            // English health keywords - từ khóa sức khỏe tiếng Anh
            "sick", "ill", "symptoms", "medicine", "doctor", "veterinarian", "checkup", "vaccine",
            "eat", "drink", "pain", "fever", "vomit", "diarrhea", "cough", "sneeze",
            "healthy", "weak", "treatment", "diagnosis",

            // Vietnamese adoption keywords - từ khóa nhận nuôi tiếng Việt
            "nhận nuôi", "thú cưng", "chó", "mèo", "chim", "nuôi",

            // English adoption keywords - từ khóa nhận nuôi tiếng Anh
            "adopt", "adoption", "pet", "dog", "cat", "bird", "adoptable",

            // Training/behavior keywords - từ khóa huấn luyện/hành vi
            "huấn luyện", "hành vi", "ngoan", "cắn", "training", "behavior", "bite", "good boy"
        );

        return allKeywords.stream()
            .filter(message::contains)
            .collect(Collectors.toList());
    }

    private boolean isRecentMessage(int messageIndex, int totalMessages) {
        // Always include messages from the last 3 exchanges (6 messages)
        return (totalMessages - messageIndex) <= 6;
    }

    /**
     * Check if user query is related to adoption/pet finding
     */
    private boolean isAdoptionRelatedQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = userQuery.toLowerCase();

        // Adoption-related keywords (Vietnamese + English mapping)
        List<String> adoptionKeywords = List.of(
            // Vietnamese keywords - nhận nuôi thú cưng
            "nhận nuôi", "tìm thú cưng", "giới thiệu thú cưng", "thú cưng nào",
            "có thú cưng", "thú cưng phù hợp", "muốn nuôi", "tìm chó", "tìm mèo",
            "tìm chim", "chó đực", "chó cái", "mèo đực", "mèo cái", "chim đực", "chim cái",
            "giống đực", "giống cái", "giới tính", "tuổi", "breed", "giống",
            "thông tin thú cưng", "chi tiết thú cưng", "thống kê", "số lượng",
            "danh sách thú cưng", "có chó", "có mèo", "có chim", "tìm kiếm",
            "nuôi chó", "nuôi mèo", "nuôi chim", "muốn nhận", "cần nuôi",

            // English keywords - pet adoption
            "adopt", "adoption", "find pet", "looking for", "search pet",
            "pet available", "pet listings", "available pets", "adoptable",
            "male dog", "female dog", "male cat", "female cat", "male bird", "female bird",
            "dog adoption", "cat adoption", "bird adoption", "pet rescue", "animal shelter",
            "statistics", "pet information", "pet details", "how many pets",
            "pet list", "pet directory", "pet finder", "want to adopt",
            "need pet", "looking for dog", "looking for cat", "looking for bird",
            "puppy adoption", "kitten adoption", "breed", "age", "gender"
        );

        // Check if query contains any adoption-related keywords
        return adoptionKeywords.stream().anyMatch(lowerQuery::contains);
    }

    private ChatResponse convertToChatResponse(ChatMessage message) {
        ChatResponse response = new ChatResponse();
        response.setConversationId(message.getConversation().getId());
        response.setSender(message.getSenderName());
        response.setMessage(message.getContent());
        response.setIsFromAI(message.getIsFromAI());
        response.setStatus("SENT");
        response.setMessageId(message.getId());
        response.setTimestamp(message.getTimestamp());
        response.setConversationStatus("AI");
        return response;
    }


    @Transactional
    public void updateConversationSummaryIfNeeded(Long conversationId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

            long messageCount = chatMessageRepository.countByConversation(conversation);
            if (conversation.getSummary() != null && messageCount % 10 == 0) {
                List<ChatMessage> allMessages = chatMessageRepository.findByConversation(conversation);
                String newSummary = generateSummary(allMessages);
                conversation.setSummary(newSummary);
                conversationRepository.save(conversation);
            }
        } catch (Exception e) {
            log.error("Error updating conversation summary", e);
        }
    }

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

    public String cleanMarkdown(String text) {
        if (text == null || text.isEmpty()) return text;

        try {
            text = text.replaceAll("\\*\\*(.*?)\\*\\*", "$1") // bold
                      .replaceAll("\\*(.*?)\\*", "$1")       // italic
                      .replaceAll("__(.*?)__", "$1")         // underline
                      .replaceAll("_(.*?)_", "$1")           // italic
                      .replaceAll("^#{1,6}\\s+", "")         // headers
                      .replaceAll("\n#{1,6}\\s+", "\n")      // headers on new lines
                      .replaceAll("```[\\s\\S]*?```", "")    // code blocks
                      .replaceAll("`([^`]*)`", "$1")         // inline code
                      .replaceAll("\\[([^\\]]+)\\]\\([^\\)]+\\)", "$1") // links
                      .replaceAll("\n{3,}", "\n\n")          // excessive newlines
                      .replaceAll("^[-*+]\\s+", "• ")        // unordered lists
                      .replaceAll("^\\d+\\.\\s+", "")        // ordered lists
                      .replaceAll("^[-*_]{3,}$", "")         // horizontal rules
                      .trim()
                      .replaceAll("\\s+", " ")              // multiple spaces
                      .replaceAll("\\s*\n\\s*", "\n");       // clean line breaks

            return text;
        } catch (Exception e) {
            log.error("Error cleaning markdown", e);
            return text;
        }
    }

    /**
     * Clean pet lists from AI text response to avoid duplication with structured data
     * FE should render pet cards from adoptionData.adoption, not parse from text
     */
    public String cleanPetListFromText(String text) {
        if (text == null || text.isEmpty()) return text;

        try {
            // Remove numbered pet lists (Vietnamese + English patterns)
            text = text.replaceAll("\\d+\\.\\s*Tên:\\s*[^\\n]+(?:\\n.*?)*?(?=\\d+\\.\\s*Tên:|Hy vọng|$)", ""); // Vietnamese: "1. Tên: ..."
            text = text.replaceAll("\\d+\\.\\s*Name:\\s*[^\\n]+(?:\\n.*?)*?(?=\\d+\\.\\s*Name:|Hope|$)", ""); // English: "1. Name: ..."

            // Remove list introduction patterns (Vietnamese + English)
            text = text.replaceAll("Dưới đây là danh sách[^\\n]*", ""); // Vietnamese: "Dưới đây là danh sách..."
            text = text.replaceAll("Below is the list[^\\n]*", ""); // English: "Below is the list..."
            text = text.replaceAll("Here are the pets[^\\n]*", ""); // English: "Here are the pets..."
            text = text.replaceAll("Danh sách thú cưng[^\\n]*", ""); // Vietnamese: "Danh sách thú cưng..."

            // Remove individual pet mentions with details (Vietnamese + English)
            text = text.replaceAll("chú chó[^\\n]*", ""); // Vietnamese: "chú chó..."
            text = text.replaceAll("con mèo[^\\n]*", ""); // Vietnamese: "con mèo..."
            text = text.replaceAll("chú mèo[^\\n]*", ""); // Vietnamese: "chú mèo..."
            text = text.replaceAll("con chó[^\\n]*", ""); // Vietnamese: "con chó..."
            text = text.replaceAll("puppy[^\\n]*", ""); // English: "puppy..."
            text = text.replaceAll("kitten[^\\n]*", ""); // English: "kitten..."
            text = text.replaceAll("dog[^\\n]*", ""); // English: "dog..."
            text = text.replaceAll("cat[^\\n]*", ""); // English: "cat..."
            text = text.replaceAll("bird[^\\n]*", ""); // English: "bird..."

            // Remove image references
            text = text.replaceAll("!\\w+", ""); // !PetName patterns

            // Remove excessive whitespace and empty lines
            text = text.replaceAll("\\n{3,}", "\n\n").trim();

            // If text becomes too short after cleaning, add a generic message
            if (text.length() < 10) {
                text = "Found pets that match your requirements.";
            }

            return text;

        } catch (Exception e) {
            // Return original text if cleaning fails
            return text;
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


    private String getDefaultSystemPrompt() {
        return """
            You are an AI assistant specialized in pet care for PetCare Portal.

            Your mission:
            - Provide accurate information about pet care
            - Guide pet owners on proper pet care methods
            - Advise on nutrition, health, and pet behavior
            - Support pet adoption when users request
            - Encourage disease prevention and regular health check-ups
            - Help answer common questions and concerns

            IMPORTANT PRINCIPLES:
            - PRIORITIZE answering the user's current question directly
            - Only reference conversation history when NECESSARY
            - DO NOT repeat known information unless asked
            - Focus on solving the current problem
            - Use tools when users ask about pet adoption

            When users ask about pet adoption:
            - Use tools to get accurate information from database
            - Display list of pets matching requirements
            - Provide detailed and accurate information
            - Encourage users to contact shelters directly for adoption

            Operating principles:
            - Always respond in English in a friendly and understandable way
            - Do not provide specific disease diagnoses
            - Always encourage consulting veterinarians when necessary
            - Focus on educating and guiding pet owners
            - Use positive and encouraging language

            When responding:
            - Listen and understand the pet owner's concerns
            - Provide information based on professional knowledge or tools
            - Give practical and feasible advice
            - Encourage positive interaction with pets
            - Guide owners to recognize abnormal signs

            Text formatting:
            - DO NOT use markdown formatting (**bold**, *italic*, headers, lists, etc.)
            - Use plain text only for easy reading on chat apps
            - Write naturally as if conversing with a pet owner
            """;
    }

    private String cleanEmojiAndUnicode(String text) {
        if (text == null || text.isEmpty()) return text;

        try {
            String cleaned = text
                .replaceAll("[\\x{1F600}-\\x{1F64F}]", "") // Emoticons
                .replaceAll("[\\x{1F300}-\\x{1F5FF}]", "") // Misc Symbols and Pictographs
                .replaceAll("[\\x{1F680}-\\x{1F6FF}]", "") // Transport and Map
                .replaceAll("[\\x{1F1E0}-\\x{1F1FF}]", "") // Regional indicator symbol
                .replaceAll("[\\x{2600}-\\x{26FF}]", "")   // Misc symbols
                .replaceAll("[\\x{2700}-\\x{27BF}]", "")   // Dingbats
                .replaceAll("[\\x{10000}-\\x{10ffff}]", "") // Supplementary planes
                .replaceAll("\\u200d", "") // Zero width joiner
                .replaceAll("\\uFE0F", "") // Variation selector-16
                .replaceAll("\\uFE0E", "") // Variation selector-15
                .replaceAll("[\\x{2190}-\\x{21FF}]", "") // Arrows
                .replaceAll("[\\x{20D0}-\\x{20FF}]", "") // Combining Diacritical Marks
                .replaceAll("[\\x{1F900}-\\x{1F9FF}]", "") // Supplemental Symbols
                .replaceAll("[\\x{0000}-\\x{0008}]", "")  // Control chars
                           .replaceAll("[\\x{000B}-\\x{000C}]", "")
                           .replaceAll("[\\x{000E}-\\x{001F}]", "")
                .replaceAll("[\\x{007F}-\\x{009F}]", "")
                .trim()
                .replaceAll("\\s+", " "); // Multiple spaces to single

            return cleaned;
        } catch (Exception e) {
            log.warn("Error cleaning emoji from text", e);
            return text;
        }
    }



    /**
     * Generate AI response using real Spring AI function calling
     */
    private ChatResponse generateAIResponseWithTools(Conversation conversation, List<ChatMessage> messages, String userQuery) {
        try {
            // Load system prompt
            String systemPrompt = loadSystemPrompt();

            // Build smart context từ chat history
            String context = buildSmartContext(messages);
            String latestUserMessage = getLatestUserMessage(messages);

            // Enhanced system prompt để guide AI khi nào gọi tools
            String enhancedSystemPrompt = systemPrompt + "\n\n" +
                "=== FUNCTION CALLING INSTRUCTIONS ===\n" +
                "You have access to these tools for pet adoption queries:\n" +
                "- searchAdoptionListings: Search pets by species, gender, age range\n" +
                "- getAdoptionListingDetails: Get detailed info for specific pet by ID\n" +
                "- getAdoptionStatistics: Get adoption statistics summary\n" +
                "- findMatchingPets: Find pets matching user preferences\n\n" +
                "ALWAYS use tools when users ask about:\n" +
                "- Finding or adopting pets (nhận nuôi, tìm, giới thiệu)\n" +
                "- Pet listings or availability (danh sách, có thú cưng nào)\n" +
                "- Adoption statistics (thống kê, số lượng)\n" +
                "- Specific pet details\n\n" +
                "DO NOT use tools for:\n" +
                "- General pet care advice (chăm sóc, bệnh, thuốc)\n" +
                "- Health/medical questions\n" +
                "- Adoption procedures/policies\n\n" +
                "CRITICAL FILTERING RULES:\n"
                + "1. ONLY use parameters that user EXPLICITLY mentioned\n"
                + "2. If user says 'chó đực' → species='DOG', gender='MALE' (both required)\n"
                + "3. If user says 'chó' only → species='DOG', gender=null (only species)\n"
                + "4. If user says 'đực' only → gender='MALE', species=null (only gender)\n"
                + "5. NEVER add criteria user didn't mention (no colors, no ages, no breeds unless specified)\n"
                + "6. If user wants multiple criteria, they must mention ALL of them\n" +
                "DO NOT add extra criteria like colors, breeds, or ages that user didn't specify.\n\n" +
                "=== VIETNAMESE GENDER MAPPINGS ===\n" +
                "Map Vietnamese gender terms to English values:\n" +
                "- 'đực', 'giống đực', 'giới tính đực','chó đực', 'mèo đực' → gender='MALE'\n" +
                "- 'cái', 'giống cái', 'giới tính cái','chó cái', 'mèo cái' → gender='FEMALE'\n" +
                "- 'male', 'con đực' → gender='MALE'\n" +
                "- 'female', 'con cái' → gender='FEMALE'\n\n" +
                "=== ENGLISH GENDER MAPPINGS ===\n" +
                "Map English gender terms and phrases:\n" +
                "- 'male', 'boy', 'he' → gender='MALE'\n" +
                "- 'female', 'girl', 'she' → gender='FEMALE'\n" +
                "- 'male dog', 'boy dog', 'he dog' → species='DOG' and gender='MALE'\n" +
                "- 'female dog', 'girl dog', 'she dog' → species='DOG' and gender='FEMALE'\n" +
                "- 'male cat', 'boy cat', 'he cat' → species='CAT' and gender='MALE'\n" +
                "- 'female cat', 'girl cat', 'she cat' → species='CAT' and gender='FEMALE'\n\n" +
                "=== SPECIES MAPPINGS ===\n" +
                "Map Vietnamese species terms to English values:\n" +
                "- 'chó', 'dog' → species='DOG'\n" +
                "- 'mèo', 'cat' → species='CAT'\n" +
                "- 'chim', 'bird' → species='BIRD'\n\n" +
                "=== BREED NAME MAPPINGS ===\n" +
                "Common breed names that should be recognized:\n" +
                "- 'Labrador', 'Labrador Retriever' → search for breedId corresponding to Labrador\n" +
                "- 'Golden Retriever', 'Golden' → search for breedId corresponding to Golden Retriever\n" +
                "- 'Persian', 'Ba Tư' → search for breedId corresponding to Persian\n" +
                "- 'Siamese', 'Xiêm' → search for breedId corresponding to Siamese\n\n" +
                "Examples:\n" +
                "- User says 'chó đực' → search for species='DOG' and gender='MALE'\n" +
                "- User says 'giống cái' → search for gender='FEMALE'\n" +
                "- User says 'mèo cái' → search for species='CAT' and gender='FEMALE'\n" +
                "- User says 'chó Labrador giống cái' → search for species='DOG', breed='Labrador', gender='FEMALE'\n" +
                "- User says 'female dog' → search for species='DOG' and gender='FEMALE'\n" +
                "- User says 'male cat' → search for species='CAT' and gender='MALE'\n\n" +
                "Do not assume colors like 'white' unless explicitly stated.\n\n" +
                "When calling tools, use ONLY the parameters user mentioned.\n\n"
                + "TOOL CALLING GUIDELINES:\n"
                + "- Call EXACTLY ONE tool per user request\n"
                + "- Do NOT call multiple tools for the same request\n"
                + "- Do NOT call tools with null parameters unless user explicitly wants general search\n"
                + "- If user wants general search, use species=null, gender=null, etc.\n"
                + "- Always provide page=0, size=10 (or reasonable values) for pagination\n\n"
                + "RESPONSE STRATEGY:\n"
                + "- If tool returns results: Provide SHORT summary in text (e.g., 'Found X pets that match your criteria')\n"
                + "- NEVER include pet lists/details in text response - use structured data instead\n"
                + "- If no pets found: Tell user clearly, set adoptionData.adoption to empty array\n"
                + "- Do NOT automatically try different criteria unless user asks\n\n"
                + "STRUCTURED DATA REQUIREMENTS:\n"
                + "- ALWAYS include structured data when tool returns results\n"
                + "- adoptionData.adoption: Full pet list with details (name, breed, age, description, image)\n"
                + "- adoptionData.message: Short summary message for structured data\n"
                + "- For empty results: adoptionData.adoption = [], message = 'No pets found'\n";

            // Build prompt với context
            String prompt = String.format("""
                === CONVERSATION HISTORY (FOR REFERENCE) ===
                %s

                === CURRENT USER QUESTION ===
                %s

                === RESPONSE GUIDELINES ===
                - Use tools when user asks about pet adoption/finding pets
                - Answer directly from knowledge for general pet care questions
                - Prioritize current question, reference history only when relevant
                - Respond in English/Vietnamese, friendly and professional
                - DO NOT use markdown formatting
                - TEXT RESPONSE: Keep it SHORT and CONCISE - don't include pet lists
                - STRUCTURED DATA: Put ALL pet details in adoptionData.adoption array
                - If no pets found: Clear message, let FE handle empty state via structured data

                Analyze the user's question and respond appropriately, using tools when relevant.
                """, context, latestUserMessage);

            // Execute chat with function calling
            var chatResponse = chatClient.prompt()
                .system(enhancedSystemPrompt)
                .user(prompt)
                .tools(tools)
                .call();

            // DEBUG: Log function calling details
            log.debug("=== FUNCTION CALLING DEBUG ===");
            log.debug("User prompt: {}", prompt);
            log.debug("Available tools: searchAdoptionListings, getAdoptionListingDetails, getAdoptionStatistics, findMatchingPets");

            // Get AI text response
            String aiResponse = chatResponse.content();
            String cleanedResponse = cleanMarkdown(aiResponse);

            // Clean pet lists from text response - let FE use structured data
            cleanedResponse = cleanPetListFromText(cleanedResponse);

            log.debug("AI Response: {}", aiResponse);
            log.debug("Cleaned Response: {}", cleanedResponse);

            // Get structured data from Spring AI function calling
            // Pure function calling - only use Spring AI entity extraction
            AdoptionListingsAiResponse adoptionData = null;
            try {
                adoptionData = chatResponse.entity(AdoptionListingsAiResponse.class);

                if (adoptionData != null && adoptionData.getAdoption() != null && !adoptionData.getAdoption().isEmpty()) {
                    log.debug("✅ FUNCTION CALLING SUCCESS: {} pets returned", adoptionData.getAdoption().size());
                    log.debug("Pet data: {}", adoptionData.getAdoption().stream()
                             .map(p -> p.getPetName() + "(" + p.getSpecies() + ")")
                             .toList());

                    // Ensure adoptionData has proper message
                    if (adoptionData.getMessage() == null || adoptionData.getMessage().isEmpty()) {
                        adoptionData.setMessage("Found " + adoptionData.getAdoption().size() + " pets that match your requirements.");
                    }

                } else if (adoptionData != null) {
                    log.debug("⚠️ FUNCTION CALLING: Tools called but no pets found with specific criteria");

                    // Ensure adoptionData has proper structure for empty results
                    if (adoptionData.getMessage() == null || adoptionData.getMessage().isEmpty()) {
                        adoptionData.setMessage("Currently no pets found that match your criteria.");
                    }
                    if (adoptionData.getAdoption() == null) {
                        adoptionData.setAdoption(List.of());
                    }

                } else {
                    log.debug("ℹ️ NO STRUCTURED DATA: AI responded with text only");
                    // AI didn't call tools, create minimal adoptionData
                    adoptionData = new AdoptionListingsAiResponse();
                    adoptionData.setMessage("I have answered your question.");
                    adoptionData.setAdoption(List.of());
                }
        } catch (Exception e) {
                log.debug("❌ FUNCTION CALLING FAILED: Entity extraction error - {}", e.getMessage());
                log.debug("This means AI did not execute any tools");
                adoptionData = null;
            }

            return new ChatResponse(
                conversation.getId(),
                "PetCare AI",
                cleanedResponse,
                true,
                "COMPLETED",
                null, // messageId will be set when saved
                LocalDateTime.now(),
                "AI",
                false,
                null,
                adoptionData // Structured data from tool calls
            );

        } catch (Exception e) {
            log.error("Error in function calling response generation", e);
            return new ChatResponse(
                conversation.getId(),
                "PetCare AI",
                "Sorry, I cannot process your request at this time. Please try again later.",
                true,
                "ERROR",
                null,
                LocalDateTime.now(),
                "AI",
                false,
                null,
                null
            );
        }
    }

    /**
     * Send typing start event via WebSocket
     */
    private void sendTypingStartEvent(Conversation conversation, String typingMessage) {
        try {
            Map<String, Object> typingEvent = new HashMap<>();
            typingEvent.put("type", "TYPING_START");
            typingEvent.put("conversationId", conversation.getId());
            typingEvent.put("sender", "PetCare AI");
            typingEvent.put("timestamp", LocalDateTime.now());
            typingEvent.put("message", typingMessage);
            typingEvent.put("isTyping", true);

            messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getId(),
                typingEvent
            );

            log.debug("Sent TYPING_START event for conversation: {}", conversation.getId());
        } catch (Exception e) {
            log.warn("Failed to send typing start event", e);
        }
    }

    /**
     * Send typing stop event via WebSocket
     */
    private void sendTypingStopEvent(Conversation conversation) {
        try {
            Map<String, Object> typingEvent = new HashMap<>();
            typingEvent.put("type", "TYPING_STOP");
            typingEvent.put("conversationId", conversation.getId());
            typingEvent.put("sender", "PetCare AI");
            typingEvent.put("timestamp", LocalDateTime.now());
            typingEvent.put("isTyping", false);

            messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getId(),
                typingEvent
            );

            log.debug("Sent TYPING_STOP event for conversation: {}", conversation.getId());
        } catch (Exception e) {
            log.warn("Failed to send typing stop event", e);
        }
    }

    /**
     * Get appropriate typing message based on user query context
     */
    private String getTypingMessageForQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return "AI is preparing to advise...";
        }

        String lowerQuery = userQuery.toLowerCase();

        // Adoption-related queries (Vietnamese + English)
        if (lowerQuery.contains("nhận nuôi") || lowerQuery.contains("tìm thú cưng") ||
            lowerQuery.contains("giới thiệu thú cưng") || lowerQuery.contains("tìm chó") ||
            lowerQuery.contains("tìm mèo") || lowerQuery.contains("thú cưng nào") ||
            lowerQuery.contains("adopt") || lowerQuery.contains("adoption") ||
            lowerQuery.contains("find pet") || lowerQuery.contains("looking for") ||
            lowerQuery.contains("pet available") || lowerQuery.contains("pet listings")) {
            return "AI is searching for pets that match your requirements...";
        }

        // Health-related queries (Vietnamese + English)
        if (lowerQuery.contains("bệnh") || lowerQuery.contains("ốm") ||
            lowerQuery.contains("triệu chứng") || lowerQuery.contains("thuốc") ||
            lowerQuery.contains("bác sĩ") || lowerQuery.contains("khám") ||
            lowerQuery.contains("sick") || lowerQuery.contains("ill") ||
            lowerQuery.contains("symptoms") || lowerQuery.contains("medicine") ||
            lowerQuery.contains("doctor") || lowerQuery.contains("veterinarian") ||
            lowerQuery.contains("checkup") || lowerQuery.contains("pain")) {
            return "AI is analyzing symptoms and preparing advice...";
        }

        // Care-related queries (Vietnamese + English)
        if (lowerQuery.contains("ăn") || lowerQuery.contains("uống") ||
            lowerQuery.contains("dinh dưỡng") || lowerQuery.contains("chăm sóc") ||
            lowerQuery.contains("eat") || lowerQuery.contains("drink") ||
            lowerQuery.contains("nutrition") || lowerQuery.contains("care") ||
            lowerQuery.contains("feeding") || lowerQuery.contains("grooming")) {
            return "AI is advising on nutrition and care routines...";
        }

        // Training/behavior queries (Vietnamese + English)
        if (lowerQuery.contains("huấn luyện") || lowerQuery.contains("hành vi") ||
            lowerQuery.contains("ngoan") || lowerQuery.contains("cắn") ||
            lowerQuery.contains("training") || lowerQuery.contains("behavior") ||
            lowerQuery.contains("good boy") || lowerQuery.contains("good girl") ||
            lowerQuery.contains("bite") || lowerQuery.contains("bark")) {
            return "AI is preparing advice on pet training...";
        }

        // General greeting or casual conversation (Vietnamese + English)
        if (lowerQuery.contains("chào") || lowerQuery.contains("hello") ||
            lowerQuery.contains("cảm ơn") || lowerQuery.contains("thanks") ||
            lowerQuery.contains("hi") || lowerQuery.contains("hey") ||
            lowerQuery.contains("xin chào") || lowerQuery.contains("good morning")) {
            return "AI is preparing greetings and advice...";
        }

        // Default typing message
        return "AI is advising about your pet...";
    }


}
