package com.innowise.order.repository;

import com.innowise.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link OrderItem} entity operations
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}