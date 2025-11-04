package com.tiffin.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.tiffin.user.model.User;
import com.tiffin.user.model.Address;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"items", "user", "deliveryAddress"})
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime orderTime;
    
    private LocalDateTime deliveryTime;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    private String paymentId;
    
    @Column(nullable = false)
    @lombok.Builder.Default
    private String paymentStatus = "PENDING";
    
    private String specialInstructions;
    
    // Manually add missing methods for Lombok compatibility
    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public Set<OrderItem> getItems() {
        return items;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public LocalDateTime getOrderTime() {
        return orderTime;
    }
    
    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }
    
    public Address getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public Long getId() {
        return id;
    }
}
