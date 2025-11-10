package com.innowise.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Kafka connection and topic settings
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {

  private String bootstrapServers;
  private String consumerGroupId;
  private Topics topics = new Topics();

  @Data
  public static class Topics {

    private String orderCreated;
    private String orderPaymentProcessed;
    private String paymentFailed;
  }
}