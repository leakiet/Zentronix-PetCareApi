package com.petcare.portal.repositories;

import com.petcare.portal.entities.Order;
import com.petcare.portal.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByPetOwner(User petOwner);
    List<Order> findByPetOwnerAndStatus(User petOwner, String status);
}
