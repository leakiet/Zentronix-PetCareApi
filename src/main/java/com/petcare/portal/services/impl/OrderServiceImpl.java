package com.petcare.portal.services.impl;

import com.petcare.portal.entities.Order;
import com.petcare.portal.entities.OrderItem;
import com.petcare.portal.entities.Product;
import com.petcare.portal.entities.User;
import com.petcare.portal.entities.Address;
import com.petcare.portal.services.OrderService;
import com.petcare.portal.services.EmailService;
import com.petcare.portal.repositories.OrderRepository;
import com.petcare.portal.repositories.ProductRepository;
import com.petcare.portal.repositories.UserRepository;
import com.petcare.portal.dtos.orderDto.CreateOrderRequest;
import com.petcare.portal.dtos.orderDto.OrderResponse;
import com.petcare.portal.dtos.authDtos.AddressRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EmailService emailService;

    // Method to generate order code
    private String generateOrderCode() {
        // Get the count of existing orders to determine the next number
        long orderCount = orderRepository.count();
        int nextOrderNumber = (int) orderCount + 1;
        return String.format("FK-%04d", nextOrderNumber);
    }

    @Override
    public Order createOrder(CreateOrderRequest request) {
        try {
            // Find the pet owner
            User petOwner = userRepository.findById(request.getPetOwnerId())
                .orElseThrow(() -> new RuntimeException("Pet owner not found with id: " + request.getPetOwnerId()));

            // Create new order
            Order order = new Order();
            order.setPetOwner(petOwner);
            order.setOrderCode(generateOrderCode());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setShippingAddress(modelMapper.map(request.getShippingAddress(), Address.class));
            order.setPaymentMethod(request.getPaymentMethod());

            // Calculate total amount and create order items
            double totalAmount = 0.0;
            List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(itemRequest -> {
                    // Find the product
                    Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + itemRequest.getProductId()));

                    // Create order item
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setUnitPrice(itemRequest.getUnitPrice());
                    orderItem.setTotalPrice(itemRequest.getUnitPrice() * itemRequest.getQuantity());

                    return orderItem;
                })
                .collect(Collectors.toList());

            order.setOrderItems(orderItems);

            // Calculate total amount
            for (OrderItem item : orderItems) {
                totalAmount += item.getTotalPrice();
            }
            order.setTotalAmount(totalAmount);

            Order savedOrder = orderRepository.save(order);

            // Send order confirmation email
            try {
                emailService.sendOrderConfirmationEmail(petOwner.getEmail(), savedOrder);
            } catch (Exception emailException) {
                // Log the email error but don't fail the order creation
                System.err.println("Failed to send order confirmation email: " + emailException.getMessage());
            }

            return savedOrder;
        } catch (Exception e) {
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Order> getOrdersByPetOwner(Long petOwnerId) {
        try {
            User petOwner = userRepository.findById(petOwnerId)
                .orElseThrow(() -> new RuntimeException("Pet owner not found with id: " + petOwnerId));

            return orderRepository.findByPetOwner(petOwner);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching orders for pet owner: " + e.getMessage(), e);
        }
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    // Helper method to convert Order entity to OrderResponse DTO
    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
        response.setPetOwnerId(order.getPetOwner().getId());
        response.setPetOwnerName(order.getPetOwner().getFirstName() + " " + order.getPetOwner().getLastName());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod());

        // Convert shipping address
        if (order.getShippingAddress() != null) {
            AddressRequest addressRequest = new AddressRequest();
            addressRequest.setStreet(order.getShippingAddress().getStreet());
            addressRequest.setWard(order.getShippingAddress().getWard());
            addressRequest.setCity(order.getShippingAddress().getCity());
            addressRequest.setLatitude(order.getShippingAddress().getLatitude());
            addressRequest.setLongitude(order.getShippingAddress().getLongitude());
            response.setShippingAddress(addressRequest);
        }

        // Convert order items with product details
        List<OrderResponse.OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
            .map(orderItem -> {
                OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
                itemResponse.setId(orderItem.getId());
                itemResponse.setProductId(orderItem.getProduct().getId());
                itemResponse.setProductName(orderItem.getProduct().getName());
                itemResponse.setProductImage(orderItem.getProduct().getImage());
                itemResponse.setProductDescription(orderItem.getProduct().getDescription());
                itemResponse.setQuantity(orderItem.getQuantity());
                itemResponse.setUnitPrice(orderItem.getUnitPrice());
                itemResponse.setTotalPrice(orderItem.getTotalPrice());
                return itemResponse;
            })
            .collect(Collectors.toList());

        response.setOrderItems(orderItemResponses);
        return response;
    }

    // Method to get orders as DTOs
    public List<OrderResponse> getOrdersByPetOwnerAsDTO(Long petOwnerId) {
        List<Order> orders = getOrdersByPetOwner(petOwnerId);
        return orders.stream()
            .map(this::convertToOrderResponse)
            .collect(Collectors.toList());
    }
}
