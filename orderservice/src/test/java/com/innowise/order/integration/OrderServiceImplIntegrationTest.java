package com.innowise.order.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.dto.OrderItemRequest;
import com.innowise.order.entity.Item;
import com.innowise.order.entity.Order;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.OrderNotFoundException;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.rest.OrderClientService;
import com.innowise.order.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class OrderServiceImplIntegrationTest {

  private static final String USER_ID_1 = "123";
  private static final String USER_ID_2 = "321";
  private static final String ORDER_NOT_FOUND = "Order not found";
  private static final Long NON_EXISTENT_ID = 999L;


  @Autowired
  private OrderServiceImpl orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ItemRepository itemRepository;

  @MockBean
  private OrderClientService userClientService;

  @Container
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:15-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
    itemRepository.deleteAll();

    doNothing().when(userClientService).getUserById(anyString());
  }

  @Test
  @DisplayName("Should create order successfully when valid request")
  void createOrder_ShouldCreateOrderSuccessfully_WhenValidRequest() {
    Item item = createTestItem("Laptop", new BigDecimal("999.99"));

    CreateOrderRequest request = new CreateOrderRequest(
        USER_ID_1,
        List.of(new OrderItemRequest(item.getId(), 1))
    );

    OrderDTO result = orderService.createOrder(request);

    assertEquals(USER_ID_1, result.userId());
    assertEquals(OrderStatus.PENDING, result.status());
    assertEquals(1, result.orderItems().size());
    assertEquals(1, orderRepository.count());
  }

  @Test
  @DisplayName("Should throw ItemNotFoundException when items do not exist")
  void createOrder_ShouldThrowItemNotFoundException_WhenItemsDoNotExist() {
    Long nonExistentItemId = 999L;

    CreateOrderRequest request =
        new CreateOrderRequest(
            USER_ID_1,
            List.of(new OrderItemRequest(nonExistentItemId, 1))
        );

    ItemNotFoundException exception = assertThrows(
        ItemNotFoundException.class,
        () -> orderService.createOrder(request)
    );

    assertTrue(exception.getMessage().contains("Items not found"));
    assertTrue(exception.getMessage().contains("999"));
  }

  @Test
  @DisplayName("Should return order when order exists")
  void getOrderById_ShouldReturnOrder_WhenOrderExists() {
    Item item = createTestItem("Tablet", new BigDecimal("399.99"));
    OrderDTO createdOrder = createTestOrder(USER_ID_1, List.of(item), List.of(1));

    Long orderId = getOrderIdFromDatabase(createdOrder);

    OrderDTO result = orderService.getOrderById(orderId);

    assertNotNull(result);
    assertEquals(USER_ID_1, result.userId());
    assertEquals(OrderStatus.PENDING, result.status());
    assertEquals(1, result.orderItems().size());
    assertEquals(item.getId(), result.orderItems().get(0).itemId());
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist")
  void getOrderById_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
    Long nonExistentOrderId = 999L;

    OrderNotFoundException exception = assertThrows(
        OrderNotFoundException.class,
        () -> orderService.getOrderById(nonExistentOrderId)
    );

    assertEquals(ORDER_NOT_FOUND + " with id: " + nonExistentOrderId, exception.getMessage());
  }

  @Test
  @DisplayName("Should return orders when orders exist")
  void getOrdersByIds_ShouldReturnOrders_WhenOrdersExist() {
    Item item1 = createTestItem("Item1", new BigDecimal("10.00"));
    Item item2 = createTestItem("Item2", new BigDecimal("20.00"));

    createTestOrder(USER_ID_1, List.of(item1), List.of(1));
    createTestOrder(USER_ID_2, List.of(item2), List.of(2));

    List<Long> orderIds = orderRepository.findAll().stream()
        .map(Order::getId)
        .collect(Collectors.toList());

    List<OrderDTO> result = orderService.getOrdersByIds(orderIds);

    assertEquals(2, result.size());

    assertTrue(result.stream().anyMatch(order -> USER_ID_1.equals(order.userId())));
    assertTrue(result.stream().anyMatch(order -> USER_ID_2.equals(order.userId())));
  }

  @Test
  @DisplayName("Should return empty list when no orders found")
  void getOrdersByIds_ShouldReturnEmptyList_WhenNoOrdersFound() {
    List<OrderDTO> result = orderService.getOrdersByIds(List.of(999L, 1000L));

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should filter orders by status")
  void getOrdersByStatuses_ShouldFilterOrdersByStatus() {
    Item item = createTestItem("Product", new BigDecimal("50.00"));

    createTestOrder(USER_ID_1, List.of(item), List.of(1));
    createTestOrder(USER_ID_2, List.of(item), List.of(2));

    List<Order> allOrders = orderRepository.findAll();
    assertEquals(2, allOrders.size());

    Order secondOrder = allOrders.get(1);
    orderService.updateOrder(secondOrder.getId(), OrderStatus.COMPLETED);

    List<OrderDTO> completedOrders = orderService.getOrdersByStatuses(
        List.of(OrderStatus.COMPLETED));
    List<OrderDTO> pendingOrders = orderService.getOrdersByStatuses(List.of(OrderStatus.PENDING));

    assertEquals(1, completedOrders.size());
    assertEquals(OrderStatus.COMPLETED, completedOrders.get(0).status());

    assertEquals(1, pendingOrders.size());
    assertEquals(OrderStatus.PENDING, pendingOrders.get(0).status());
  }

  @Test
  @DisplayName("Should update order status when order exists")
  void updateOrder_ShouldUpdateOrderStatus_WhenOrderExists() {
    Item item = createTestItem("Book", new BigDecimal("15.99"));
    createTestOrder(USER_ID_1, List.of(item), List.of(1));

    List<Order> allOrders = orderRepository.findAll();
    assertEquals(1, allOrders.size());

    Long orderId = allOrders.get(0).getId();
    OrderStatus initialStatus = allOrders.get(0).getStatus();
    assertEquals(OrderStatus.PENDING, initialStatus);

    OrderDTO updatedOrder = orderService.updateOrder(orderId, OrderStatus.PROCESSING);

    assertEquals(OrderStatus.PROCESSING, updatedOrder.status());
    assertEquals(USER_ID_1, updatedOrder.userId());

    OrderDTO persistedOrder = orderService.getOrderById(orderId);
    assertEquals(OrderStatus.PROCESSING, persistedOrder.status());
    assertEquals(USER_ID_1, persistedOrder.userId());
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist for update")
  void updateOrder_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
    OrderNotFoundException exception = assertThrows(
        OrderNotFoundException.class,
        () -> orderService.updateOrder(NON_EXISTENT_ID, OrderStatus.COMPLETED)
    );
    assertEquals(ORDER_NOT_FOUND + " with id: " + NON_EXISTENT_ID, exception.getMessage());
  }

  @Test
  @DisplayName("Should delete order when order exists")
  void deleteOrder_ShouldDeleteOrder_WhenOrderExists() {
    Item item = createTestItem("Phone", new BigDecimal("499.99"));
    createTestOrder(USER_ID_1, List.of(item), List.of(1));

    List<Order> allOrders = orderRepository.findAll();
    assertEquals(1, allOrders.size());

    Long orderId = allOrders.get(0).getId();
    assertTrue(orderRepository.existsById(orderId));

    orderService.deleteOrder(orderId);
    assertFalse(orderRepository.existsById(orderId));

    OrderNotFoundException exception = assertThrows(
        OrderNotFoundException.class,
        () -> orderService.getOrderById(orderId)
    );
    assertEquals(ORDER_NOT_FOUND + " with id: " + orderId, exception.getMessage());
  }

  @Test
  @DisplayName("Should throw OrderNotFoundException when order does not exist for deletion")
  void deleteOrder_ShouldThrowOrderNotFoundException_WhenOrderDoesNotExist() {
    OrderNotFoundException exception = assertThrows(
        OrderNotFoundException.class,
        () -> orderService.deleteOrder(NON_EXISTENT_ID)
    );
    assertEquals(ORDER_NOT_FOUND + " with id: " + NON_EXISTENT_ID, exception.getMessage());
  }

  private Long getOrderIdFromDatabase(OrderDTO orderDTO) {
    return orderRepository.findAll().stream()
        .filter(order -> order.getUserId().equals(orderDTO.userId()))
        .filter(order -> order.getStatus() == orderDTO.status())
        .map(Order::getId)
        .findFirst()
        .orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND));
  }

  private Item createTestItem(String name, BigDecimal price) {
    Item item = new Item();
    item.setName(name);
    item.setPrice(price);
    return itemRepository.save(item);
  }

  private OrderDTO createTestOrder(String userId, List<Item> items, List<Integer> quantities) {
    List<OrderItemRequest> orderItems = items.stream()
        .map(item -> new OrderItemRequest(item.getId(), quantities.get(items.indexOf(item))))
        .toList();

    CreateOrderRequest request = new CreateOrderRequest(userId, orderItems);
    return orderService.createOrder(request);
  }
}