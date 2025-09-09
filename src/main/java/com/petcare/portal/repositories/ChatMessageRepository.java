package com.petcare.portal.repositories;

import com.petcare.portal.entities.ChatMessage;
import com.petcare.portal.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversation(Conversation conversation);
}
