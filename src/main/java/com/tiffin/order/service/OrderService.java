package com.tiffin.order.service;

import com.tiffin.order.dto.OrderDto;
import com.tiffin.order.dto.OrderItemDto;
import com.tiffin.order.model.Order;
import com.tiffin.order.model.OrderItem;
import com.tiffin.order.model.OrderStatus;
import com.tiffin.order.repository.OrderRepository;
import com.tiffin.user.model.Address;
import com.tiffin.user.repository.AddressRepository;
import com.tiffin.dish.repository.DishRepository;
import com.tiffin.dish.model.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
        private final OrderRepository orderRepository;
                private final DishRepository dishRepository;
        private final AddressRepository addressRepository;

    @Transactional
        public OrderDto createOrder(OrderDto orderDto) {
                Address deliveryAddress = null;
                if (orderDto.getDeliveryAddressId() != null) {
                        deliveryAddress = addressRepository.findById(orderDto.getDeliveryAddressId())
                                        .orElseThrow(() -> new RuntimeException("Delivery address not found"));
                }

                Order order = Order.builder()
                                .deliveryAddress(deliveryAddress)
                                .deliveryTime(orderDto.getDeliveryTime())
                                .status(OrderStatus.PENDING)
                                .paymentMethod(orderDto.getPaymentMethod())
                                .paymentStatus("PENDING")
                                .orderTime(LocalDateTime.now())
                                .createdAt(LocalDateTime.now())
                                .build();

        Set<OrderItem> items = orderDto.getItems().stream()
                .map(itemDto -> {
                    Dish dish = dishRepository.findById(itemDto.getDishId())
                            .orElseThrow(() -> new RuntimeException("Dish not found"));
                    return OrderItem.builder()
                            .order(order)
                            .dish(dish)
                            .quantity(itemDto.getQuantity())
                            .unitPrice(dish.getPrice())
                            .totalPrice(dish.getPrice().multiply(java.math.BigDecimal.valueOf(itemDto.getQuantity())))
                            .build();
                })
                .collect(Collectors.toSet());

        order.setItems(items);
        order.setTotalAmount(calculateTotalAmount(items));

        Order savedOrder = orderRepository.save(order);
        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
                public Page<OrderDto> getUserOrders(Long userId, Pageable pageable) {
                        return orderRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
        public OrderDto updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
                order.setStatus(OrderStatus.valueOf(status));
        Order updatedOrder = orderRepository.save(order);
        return mapToDto(updatedOrder);
    }

        @Transactional
        public void updateOrderPaymentStatus(String orderId, String paymentStatus) {
                Order order = orderRepository.findById(Long.parseLong(orderId))
                                .orElseThrow(() -> new RuntimeException("Order not found"));
                order.setPaymentStatus(paymentStatus);
                if ("PAID".equalsIgnoreCase(paymentStatus)) {
                        order.setStatus(OrderStatus.CONFIRMED);
                }
                orderRepository.save(order);
        }

    private OrderDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .dishId(item.getDish().getId())
                        .dishName(item.getDish().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .items(itemDtos)
                .totalAmount(order.getTotalAmount())
                .deliveryAddressId(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getId() : null)
                .deliveryTime(order.getDeliveryTime())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentId(order.getPaymentId())
                .specialInstructions(order.getSpecialInstructions())
                .isPaid("PAID".equalsIgnoreCase(order.getPaymentStatus()))
                .orderTime(order.getOrderTime())
                .build();
    }

    private java.math.BigDecimal calculateTotalAmount(Set<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}