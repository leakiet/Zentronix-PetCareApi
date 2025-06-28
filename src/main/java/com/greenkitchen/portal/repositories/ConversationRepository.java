package com.greenkitchen.portal.repositories;

import com.greenkitchen.portal.entities.Conversation;
import com.greenkitchen.portal.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByCustomer(Customer customer);
}
