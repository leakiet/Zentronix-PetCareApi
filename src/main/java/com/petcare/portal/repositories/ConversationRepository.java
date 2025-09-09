package com.petcare.portal.repositories;

import com.petcare.portal.entities.Conversation;
import com.petcare.portal.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByCustomer(Customer customer);
}
