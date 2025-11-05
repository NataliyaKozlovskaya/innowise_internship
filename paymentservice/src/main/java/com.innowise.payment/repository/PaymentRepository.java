package com.innowise.payment.repository;

import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.entity.Payment;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PaymentRepository extends MongoRepository<Payment, String> {

  // Найти платежи по order_id
  List<Payment> findByOrderId(String orderId);

  // Найти платежи по user_id
  List<Payment> findByUserId(String userId);

  // Найти платежи по статусу
  List<Payment> findByStatus(PaymentStatus status);

  // Найти платежи по нескольким статусам
  List<Payment> findByStatusIn(List<PaymentStatus> statuses);

  // Получить общую сумму платежей за период
  @Query(value = "{'timestamp': {$gte: ?0, $lte: ?1}, 'status': 'COMPLETED'}",
      fields = "{'paymentAmount': 1}")
  List<Payment> findCompletedPaymentsInPeriod(LocalDateTime start, LocalDateTime end);

  // Проверить существование платежа по order_id
  boolean existsByOrderId(String orderId);
}
