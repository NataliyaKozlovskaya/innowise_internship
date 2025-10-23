//package com.innowise.apigateway.fiter;
//
//import org.springframework.stereotype.Component;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import java.util.Arrays;
//import java.util.List;
//import java.util.function.Predicate;
//
//@Component
//public class RouterValidator {
//
//  private final List<String> openEndpoints = Arrays.asList(
//      "/auth/login",
//      "/auth/register",
//      "/users/register",
//      "/actuator/health",
//      "/actuator/info"
//  );
//
//  public Predicate<ServerHttpRequest> isSecured = request -> {
//    String path = request.getURI().getPath();
//    return openEndpoints.stream()
//        .noneMatch(endpoint -> path.contains(endpoint));
//  };
//}














