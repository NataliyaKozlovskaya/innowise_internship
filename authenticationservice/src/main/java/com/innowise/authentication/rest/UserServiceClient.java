package com.innowise.authentication.rest;

import com.innowise.authentication.dto.CreateUserRequest;
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
  private String createUser = "/api/v1/users";
  private final RestTemplate restTemplate;

  public void createUser(CreateUserRequest request) {
    try {
      String url = host + createUser;
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