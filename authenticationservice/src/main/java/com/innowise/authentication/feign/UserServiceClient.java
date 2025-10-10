package com.innowise.authentication.feign;

import com.innowise.authentication.dto.CreateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${user.service.url}" )
public interface UserServiceClient {
  @PostMapping("/api/v1/users")
  ResponseEntity<Void> createUser(@RequestBody CreateUserRequest request);
}
