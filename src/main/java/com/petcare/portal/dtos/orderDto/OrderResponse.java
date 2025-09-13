package com.petcare.portal.dtos.orderDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private Long id;
    private String orderCode;
    private Long petOwnerId;
    private String petOwnerName;
    private LocalDateTime orderDate;
    private String status;
    private Double totalAmount;
    private com.petcare.portal.dtos.authDtos.AddressRequest shippingAddress;
    private String paymentMethod;
    private List<OrderItemResponse> orderItems;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private String productDescription;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
    }
}
