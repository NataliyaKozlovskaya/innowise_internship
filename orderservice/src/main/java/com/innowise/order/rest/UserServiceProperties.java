package com.innowise.order.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@ConfigurationProperties(prefix = "service.user")
@Validated
public class UserServiceProperties {

  @NotBlank
  private String url;

  @NotBlank
  private String methodGetUserById;

  private int timeout = 5000; // default value if not specified in configuration
  private int maxRetries = 3;
}
