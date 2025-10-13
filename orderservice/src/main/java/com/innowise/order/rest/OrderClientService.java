package com.innowise.order.rest;

import com.innowise.order.dto.UserDTO;
import com.innowise.order.exception.UserNotFoundException;
import com.innowise.order.exception.UserServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final RestTemplate restTemplate;
  private final UserServiceProperties userServiceProperties;

  public void getUserById(String userId) {
    log.info("Start calling user-service with userId: {}", userId);

    String url = userServiceProperties.getUrl();
    String methodGetUserById = userServiceProperties.getMethodGetUserById();
    String fullUrl = url + methodGetUserById + userId;
    try {
      ResponseEntity<UserDTO> response = restTemplate.getForEntity(fullUrl, UserDTO.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new UserNotFoundException("User not found with id: " + userId);
      }
    } catch (Exception ex) {
      log.error("Error calling user-service: {}", ex.getMessage());
      throw new UserServiceUnavailableException("User service unavailable", ex);
    }
  }
}
