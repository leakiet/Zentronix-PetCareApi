package com.petcare.portal.repositories;

import com.petcare.portal.entities.ChatMessage;
import com.petcare.portal.entities.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversation(Conversation conversation);

    // For pagination - get messages ordered by timestamp desc
    Page<ChatMessage> findByConversationOrderByTimestampDesc(Conversation conversation, Pageable pageable);

    // Count messages in conversation
    long countByConversation(Conversation conversation);

    // Get recent messages for context (optimized query)
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation = :conversation " +
           "ORDER BY m.timestamp DESC LIMIT :limit")
    List<ChatMessage> findRecentMessagesByConversation(Conversation conversation, int limit);
}
