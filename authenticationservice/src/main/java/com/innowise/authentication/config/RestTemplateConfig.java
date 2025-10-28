package com.innowise.authentication.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Value(value = "${service.user.url}")
  private String url;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .rootUri(url)
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .build();
  }
}