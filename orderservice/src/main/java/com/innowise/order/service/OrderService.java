package com.innowise.order.service;

import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.enums.OrderStatus;
import java.util.List;

public interface OrderService {

  /**
   * Create Order
   *
   * @param request order to be created
   * @return order
   */
  OrderDTO createOrder(CreateOrderRequest request);

  /**
   * Find order by identifier
   *
   * @param id order identifier
   * @return order
   */
  OrderDTO getOrderById(Long id);

  /**
   * Find list of orders by ids
   *
   * @param ids list of identifiers
   * @return list of orders
   */
  List<OrderDTO> getOrdersByIds(List<Long> ids);

  /**
   * Find list of orders by statuses
   *
   * @param statuses list of orders statuses
   * @return list of orders
   */
  List<OrderDTO> getOrdersByStatuses(List<OrderStatus> statuses);

  /**
   * Update order
   *
   * @param id        order identifier
   * @param newStatus new order status
   * @return order
   */
  OrderDTO updateOrder(Long id, OrderStatus newStatus);

  /**
   * Delete order by identifier
   *
   * @param id order identifier
   */
  void deleteOrder(Long id);
}
