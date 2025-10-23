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
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.access.AccessDeniedHandler;
//
///**
// * Handles access denied scenarios in JWT-based authentication
// * <p>
// * This component is invoked when an authenticated user attempts to access a resource for which they
// * don't have sufficient authorities. It returns a standardized JSON response with HTTP 403 status
// * instead of the default HTML error page
// */
//public class JwtAccessDeniedHandler implements AccessDeniedHandler {
//
//  private static final ObjectMapper objectMapper = new ObjectMapper();
//
//  @Override
//  public void handle(HttpServletRequest request,
//      HttpServletResponse response,
//      AccessDeniedException accessDeniedException) throws IOException {
//    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//    Map<String, Object> body = Map.of(
//        "timestamp", LocalDateTime.now(),
//        "status", HttpStatus.FORBIDDEN.value(),
//        "error", "Forbidden",
//        "message", accessDeniedException.getMessage(),
//        "path", request.getServletPath()
//    );
//
//    objectMapper.writeValue(response.getOutputStream(), body);
//  }
//}