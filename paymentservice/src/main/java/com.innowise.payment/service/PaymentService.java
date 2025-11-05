package com.innowise.payment.service;

import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

  Payment createPayment(Payment payment);

  List<Payment> getPaymentsByOrderId(String orderId);

  List<Payment> getPaymentsByUserId(String userId);

  List<Payment> getPaymentsByStatus(PaymentStatus status);

  BigDecimal getTotalSumForPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
