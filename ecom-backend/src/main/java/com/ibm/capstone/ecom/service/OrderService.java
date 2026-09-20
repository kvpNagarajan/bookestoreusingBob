package com.ibm.capstone.ecom.service;

import com.ibm.capstone.ecom.dto.request.CheckoutRequest;
import com.ibm.capstone.ecom.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse checkout(Long userId, CheckoutRequest request);
    Page<OrderResponse> getUserOrders(Long userId, Pageable pageable);
    OrderResponse getOrderById(Long userId, Long orderId);
    OrderResponse cancelOrder(Long userId, Long orderId);
}
