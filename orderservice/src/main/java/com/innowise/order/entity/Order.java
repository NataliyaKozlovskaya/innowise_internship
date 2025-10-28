package com.innowise.order.entity;

import com.innowise.order.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Entity class representing a customer order
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status;

  @CreationTimestamp
  @Column(name = "creation_date", nullable = false, updatable = false)
  private LocalDateTime creationDate;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  public Order(String userId, OrderStatus status) {
    this.userId = userId;
    this.status = status;
  }

  // Helper methods for managing order items
  public void addOrderItem(Item item, Integer quantity) {
    OrderItem orderItem = new OrderItem(this, item, quantity);
    orderItems.add(orderItem);
  }
}
