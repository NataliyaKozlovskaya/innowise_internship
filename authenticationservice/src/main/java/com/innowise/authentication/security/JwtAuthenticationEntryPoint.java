//package com.innowise.authentication.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Map;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//
///**
// * Handles authentication entry point for JWT-based security
// * <p>
// * This component is invoked when an unauthenticated user attempts to access a secured resource. It
// * returns a standardized JSON response with HTTP 401 status instead of the default redirect to
// * login page or HTML error page
// */
//public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
//
//  private static final ObjectMapper objectMapper = new ObjectMapper();
//
//  @Override
//  public void commence(HttpServletRequest request,
//      HttpServletResponse response,
//      AuthenticationException authException) throws IOException {
//
//    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//    Map<String, Object> body = Map.of(
//        "timestamp", LocalDateTime.now(),
//        "status", HttpStatus.UNAUTHORIZED.value(),
//        "error", "Unauthorized",
//        "message", authException.getMessage(),
//        "path", request.getServletPath()
//    );
//
//    objectMapper.writeValue(response.getOutputStream(), body);
//  }
//}