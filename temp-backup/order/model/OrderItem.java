package com.tiffin.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.tiffin.dish.model.Dish;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = {"order", "dish"})
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private BigDecimal totalPrice;
    
    // Add missing methods for Lombok compatibility
    public static OrderItemBuilder builder() {
        return new OrderItemBuilder();
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public static class OrderItemBuilder {
        private Long id;
        private Order order;
        private Dish dish;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        
        public OrderItemBuilder id(Long id) {
            this.id = id;
            return this;
        }
        
        public OrderItemBuilder order(Order order) {
            this.order = order;
            return this;
        }
        
        public OrderItemBuilder dish(Dish dish) {
            this.dish = dish;
            return this;
        }
        
        public OrderItemBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }
        
        public OrderItemBuilder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }
        
        public OrderItemBuilder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }
        
        public OrderItem build() {
            OrderItem item = new OrderItem();
            item.id = this.id;
            item.order = this.order;
            item.dish = this.dish;
            item.quantity = this.quantity;
            item.unitPrice = this.unitPrice;
            item.totalPrice = this.totalPrice;
            return item;
        }
    }
}