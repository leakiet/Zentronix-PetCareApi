package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.ChatMessage;
import com.greenkitchen.portal.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversation(Conversation conversation);
}
