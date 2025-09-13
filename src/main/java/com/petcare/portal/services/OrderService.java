package com.petcare.portal.services;

import com.petcare.portal.entities.Order;
import com.petcare.portal.dtos.orderDto.CreateOrderRequest;

import java.util.List;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);
    List<Order> getOrdersByPetOwner(Long petOwnerId);
    Order getOrderById(Long orderId);
}
