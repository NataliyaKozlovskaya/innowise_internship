package com.innowise.payment.repository;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Repository interface for managing Payment entities in MongoDB
 */
public interface PaymentRepository extends MongoRepository<Payment, String> {

  /**
   * Finds all payments by order id
   */
  List<Payment> findByOrderId(Long orderId);

  /**
   * Finds all payments by user id
   */
  List<Payment> findByUserId(String userId);

  /**
   * Finds all payments made by statuses
   */
  List<Payment> findByStatusIn(List<PaymentStatus> statuses);

  /**
   * Finds completed payments within a specified time period and returns only their payment amounts
   */
  @Query(value = "{'timestamp': {$gte: ?0, $lte: ?1}, 'status': 'COMPLETED'}",
      fields = "{'paymentAmount': 1}")
  List<Payment> findCompletedPaymentsInPeriod(LocalDateTime start, LocalDateTime end);
}