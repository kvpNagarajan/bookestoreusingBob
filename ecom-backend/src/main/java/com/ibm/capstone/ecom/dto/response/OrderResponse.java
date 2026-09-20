package com.ibm.capstone.ecom.dto.response;

import com.ibm.capstone.ecom.entity.Order;
import com.ibm.capstone.ecom.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Order.OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private Integer giftPointsRedeemed;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
    private AddressResponse shippingAddress;
    private List<OrderItemResponse> items;
    private PaymentSummary payment;

    @Data @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productTitle;
        private String productAuthor;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    @Data @Builder
    public static class PaymentSummary {
        private Long id;
        private Payment.PaymentMethod method;
        private Payment.PaymentStatus status;
        private BigDecimal amount;
        private LocalDateTime paidAt;
    }
}
