package com.innowise.order.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.dto.OrderItemRequest;
import com.innowise.order.dto.kafka.PaymentProcessedEvent;
import com.innowise.order.entity.Item;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.UserServiceUnavailableException;
import com.innowise.order.kafka.OrderEventService;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.OrderService;
import com.innowise.order.unit.TestKafkaConsumer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrderServiceImplWireMockIntegrationTest extends BaseTestcontainersTest {

  public static final BigDecimal AMOUNT = new BigDecimal(100.50);
  private static final String USER_ID = "123";
  private static final String URL = "/api/v1/users/.*";
  private static final String UNAVAILABLE = "User service unavailable";
  private final Long ORDER_ID = 1L;
  private final String STATUS = "COMPLETED";

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderService orderService;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OrderEventService orderEventService;

  @Autowired
  private ObjectMapper objectMapper;

  private static WireMockServer wireMockServer;

  @Container
  static KafkaContainer kafkaContainer = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("service.user.url", () -> "http://localhost:" + wireMockServer.port());
    registry.add("service.user.methodGetUserById", () -> "/api/v1/users/");
  }

  @BeforeAll
  static void beforeAll() {
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    wireMockServer.start();
  }

  @AfterAll
  static void afterAll() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
    itemRepository.deleteAll();
    wireMockServer.resetAll();
  }

  @Test
  @DisplayName("Order creation should succeed when user exists and items are available")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void createOrder_ShouldSuccess_WhenUserExistsAndItemsAvailable() throws Exception {
    Item laptop = createTestItem("Laptop", new BigDecimal("999.99"));
    PaymentProcessedEvent paymentProcessedEvent = getPaymentProcessedEvent(ORDER_ID, USER_ID,
        STATUS);
    stubUserServiceCall(USER_ID, HttpStatus.OK, """
        {
            "name": "Anna",
            "surname": "Ivanova",
            "birthDate": "1985-08-20",
            "email": "anna.ivanova@example.com"
        }
        """);

    CreateOrderRequest request = new CreateOrderRequest(
        USER_ID,
        List.of(new OrderItemRequest(laptop.getId(), 1))
    );

    OrderDTO result = orderService.createOrder(request);
    OrderDTO order = orderService.getOrderById(ORDER_ID);

    assertEquals(result, order);
    assertNotNull(order);
    assertEquals(OrderStatus.PENDING, result.status());

    orderEventService.sendOrderCreatedEvent(2L, "1234", AMOUNT, LocalDateTime.now());

    try (TestKafkaConsumer testConsumer = new TestKafkaConsumer(
        kafkaContainer.getBootstrapServers(), "order-created")) {

      orderEventService.processPaymentEvent(ORDER_ID, STATUS);

      ConsumerRecord<String, String> record = testConsumer.pollRecord(10, TimeUnit.SECONDS);
      assertThat(record).isNotNull();

      PaymentProcessedEvent kafka = objectMapper.readValue(
          record.value(), PaymentProcessedEvent.class);

      assertNotNull(result);
      assertEquals(paymentProcessedEvent.getOrderId(), kafka.getOrderId());
      assertEquals(1, result.orderItems().size());
      wireMockServer.verify(1, getRequestedFor(urlPathMatching(URL)));
    }
  }

  @Test
  @DisplayName("Should throw UserServiceUnavailableException when user is not found")
  void createOrder_ShouldThrowUserServiceUnavailableException_WhenUserNotFound() {
    String userId = "non-existent-user";
    Item item = createTestItem("Mouse", new BigDecimal("29.99"));

    stubUserServiceCall(userId, HttpStatus.NOT_FOUND, "");

    CreateOrderRequest request = new CreateOrderRequest(
        userId,
        List.of(new OrderItemRequest(item.getId(), 1))
    );

    UserServiceUnavailableException exception = assertThrows(
        UserServiceUnavailableException.class,
        () -> orderService.createOrder(request)
    );

    assertTrue(exception.getMessage().contains(UNAVAILABLE));
    wireMockServer.verify(1, getRequestedFor(urlPathMatching(URL)));
  }

  @Test
  @DisplayName("Should throw UserServiceUnavailableException when user service returns 5xx error")
  void createOrder_ShouldThrowUserServiceUnavailableException_WhenUserServiceReturns5xx() {
    Item item = createTestItem("Keyboard", new BigDecimal("79.99"));

    stubUserServiceCall(USER_ID, HttpStatus.INTERNAL_SERVER_ERROR, "");

    CreateOrderRequest request = new CreateOrderRequest(
        USER_ID,
        List.of(new OrderItemRequest(item.getId(), 1))
    );

    UserServiceUnavailableException exception = assertThrows(
        UserServiceUnavailableException.class,
        () -> orderService.createOrder(request)
    );

    assertEquals(UNAVAILABLE, exception.getMessage());
    wireMockServer.verify(1, getRequestedFor(urlPathMatching(URL)));
  }

  @Test
  @DisplayName("Should throw UserServiceUnavailableException when user service times out")
  void createOrder_ShouldThrowUserServiceUnavailableException_WhenUserServiceTimeout() {
    Item item = createTestItem("Monitor", new BigDecimal("299.99"));

    wireMockServer.stubFor(get(urlPathMatching(URL))
        .willReturn(aResponse()
            .withFixedDelay(1000)
            .withStatus(500)));

    CreateOrderRequest request = new CreateOrderRequest(
        USER_ID,
        List.of(new OrderItemRequest(item.getId(), 1))
    );

    UserServiceUnavailableException exception = assertThrows(
        UserServiceUnavailableException.class,
        () -> orderService.createOrder(request)
    );

    assertEquals(UNAVAILABLE, exception.getMessage());
    wireMockServer.verify(1, getRequestedFor(urlPathMatching(URL)));
  }

  @Test
  @DisplayName("Should throw ItemNotFoundException when items do not exist")
  void createOrder_ShouldThrowItemNotFoundException_WhenItemsDoNotExist() {
    Long nonExistentItemId = 999L;

    stubUserServiceCall(USER_ID, HttpStatus.OK, """
        {
            "name": "Anna",
            "surname": "Ivanova",
            "birthDate": "1985-08-20",
            "email": "anna.ivanova@example.com"
        }
        """);

    CreateOrderRequest request = new CreateOrderRequest(
        USER_ID,
        List.of(new OrderItemRequest(nonExistentItemId, 1))
    );

    ItemNotFoundException exception = assertThrows(
        ItemNotFoundException.class, () -> orderService.createOrder(request));

    assertTrue(exception.getMessage().contains("Items not found: " + List.of(nonExistentItemId)));
    wireMockServer.verify(1, getRequestedFor(urlPathMatching(URL)));
  }

  @Test
  @DisplayName("Order creation should succeed for multiple users and items")
  void createOrder_ShouldSuccess_WhenMultipleUsersAndItems() {
    String user1 = "123";
    String user2 = "321";
    Item item1 = createTestItem("Item1", new BigDecimal("10.00"));
    Item item2 = createTestItem("Item2", new BigDecimal("20.00"));

    stubUserServiceCall(USER_ID, HttpStatus.OK, """
        {
            "name": "Anna",
            "surname": "Ivanova",
            "birthDate": "1985-08-20",
            "email": "anna.ivanova@example.com"
        }
        """);

    stubUserServiceCall(USER_ID, HttpStatus.OK, """
        {
            "name": "Boris",
            "surname": "Makarov",
            "birthDate": "1992-03-10",
            "email": "boris.makarov@example.com"
        }
        """);

    CreateOrderRequest request1 = new CreateOrderRequest(
        user1,
        List.of(new OrderItemRequest(item1.getId(), 1))
    );

    CreateOrderRequest request2 = new CreateOrderRequest(
        user2,
        List.of(new OrderItemRequest(item2.getId(), 2))
    );

    OrderDTO result1 = orderService.createOrder(request1);
    OrderDTO result2 = orderService.createOrder(request2);

    assertEquals(user1, result1.userId());
    assertEquals(user2, result2.userId());
    assertEquals(2, orderRepository.count());
    wireMockServer.verify(2, getRequestedFor(urlPathMatching(URL)));
  }

  @Test
  @DisplayName("Should handle different HTTP status codes correctly")
  void createOrder_ShouldHandleDifferentHttpStatusCodes() {
    Item item = createTestItem("Product", new BigDecimal("50.00"));

    stubUserServiceCall(USER_ID, HttpStatus.BAD_REQUEST, "");

    CreateOrderRequest request = new CreateOrderRequest(
        USER_ID,
        List.of(new OrderItemRequest(item.getId(), 1))
    );

    UserServiceUnavailableException exception = assertThrows(
        UserServiceUnavailableException.class,
        () -> orderService.createOrder(request)
    );

    assertTrue(exception.getMessage().contains(UNAVAILABLE));
  }

  private void stubUserServiceCall(String userId, HttpStatus status, String responseBody) {
    wireMockServer.stubFor(get(urlPathMatching(URL))
        .willReturn(aResponse()
            .withStatus(status.value())
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)));
  }

  private Item createTestItem(String name, BigDecimal price) {
    Item item = new Item();
    item.setName(name);
    item.setPrice(price);
    return itemRepository.save(item);
  }

  private PaymentProcessedEvent getPaymentProcessedEvent(Long orderId, String userId,
      String status) {
    PaymentProcessedEvent event = new PaymentProcessedEvent();
    event.setPaymentId(userId);
    event.setOrderId(orderId);
    event.setStatus(status);
    return event;
  }
}
