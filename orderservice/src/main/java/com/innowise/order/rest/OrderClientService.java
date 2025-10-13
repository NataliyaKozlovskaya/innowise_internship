package com.innowise.order.rest;

import com.innowise.order.dto.UserDTO;
import com.innowise.order.exception.UserNotFoundException;
import com.innowise.order.exception.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service client for communicating with the User Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderClientService {

  @Value("${service.user.url}")
  private String host;
  @Value("${service.user.method.getUserById}")
  private String getUserById;
  private final RestTemplate restTemplate;

  public void getUserById(String userId) {
    try {
      String url = host + getUserById;

      ResponseEntity<UserDTO> response = restTemplate.getForEntity(url, UserDTO.class, userId);

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new UserNotFoundException("User not found with id: " + userId);
      }
    } catch (Exception ex) {
      log.error("Error calling user-service: {}", ex.getMessage());
      throw new UserServiceUnavailableException("User service unavailable", ex);
    }
  }
}
