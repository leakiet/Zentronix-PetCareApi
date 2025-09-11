package com.petcare.portal.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessage extends AbstractEntity {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4589087290296753730L;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;           // null = guest

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;   // null = guest

    private String  senderName;
    private Boolean isFromAI;

    @Lob private String content;
    private LocalDateTime timestamp;

    public ChatMessage(Long id, User user, Conversation conversation,
                       String senderName, Boolean isFromAI,
                       String content, LocalDateTime timestamp) {
        this.id = id;
        this.user = user;
        this.conversation = conversation;
        this.senderName = senderName;
        this.isFromAI = isFromAI;
        this.content = content;
        this.timestamp = timestamp;
    }
}