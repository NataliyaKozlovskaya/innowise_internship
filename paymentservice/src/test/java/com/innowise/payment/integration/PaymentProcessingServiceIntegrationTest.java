//package com.innowise.payment.integration;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
//import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
//import static com.github.tomakehurst.wiremock.client.WireMock.get;
//import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
//import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.tomakehurst.wiremock.WireMockServer;
//import com.innowise.payment.util.TestKafkaConsumer;
//import com.innowise.payment.dto.PaymentDTO;
//import com.innowise.payment.dto.kafka.OrderCreatedEvent;
//import com.innowise.payment.dto.kafka.PaymentProcessedEvent;
//import com.innowise.payment.enums.PaymentStatus;
//import com.innowise.payment.kafka.PaymentProcessingService;
//import com.innowise.payment.service.PaymentService;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.containers.MongoDBContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
///**
// * Tests for class {@link PaymentProcessingService}
// */
//@SpringBootTest
//@Testcontainers
//@ActiveProfiles("test")
//class PaymentProcessingServiceIntegrationTest {
//
//  @Container
//  static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");
//
//  @Container
//  static KafkaContainer kafkaContainer = new KafkaContainer(
//      DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
//
//  public static final Long ORDER_ID = 1L;
//  public static final String USER_ID = "123";
//  public static final BigDecimal AMOUNT = new BigDecimal(100.50);
//
//  private static WireMockServer wireMockServer;
//
//  @Autowired
//  private PaymentProcessingService paymentProcessingService;
//
//  @Autowired
//  private PaymentService paymentService;
//
//  @Autowired
//  private MongoTemplate mongoTemplate;
//
//  @Autowired
//  private ObjectMapper objectMapper;
//
//  @BeforeAll
//  static void beforeAll() {
//    wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
//    wireMockServer.start();
//  }
//
//  @AfterAll
//  static void afterAll() {
//    if (wireMockServer != null) {
//      wireMockServer.stop();
//    }
//  }
//
//  @BeforeEach
//  void setUp() {
//    wireMockServer.resetAll();
//    mongoTemplate.getDb().getCollection("payments").deleteMany(new org.bson.Document());
//  }
//
//  @DynamicPropertySource
//  static void configureProperties(DynamicPropertyRegistry registry) {
//    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//    registry.add("spring.data.mongodb.database", () -> "testdb");
//
//    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
//    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
//    registry.add("spring.kafka.producer.acks", () -> "all");
//
//    registry.add("external.payment.service.url",
//        () -> "http://localhost:" + wireMockServer.port());
//  }
//
//  @Test
//  @DisplayName("Should process payment successfully when order created event received")
//  void whenOrderCreatedEventReceived_thenPaymentProcessedSuccessfully() throws Exception {
//    OrderCreatedEvent orderCreatedEvent = getOrderCreatedEvent(AMOUNT, ORDER_ID, USER_ID);
//
//    wireMockServer.stubFor(get(anyUrl())
//        .willReturn(aResponse()
//            .withHeader("Content-Type", "text/plain")
//            .withBody("12345")
//            .withStatus(200)));
//
//    // Setup Kafka consumer
//    try (TestKafkaConsumer testConsumer = new TestKafkaConsumer(
//        kafkaContainer.getBootstrapServers(), "order-payment-processed")) {
//
//      paymentProcessingService.processOrderCreatedEvent(orderCreatedEvent);
//
//      Thread.sleep(2000);
//
//      List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(ORDER_ID);
//      assertThat(payments).hasSize(1);
//
//      PaymentDTO payment = payments.getFirst();
//      assertThat(payment.orderId()).isEqualTo(ORDER_ID);
//      assertThat(payment.userId()).isEqualTo(USER_ID);
//      assertThat(payment.paymentAmount()).isEqualTo(AMOUNT);
//      assertThat(payment.status()).isIn(PaymentStatus.FAILED, PaymentStatus.COMPLETED);
//
//      ConsumerRecord<String, String> record = testConsumer.pollRecord(10, TimeUnit.SECONDS);
//      assertThat(record).isNotNull();
//
//      PaymentProcessedEvent paymentProcessedEvent = objectMapper.readValue(
//          record.value(), PaymentProcessedEvent.class);
//
//      assertThat(paymentProcessedEvent.getOrderId()).isEqualTo(ORDER_ID);
//      assertThat(payment.status()).isIn(PaymentStatus.FAILED, PaymentStatus.COMPLETED);
//
//      wireMockServer.verify(1, getRequestedFor(anyUrl()));
//    }
//  }
//
//  @Test
//  @DisplayName("Should use fallback payment ID when external service returns errors")
//  void whenExternalServiceErrors_thenUsesFallbackPaymentId() throws Exception {
//    OrderCreatedEvent orderCreatedEvent = getOrderCreatedEvent(AMOUNT, ORDER_ID, USER_ID);
//
//    wireMockServer.stubFor(get(anyUrl())
//        .willReturn(aResponse()
//            .withStatus(503)
//            .withBody("Service unavailable")));
//
//    try (TestKafkaConsumer paymentProcessedConsumer = new TestKafkaConsumer(
//        kafkaContainer.getBootstrapServers(),
//        "order-payment-processed")) {
//
//      paymentProcessingService.processOrderCreatedEvent(orderCreatedEvent);
//
//      ConsumerRecord<String, String> record = paymentProcessedConsumer.pollRecord(10,
//          TimeUnit.SECONDS);
//      assertThat(record).isNotNull();
//
//      PaymentProcessedEvent event = objectMapper.readValue(record.value(),
//          PaymentProcessedEvent.class);
//
//      assertThat(event.getOrderId()).isEqualTo(ORDER_ID);
//      assertThat(event.getPaymentId()).isNotNull();
//      assertThat(event.getStatus()).isIn(PaymentStatus.COMPLETED, PaymentStatus.FAILED);
//
//      wireMockServer.verify(3, getRequestedFor(anyUrl()));
//
//      List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(ORDER_ID);
//      assertThat(payments).hasSize(1);
//      assertThat(payments.getFirst().status()).isEqualTo(event.getStatus());
//    }
//  }
//
//  private static @NotNull OrderCreatedEvent getOrderCreatedEvent(BigDecimal amount, Long orderId,
//      String userId) {
//    OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
//    orderCreatedEvent.setAmount(amount);
//    orderCreatedEvent.setOrderId(orderId);
//    orderCreatedEvent.setUserId(userId);
//
//    return orderCreatedEvent;
//  }
//}
