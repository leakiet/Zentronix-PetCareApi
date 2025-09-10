package com.petcare.portal.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
@Table(name = "conversations")
public class Conversation extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;          // null = guest

    private String title;
    private LocalDateTime startTime;

    @Lob
    private String summary; // Tóm tắt cuộc trò chuyện khi có quá nhiều tin nhắn

    @OneToMany(mappedBy = "conversation",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<ChatMessage> messages;
}