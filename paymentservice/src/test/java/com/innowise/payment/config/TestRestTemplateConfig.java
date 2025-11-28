package com.innowise.payment.config;

import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class TestRestTemplateConfig {

  @Bean
  @Primary
  public RestTemplate testRestTemplate() {
    return new RestTemplateBuilder()
        .setConnectTimeout(Duration.ofMillis(500))
        .setReadTimeout(Duration.ofMillis(500))
        .build();
  }
}