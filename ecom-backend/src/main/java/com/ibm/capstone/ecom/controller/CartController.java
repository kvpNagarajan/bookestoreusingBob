package com.ibm.capstone.ecom.controller;

import com.ibm.capstone.ecom.dto.request.CartItemRequest;
import com.ibm.capstone.ecom.dto.response.ApiResponse;
import com.ibm.capstone.ecom.dto.response.CartResponse;
import com.ibm.capstone.ecom.exception.ResourceNotFoundException;
import com.ibm.capstone.ecom.repository.UserRepository;
import com.ibm.capstone.ecom.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService,
                          UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CartItemRequest request) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart",
                cartService.addToCart(userId, request)));
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok("Cart updated",
                cartService.updateCartItem(userId, productId, quantity)));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long productId) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok("Item removed",
                cartService.removeFromCart(userId, productId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UserDetails principal) {
        cartService.clearCart(resolveUserId(principal));
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared", null));
    }

    private Long resolveUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }
}
