package com.petcare.portal.dtos.orderDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    private Long petOwnerId;

    private List<OrderItemRequest> orderItems;

    private com.petcare.portal.dtos.authDtos.AddressRequest shippingAddress;

    private String paymentMethod;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private Double unitPrice;
    }
}
