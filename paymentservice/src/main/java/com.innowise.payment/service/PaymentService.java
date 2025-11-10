package com.innowise.payment.service;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing payment operations
 */
public interface PaymentService {

  /**
   * Creates and saves a new payment in the database
   *
   * @param payment
   * @return
   */
  Payment createPayment(Payment payment);

  /**
   * Retrieves all payments associated with the specified order ID
   *
   * @param orderId
   * @return
   */
  List<Payment> getPaymentsByOrderId(String orderId);

  /**
   * Retrieves all payments associated with the specified user ID
   *
   * @param userId
   * @return
   */
  List<Payment> getPaymentsByUserId(String userId);

  /**
   * Retrieves all payments with the specified status
   *
   * @param status
   * @return
   */
  List<Payment> getPaymentsByStatus(PaymentStatus status);

  /**
   * Calculates the total sum of all payments within the specified date period
   *
   * @param startDate
   * @param endDate
   * @return
   */
  BigDecimal getTotalSumForPeriod(LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Update order status by payment ID
   *
   * @param id     payment id
   * @param status payment status
   * @return Payment
   */
  Payment updatePayment(String id, PaymentStatus status);
}
