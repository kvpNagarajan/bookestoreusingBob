package com.ibm.capstone.ecom.service;

import com.ibm.capstone.ecom.dto.request.CartItemRequest;
import com.ibm.capstone.ecom.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addToCart(Long userId, CartItemRequest request);
    CartResponse updateCartItem(Long userId, Long productId, int quantity);
    CartResponse removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);
}
