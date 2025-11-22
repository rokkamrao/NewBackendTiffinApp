package com.tiffin.order.repository;

import com.tiffin.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    List<Order> findByDeliveryPartnerId(Long deliveryPartnerId);

    List<Order> findByStatus(Order.Status status);
}
