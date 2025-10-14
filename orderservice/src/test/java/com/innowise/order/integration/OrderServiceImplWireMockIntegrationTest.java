package com.innowise.order.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.innowise.order.dto.CreateOrderRequest;
import com.innowise.order.dto.OrderDTO;
import com.innowise.order.dto.OrderItemRequest;
import com.innowise.order.entity.Item;
import com.innowise.order.enums.OrderStatus;
import com.innowise.order.exception.ItemNotFoundException;
import com.innowise.order.exception.UserServiceUnavailableException;
import com.innowise.order.repository.ItemRepository;
import com.innowise.order.repository.OrderRepository;
import com.innowise.order.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
class OrderServiceImplWireMockIntegrationTest {

  private static final String USER_ID = "123";
  private static final String URL = "/api/v1/users/.*";
  private static final String UNAVAILABLE = "User service unavailable";

  @Autowired
  private OrderServiceImpl orderService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ItemRepository itemRepository;

  private static WireMockServer wireMockServer;

  @Container
  private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:15-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // Database properties
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    registry.add("service.user.url", () -> "http://localhost:" + wireMockServer.port());
    registry.add("service.user.methodGetUserById", () -> "/api/v1/users/");
  }

  @BeforeAll
  static void beforeAll() {
    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    wireMockServer.start();

    System.setProperty("wiremock.server.port", String.valueOf(wireMockServer.port()));
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
  void createOrder_ShouldSuccess_WhenUserExistsAndItemsAvailable() {
    Item laptop = createTestItem("Laptop", new BigDecimal("999.99"));

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

    assertNotNull(result);
    assertEquals(USER_ID, result.userId());
    assertEquals(OrderStatus.PENDING, result.status());
    assertEquals(1, result.orderItems().size());

    wireMockServer.verify(1, getRequestedFor(urlPathMatching(URL)));
  }

  @Test
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
  }

  @Test
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
}
