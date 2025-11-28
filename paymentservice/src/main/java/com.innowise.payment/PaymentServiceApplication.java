package com.innowise.payment;

import com.innowise.payment.properties.ExternalServiceProperties;
import com.innowise.payment.properties.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableConfigurationProperties({ExternalServiceProperties.class, KafkaProperties.class})
public class PaymentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
