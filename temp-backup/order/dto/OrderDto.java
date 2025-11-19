package com.tiffin.order.dto;

import com.tiffin.order.model.OrderStatus;
import com.tiffin.order.model.PaymentMethod;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private Long deliveryAddressId;
    private PaymentMethod paymentMethod;
    private String paymentStatus;
    private String paymentId;
    private String specialInstructions;
    private Boolean isPaid;
}
// Helper DTOs moved to package-private declarations to avoid duplicate public types
@Data
class CreateOrderRequest {
    private List<OrderItemDto> items;
    private Long deliveryAddressId;
    private PaymentMethod paymentMethod;
}

@Data
class OrderStatusUpdate {
    private OrderStatus status;
    private String note;
}