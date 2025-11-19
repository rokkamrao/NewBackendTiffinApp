package com.tiffin.payment.repository;

import com.tiffin.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Payment> findFirstByTransactionId(String transactionId);
}
