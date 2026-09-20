package com.ibm.capstone.ecom.service.impl;

import com.ibm.capstone.ecom.dto.request.CheckoutRequest;
import com.ibm.capstone.ecom.dto.response.AddressResponse;
import com.ibm.capstone.ecom.dto.response.OrderResponse;
import com.ibm.capstone.ecom.entity.*;
import com.ibm.capstone.ecom.entity.Order.OrderStatus;
import com.ibm.capstone.ecom.exception.BusinessException;
import com.ibm.capstone.ecom.exception.ResourceNotFoundException;
import com.ibm.capstone.ecom.repository.*;
import com.ibm.capstone.ecom.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int POINTS_PER_CURRENCY_UNIT = 10;
    private static final int POINTS_CANCELLATION_WINDOW_HRS = 48;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            AddressRepository addressRepository,
                            UserRepository userRepository,
                            PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout with an empty cart");
        }

        Address address = addressRepository.findById(req.getShippingAddressId())
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("Invalid shipping address"));

        // Calculate totals
        BigDecimal subtotal = cart.getItems().stream()
                .map(ci -> ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Gift points redemption
        int pointsToRedeem = req.getGiftPointsToRedeem() != null ? req.getGiftPointsToRedeem() : 0;
        if (pointsToRedeem > user.getGiftPoints()) {
            throw new BusinessException("Insufficient gift points");
        }
        BigDecimal discount = BigDecimal.valueOf((double) pointsToRedeem / POINTS_PER_CURRENCY_UNIT);
        BigDecimal total = subtotal.subtract(discount).max(BigDecimal.ZERO);

        // Build order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingAddress(address)
                .subtotal(subtotal)
                .discountAmount(discount)
                .giftPointsRedeemed(pointsToRedeem)
                .totalAmount(total)
                .notes(req.getNotes())
                .status(OrderStatus.CONFIRMED)
                .build();

        // Build order items and decrement stock
        List<OrderItem> items = cart.getItems().stream().map(ci -> {
            Product p = ci.getProduct();
            if (p.getStockQuantity() < ci.getQuantity()) {
                throw new BusinessException("Insufficient stock for: " + p.getTitle());
            }
            p.setStockQuantity(p.getStockQuantity() - ci.getQuantity());
            BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return OrderItem.builder()
                    .order(order)
                    .product(p)
                    .quantity(ci.getQuantity())
                    .unitPrice(p.getPrice())
                    .totalPrice(lineTotal)
                    .build();
        }).collect(Collectors.toList());
        order.setItems(items);

        // Payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(total)
                .method(req.getPaymentMethod())
                .status(Payment.PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.now())
                .build();
        order.setPayment(payment);

        // Deduct gift points, award new ones
        user.setGiftPoints(user.getGiftPoints() - pointsToRedeem + total.intValue());
        userRepository.save(user);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Override
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Order cannot be cancelled in status: " + order.getStatus());
        }
        long hoursSinceOrder = java.time.Duration.between(order.getCreatedAt(),
                LocalDateTime.now()).toHours();
        if (hoursSinceOrder > POINTS_CANCELLATION_WINDOW_HRS) {
            throw new BusinessException("Cancellation window of 48 hours has passed");
        }

        order.setStatus(OrderStatus.CANCELLED);
        // Restore stock
        order.getItems().forEach(item ->
                item.getProduct().setStockQuantity(
                        item.getProduct().getStockQuantity() + item.getQuantity()));
        // Restore gift points
        User user = order.getUser();
        user.setGiftPoints(user.getGiftPoints() + order.getGiftPointsRedeemed());
        userRepository.save(user);

        // Refund payment status
        if (order.getPayment() != null) {
            order.getPayment().setStatus(Payment.PaymentStatus.REFUNDED);
        }
        return toResponse(orderRepository.save(order));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "ORD-" + date + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> items = o.getItems().stream().map(i ->
                OrderResponse.OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productTitle(i.getProduct().getTitle())
                        .productAuthor(i.getProduct().getAuthor())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build()
        ).collect(Collectors.toList());

        AddressResponse addrResp = null;
        if (o.getShippingAddress() != null) {
            Address a = o.getShippingAddress();
            addrResp = AddressResponse.builder()
                    .id(a.getId())
                    .fullName(a.getFullName())
                    .addressLine1(a.getAddressLine1())
                    .addressLine2(a.getAddressLine2())
                    .city(a.getCity())
                    .state(a.getState())
                    .postalCode(a.getPostalCode())
                    .country(a.getCountry())
                    .phone(a.getPhone())
                    .isDefault(a.getIsDefault())
                    .build();
        }

        OrderResponse.PaymentSummary paymentSummary = null;
        if (o.getPayment() != null) {
            Payment p = o.getPayment();
            paymentSummary = OrderResponse.PaymentSummary.builder()
                    .id(p.getId())
                    .method(p.getMethod())
                    .status(p.getStatus())
                    .amount(p.getAmount())
                    .paidAt(p.getPaidAt())
                    .build();
        }

        return OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .giftPointsRedeemed(o.getGiftPointsRedeemed())
                .totalAmount(o.getTotalAmount())
                .notes(o.getNotes())
                .createdAt(o.getCreatedAt())
                .shippingAddress(addrResp)
                .items(items)
                .payment(paymentSummary)
                .build();
    }
}
