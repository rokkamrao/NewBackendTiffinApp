package com.tiffin.order.model;

import com.tiffin.user.model.User;
import com.tiffin.user.model.Address;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    private double totalAmount;

    @ManyToOne
    @JoinColumn(name = "delivery_address_id")
    private Address deliveryAddress;

    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String paymentId;
    private String paymentMethod;
    private String specialInstructions;

    // Delivery Partner
    @ManyToOne
    @JoinColumn(name = "delivery_partner_id")
    private User deliveryPartner;

    public enum Status {
        PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }
}
