package com.innowise.apigateway.manager;

import com.innowise.apigateway.dto.order.CreateOrderRequest;
import com.innowise.apigateway.dto.order.OrderDTO;
import com.innowise.apigateway.enums.OrderStatus;
import com.innowise.apigateway.service.OrderServiceClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderOperationManager {

  private final OrderServiceClient orderClient;

  public OrderOperationManager(OrderServiceClient orderClient) {
    this.orderClient = orderClient;
  }

  /**
   * Create order
   */
  public Mono<OrderDTO> createOrder(CreateOrderRequest request) {
    log.info("API Gateway: Starting create order for user with id : {}", request.userId());

    return orderClient.createOrderInOrderService(request)
        .flatMap(orderResponse -> {
          log.info("Order created successfully for user: {}", request.userId());
          return Mono.just(new OrderDTO(orderResponse.userId(), orderResponse.status(),
              orderResponse.creationDate(), orderResponse.orderItems()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Creation order failed", error.getMessage());
          return Mono.error(new RuntimeException("Creation of order failed", error));
        });
  }

  /**
   * Get order by id
   */
  public Mono<OrderDTO> getOrderById(Long id) {
    log.info("API Gateway: Starting find order in OrderService: {}", id);
    return orderClient.getOrderByIdInOrderService(id)
        .flatMap(orderDTO -> {
          log.info("API Gateway: get order by id {} successful", id);
          return Mono.just(
              new OrderDTO(orderDTO.userId(), orderDTO.status(), orderDTO.creationDate(),
                  orderDTO.orderItems()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Get order by id failed", error.getMessage());
          return Mono.error(new RuntimeException("Get order by id failed", error));
        });
  }

  /**
   * Get list orders by ids
   */
  public Mono<List<OrderDTO>> getOrdersByIds(List<Long> ids) {
    log.info("API Gateway: Starting find orders in OrderService: {}", ids);
    return orderClient.getOrdersByIdsInOrderService(ids)
        .doOnSuccess(orderDTOs ->
            log.info("API Gateway: get orders by ids {} successful. Found {} orders", ids,
                orderDTOs.size()))
        .map(orderDTOs -> orderDTOs.stream()
            .map(order -> new OrderDTO(order.userId(), order.status(), order.creationDate(),
                order.orderItems()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get orders by ids failed", error.getMessage());
          return Mono.error(new RuntimeException("Get orders by ids failed", error));
        });
  }

  /**
   * Get list orders by statuses
   */
  public Mono<List<OrderDTO>> getOrdersByStatuses(List<OrderStatus> statuses) {
    log.info("API Gateway: Starting find orders by statuses in OrderService: {}", statuses);
    return orderClient.getOrdersByStatusesInOrderService(statuses)
        .doOnSuccess(orderDTOs ->
            log.info("API Gateway: get orders by statuses {} successful. Found {} orders", statuses,
                orderDTOs.size()))
        .map(orderDTOs -> orderDTOs.stream()
            .map(order -> new OrderDTO(order.userId(), order.status(), order.creationDate(),
                order.orderItems()))
            .toList()
        )
        .onErrorResume(error -> {
          log.error("API Gateway: Get orders by statuses failed", error.getMessage());
          return Mono.error(new RuntimeException("Get orders by statuses failed", error));
        });
  }

  /**
   * Update order
   */
  public Mono<OrderDTO> updateOrder(Long id, OrderStatus status) {
    log.info("API Gateway: Starting update order with id {} in OrderService: {}", id);
    return orderClient.updateOrderInOrderService(id, status)
        .flatMap(orderDTO -> {
          log.info("API Gateway: update order by id {} successful", id);
          return Mono.just(
              new OrderDTO(orderDTO.userId(), orderDTO.status(), orderDTO.creationDate(),
                  orderDTO.orderItems()));
        })
        .onErrorResume(error -> {
          log.error("API Gateway: Update order failed", error.getMessage());
          return Mono.error(new RuntimeException("Update order failed", error));
        });
  }

  /**
   * Delete order by id
   */
  public Mono<Void> deleteOrder(Long id) {
    log.info("API Gateway: Starting delete order with id {}", id);

    return orderClient.deleteOrderInCardService(id)
        .doOnSuccess(v -> log.info("API Gateway: Order deleted successfully: {}", id))
        .doOnError(error -> log.error("API Gateway: Delete order failed: {}", error.getMessage()));
  }
}
