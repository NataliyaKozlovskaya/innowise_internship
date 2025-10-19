package com.innowise.authentication.rest;

import com.innowise.authentication.dto.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {
  @Value("${service.user.url}")
  private String host;
  private static final String CREATE_USER = "/api/v1/users";
  private final RestTemplate restTemplate;

  public void createUser(UserCreateRequest request) {
    try {
      String url = host + CREATE_USER;
      ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("User created successfully:");
      } else {
        log.error("Failed to create user. Status: {}", response.getStatusCode());
      }
    } catch (Exception e) {
      log.error("Error calling user-service: {}", e.getMessage());
      throw new RuntimeException("User service unavailable", e);
    }
  }
}