package com.innowise.payment.config;

import com.innowise.payment.dto.kafka.OrderCreatedEvent;
import com.innowise.payment.properties.KafkaProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Configuration class for kafka
 */
@Configuration
public class KafkaConfig {

  private final KafkaProperties kafkaProperties;

  public KafkaConfig(KafkaProperties kafkaProperties) {
    this.kafkaProperties = kafkaProperties;
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
    configs.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
    return new KafkaAdmin(configs);
  }

  @Bean
  public NewTopic orderCreatedTopic() {
    return new NewTopic(
        kafkaProperties.getTopics().getOrderCreated(),
        1,
        (short) 1
    );
  }

  @Bean
  public NewTopic orderPaymentProcessedTopic() {
    return new NewTopic(
        kafkaProperties.getTopics().getOrderPaymentProcessed(),
        1,
        (short) 1
    );
  }

  @Bean
  public NewTopic paymentFailedTopic() {
    return new NewTopic(
        kafkaProperties.getTopics().getPaymentFailed(),
        1,
        (short) 1
    );
  }

  @Bean
  public ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    configProps.put(ProducerConfig.ACKS_CONFIG, "all");
    configProps.put(ProducerConfig.RETRIES_CONFIG, 3);

    configProps.put("spring.json.add.type.headers", false);
    configProps.put("spring.json.type.mapping", "");

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, getConsumerGroupId());
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

    configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    return new DefaultKafkaConsumerFactory<>(configProps,
        new StringDeserializer(),
        new JsonDeserializer<>(OrderCreatedEvent.class));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

  private String getBootstrapServers() {
    return kafkaProperties != null && kafkaProperties.getBootstrapServers() != null
        ? kafkaProperties.getBootstrapServers()
        : "kafka:9092";
  }

  private String getConsumerGroupId() {
    return kafkaProperties != null && kafkaProperties.getConsumerGroupId() != null
        ? kafkaProperties.getConsumerGroupId()
        : "payment-service-group";
  }
}