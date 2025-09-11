package com.petcare.portal.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petcare.portal.dtos.ChatRequest;
import com.petcare.portal.dtos.ChatResponse;
import com.petcare.portal.entities.ChatMessage;
import com.petcare.portal.entities.Conversation;
import com.petcare.portal.entities.User;
import com.petcare.portal.repositories.ChatMessageRepository;
import com.petcare.portal.repositories.ConversationRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.services.ChatService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

	private final ChatClient chatClient;
	private final ChatMessageRepository chatMessageRepo;
	private final ConversationRepository conversationRepo;
	private final UserRepository customerRepo;
	private final ModelMapper mapper;

	@Override
	public ChatResponse sendMessage(Long customerId, ChatRequest request) {
		if (request == null || request.getMessage() == null || request.getMessage().isBlank())
			throw new IllegalArgumentException("Message must not be empty");

		// ---- GUEST ----
		if (customerId == null) {
			String aiContent = callAi(request.getMessage());
			return new ChatResponse(null, "AI", aiContent, true);
		}

		// ---- LOGGED IN ----
		User customer = customerRepo.findById(customerId)
				.orElseThrow(() -> new EntityNotFoundException("Customer not found"));

		Conversation conv = resolveConversation(customer, request.getConversationId());

		ChatMessage userMsg = new ChatMessage(null, customer, conv, customer.getFirstName(), false,
				request.getMessage(), LocalDateTime.now());	
		chatMessageRepo.save(userMsg);

		String aiContent = callAi(request.getMessage());

		ChatMessage aiMsg = new ChatMessage(null, customer, conv, "AI", true, aiContent, LocalDateTime.now());
		chatMessageRepo.save(aiMsg);

		return new ChatResponse(conv.getId(), "AI", aiContent, true);
	}

	@Override
	public List<ChatResponse> getMessagesByConversation(Long conversationId) {
		Conversation conv = conversationRepo.findById(conversationId)
				.orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
		return chatMessageRepo.findByConversation(conv).stream().map(m -> mapper.map(m, ChatResponse.class))
				.collect(Collectors.toList());
	}

	@Override
	public List<Long> getConversationsByCustomer(Long customerId) {
		User c = customerRepo.findById(customerId)
				.orElseThrow(() -> new EntityNotFoundException("Customer not found"));
		return conversationRepo.findByCustomer(c).stream().map(Conversation::getId).toList();
	}

	// ----- helpers -----
	private String callAi(String prompt) {
		  return chatClient
			        .prompt()
			        .system("Bạn là một chuyên gia dinh dưỡng tại PetCare, "
			        		+ "chuyên tư vấn thực đơn lành mạnh, hỗ trợ giảm cân,"
			        		+ " tăng cân và giữ dáng."
			        		+ " Bạn có thể tính toán lượng calo ước lượng trong từng món ăn, "
			        		+ "đưa ra gợi ý thay thế lành mạnh, đồng thời luôn trả lời bằng tiếng Việt, "
			        		+ "ngắn gọn, dễ hiểu, thân thiện.")
			        .user(prompt)
			        .call()
			        .content();	}

	private Conversation resolveConversation(User customer, Long convId) {
		if (convId != null) {
			Conversation conv = conversationRepo.findById(convId)
					.orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
			if (conv.getCustomer() != null && !conv.getCustomer().getId().equals(customer.getId())) {
				throw new SecurityException("Conversation không thuộc về bạn");
			}
			return conv;
		}
		Conversation newConv = new Conversation();
		newConv.setCustomer(customer);
		newConv.setTitle("New Conversation");
		newConv.setStartTime(LocalDateTime.now());
		return conversationRepo.save(newConv);
	}
}
