package com.petcare.portal.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessage extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;           // null = guest

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;   // null = guest

    private String  senderName;
    private Boolean isFromAI;

    @Lob private String content;
    private LocalDateTime timestamp;

    public ChatMessage(Long id, Customer customer, Conversation conversation,
                       String senderName, Boolean isFromAI,
                       String content, LocalDateTime timestamp) {
        this.id = id;
        this.customer = customer;
        this.conversation = conversation;
        this.senderName = senderName;
        this.isFromAI = isFromAI;
        this.content = content;
        this.timestamp = timestamp;
    }
}