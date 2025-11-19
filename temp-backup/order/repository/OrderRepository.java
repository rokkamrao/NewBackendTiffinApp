package com.tiffin.order.repository;

import com.tiffin.order.model.Order;
import com.tiffin.order.model.OrderStatus;
import com.tiffin.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderTimeDesc(User user);
    List<Order> findByUserAndStatusOrderByOrderTimeDesc(User user, OrderStatus status);
    Page<Order> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    boolean existsByUser_IdAndItems_Dish_Id(Long userId, Long dishId);
    
    // Admin dashboard queries
    long countByStatus(OrderStatus status);
    long countByStatusIn(List<OrderStatus> statuses);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate")
    BigDecimal findTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt < :endDate AND o.status != 'CANCELLED'")
    BigDecimal findConfirmedRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Real-time metrics queries
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStatusAndDeliveryTimeIsNotNull(OrderStatus status);
}