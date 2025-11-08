package com.innowise.payment.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "external.payment.service")
  public class ExternalServiceProperties {

    private String externalServiceUrl;
  }
