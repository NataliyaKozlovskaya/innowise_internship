package com.innowise.payment.controller;

import com.innowise.payment.entity.Payment;
import com.innowise.payment.enums.PaymentStatus;
import com.innowise.payment.service.PaymentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

  private final PaymentServiceImpl paymentService;

  public PaymentController(PaymentServiceImpl paymentService) {
    this.paymentService = paymentService;
  }

  // CREATE
  @PostMapping
  public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
    Payment created = paymentService.createPayment(payment);
    return ResponseEntity.ok(created);
  }

  @GetMapping("/order/{orderId}")
  public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable String orderId) {
    return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable String userId) {
    return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
    return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
  }

  @GetMapping("/total")
  public ResponseEntity<BigDecimal> getTotalPaymentsForPeriod(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

    BigDecimal total = paymentService.getTotalPaymentsForPeriod(start, end);
    return ResponseEntity.ok(total);
  }
}
