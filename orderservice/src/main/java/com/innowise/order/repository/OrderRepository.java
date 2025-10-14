package com.innowise.order.repository;

import com.innowise.order.entity.Order;
import com.innowise.order.enums.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Order} entity operations
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * Get Orders by ids
   */
  List<Order> findByIdIn(List<Long> ids);

  /**
   * Get Orders by statuses
   */
  List<Order> findByStatusIn(List<OrderStatus> statuses);
}
