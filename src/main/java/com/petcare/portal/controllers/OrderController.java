package com.petcare.portal.controllers;

import com.petcare.portal.entities.Order;
import com.petcare.portal.services.OrderService;
import com.petcare.portal.services.EmailService;
import com.petcare.portal.dtos.orderDto.CreateOrderRequest;
import com.petcare.portal.dtos.orderDto.OrderResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;

@RestController
@RequestMapping("/apis/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            OrderResponse response = convertToResponse(order);

            // Send order confirmation email
            try {
                emailService.sendOrderConfirmationEmail(order.getPetOwner().getEmail(), order);
            } catch (Exception emailException) {
                // Log the email error but don't fail the order creation
                System.err.println("Failed to send order confirmation email: " + emailException.getMessage());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pet-owner/{petOwnerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByPetOwner(@PathVariable("petOwnerId") Long petOwnerId) {
        try {
            List<Order> orders = orderService.getOrdersByPetOwner(petOwnerId);
            List<OrderResponse> responses = orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("orderId") Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setPetOwnerId(order.getPetOwner().getId());
        response.setPetOwnerName(order.getPetOwner().getFirstName() + " " + order.getPetOwner().getLastName());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(modelMapper.map(order.getShippingAddress(), com.petcare.portal.dtos.authDtos.AddressRequest.class));
        response.setPaymentMethod(order.getPaymentMethod());

        // Convert order items with product details
        List<OrderResponse.OrderItemResponse> itemResponses = order.getOrderItems().stream()
            .map(item -> {
                OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                itemResponse.setId(item.getId());
                itemResponse.setProductId(item.getProduct().getId());
                itemResponse.setProductName(item.getProduct().getName());
                itemResponse.setProductImage(item.getProduct().getImage());
                itemResponse.setProductDescription(item.getProduct().getDescription());
                itemResponse.setQuantity(item.getQuantity());
                itemResponse.setUnitPrice(item.getUnitPrice());
                itemResponse.setTotalPrice(item.getTotalPrice());
                return itemResponse;
            })
            .collect(Collectors.toList());

        response.setOrderItems(itemResponses);
        return response;
    }
}
