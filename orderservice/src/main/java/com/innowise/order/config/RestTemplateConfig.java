package com.innowise.order.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.innowise.order.rest.UserServiceProperties;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for RestTemplate bean setup
 */
@Configuration
public class RestTemplateConfig {

  private final UserServiceProperties userServiceProperties;

  public RestTemplateConfig(UserServiceProperties userServiceProperties) {
    this.userServiceProperties = userServiceProperties;
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());

    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(objectMapper);

    return builder
        .rootUri(userServiceProperties.getUrl())
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .additionalMessageConverters(converter)
        .build();
  }
}