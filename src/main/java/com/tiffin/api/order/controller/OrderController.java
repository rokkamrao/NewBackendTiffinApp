package com.tiffin.api.order.controller;

import com.tiffin.api.order.dto.OrderDto;
import com.tiffin.api.order.service.OrderService;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestBody OrderDto orderDto,
            @AuthenticationPrincipal(errorOnInvalidType = false) UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "anonymous";
        log.info("Creating order for user: {}", username);
        OrderDto createdOrder = orderService.createOrder(orderDto);
        return ResponseEntity.ok(createdOrder);
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getUserOrders(
            @AuthenticationPrincipal(errorOnInvalidType = false) UserDetails userDetails,
            Pageable pageable) {
        
        if (userDetails == null) {
            log.info("Fetching all orders (anonymous user)");
            // For anonymous users, return all orders
            // TODO: In production, this should return empty or require authentication
            Page<OrderDto> allOrders = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(allOrders);
        }
        
        log.info("Fetching orders for user: {}", userDetails.getUsername());
        
        // Get user by phone number
        User user = userRepository.findByPhone(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<OrderDto> orders = orderService.getUserOrders(user.getId(), pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        log.info("Fetching order: {}", id);
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("Updating order {} status to: {}", id, status);
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
