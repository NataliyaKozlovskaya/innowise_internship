package com.innowise.apigateway.config;

import com.innowise.apigateway.handler.AuthHandler;
import com.innowise.apigateway.handler.CardHandler;
import com.innowise.apigateway.handler.OrderHandler;
import com.innowise.apigateway.handler.PaymentHandler;
import com.innowise.apigateway.handler.UserHandler;
import com.innowise.apigateway.filter.JwtRouterFilter;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Configuration class for defining API routes and endpoint mappings
 */
@Configuration
public class RouterConfig {

  private final AuthHandler authHandler;
  private final UserHandler userHandler;
  private final OrderHandler orderHandler;
  private final CardHandler cardHandler;
  private final PaymentHandler paymentHandler;
  private final JwtRouterFilter jwtFilter;

  public RouterConfig(AuthHandler authHandler, UserHandler userHandler, OrderHandler orderHandler,
      CardHandler cardHandler, PaymentHandler paymentHandler, JwtRouterFilter jwtFilter) {
    this.authHandler = authHandler;
    this.userHandler = userHandler;
    this.orderHandler = orderHandler;
    this.cardHandler = cardHandler;
    this.paymentHandler = paymentHandler;
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public RouterFunction<ServerResponse> route() {
    return RouterFunctions.route()
        .filter(jwtFilter)
        .path("/api/v1", builder -> builder
            .path("/auth", authBuilder -> authBuilder
                .POST("/register", authHandler::register)
                .POST("/login", authHandler::login)
                .POST("/refresh", authHandler::refreshToken)
                .POST("/validate", authHandler::validateToken)
            )
            .path("/users", userBuilder -> userBuilder
                .GET("/batch", userHandler::getUsersByIds)
                .GET("/email", userHandler::getUserByEmail)
                .GET("/{id}", userHandler::getUserById)
                .PATCH("/{id}", userHandler::updateUser)
                .DELETE("/{id}", userHandler::deleteUser)
            )
            .path("/orders", orderBuilder -> orderBuilder
                .POST("/", orderHandler::createOrder)
                .GET("/batch", orderHandler::getOrdersByIds)
                .GET("/status", orderHandler::getOrdersByStatuses)
                .GET("/{id}", orderHandler::getOrderById)
                .PATCH("/{id}/status", orderHandler::updateOrderStatus)
                .DELETE("/{id}", orderHandler::deleteOrder)
            )
            .path("/cards", cardBuilder -> cardBuilder
                .GET("/batch", cardHandler::getCardsByIds)
                .GET("/{id}", cardHandler::getCardByUserId)
                .GET("/{id}", cardHandler::getCardById)
                .PATCH("/{id}", cardHandler::updateCard)
                .POST("/", cardHandler::createCard)
                .DELETE("/{id}", cardHandler::deleteCard)
            )
            .path("/payments", paymentBuilder -> paymentBuilder
                .GET("/user/{userId}", paymentHandler::getPaymentsByUserId)
                .GET("/order/{orderId}", paymentHandler::getPaymentsByOrderId)
                .GET("/status", paymentHandler::getPaymentsByPaymentStatuses)
                .GET("/total", paymentHandler::getTotalSumOfPaymentsForPeriod)
            )
        )
                .onError(Exception.class, this::handleError)
                .build();
  }

  private Mono<ServerResponse> handleError(Exception error, ServerRequest request) {
    return ServerResponse.badRequest()
        .bodyValue(Map.of(
            "error", "Request processing failed",
            "message", error.getMessage(),
            "path", request.path(),
            "timestamp", Instant.now()
        ));
  }
}
