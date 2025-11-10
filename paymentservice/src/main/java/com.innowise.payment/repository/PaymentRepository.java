package com.innowise.payment.repository;

import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.entity.Payment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PaymentRepository extends MongoRepository<Payment, String> {

  List<Payment> findByOrderId(String orderId);


  List<Payment> findByUserId(String userId);

  List<Payment> findByStatus(PaymentStatus status);

  @Query(value = "{'timestamp': {$gte: ?0, $lte: ?1}, 'status': 'COMPLETED'}",
      fields = "{'paymentAmount': 1}")
  List<Payment> findCompletedPaymentsInPeriod(LocalDateTime start, LocalDateTime end);

  boolean existsByOrderId(String orderId);
}
