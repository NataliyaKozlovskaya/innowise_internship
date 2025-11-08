package com.innowise.order.config;


import com.innowise.order.dto.kafka.PaymentProcessedEvent;
import com.innowise.order.properties.KafkaProperties;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

  private final KafkaProperties kafkaProperties;

  public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
    this.kafkaProperties = kafkaProperties;
  }

  @Bean
  public ConsumerFactory<String, PaymentProcessedEvent> paymentProcessedConsumerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
    configProps.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumerGroupId());
    configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.innowise.orders.dto.kafka");
    configProps.put(JsonDeserializer.TYPE_MAPPINGS,
        "paymentProcessedEvent:com.innowise.orders.dto.kafka.PaymentProcessedEvent");

    return new DefaultKafkaConsumerFactory<>(configProps,
        new StringDeserializer(),
        new JsonDeserializer<>(PaymentProcessedEvent.class));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> paymentProcessedKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, PaymentProcessedEvent> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(paymentProcessedConsumerFactory());
    return factory;
  }
}