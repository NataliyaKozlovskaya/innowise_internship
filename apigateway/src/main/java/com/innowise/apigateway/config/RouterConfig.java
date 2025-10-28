package com.innowise.apigateway.config;

import com.innowise.apigateway.controller.AuthController;
import com.innowise.apigateway.controller.CardController;
import com.innowise.apigateway.controller.OrderController;
import com.innowise.apigateway.controller.UserController;
import com.innowise.apigateway.filter.JwtRouterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Configuration class for defining API routes and endpoint mappings
 */
@Configuration
public class RouterConfig {

  private final AuthController authController;
  private final UserController userController;
  private final OrderController orderController;
  private final CardController cardController;
  private final JwtRouterFilter jwtFilter;

  public RouterConfig(AuthController authController,
      UserController userController,
      OrderController orderController,
      CardController cardController,
      JwtRouterFilter jwtFilter) {
    this.authController = authController;
    this.userController = userController;
    this.orderController = orderController;
    this.cardController = cardController;
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public RouterFunction<ServerResponse> route() {
    return RouterFunctions.route()
        .POST("/api/v1/auth/register", authController::register)
        .POST("/api/v1/auth/login", authController::login)
        .POST("/api/v1/auth/refresh", authController::refreshToken)
        .POST("/api/v1/auth/validate", authController::validateToken)

        // Specific routes to general ones
        .GET("/api/v1/users/batch", userController::getUsersByIds)
        .GET("/api/v1/users/email", userController::getUserByEmail)
        .GET("/api/v1/users/{id}", userController::getUserById)

        .PATCH("/api/v1/users/{id}", userController::updateUser)
        .DELETE("/api/v1/users/{id}", userController::deleteUser)

        .POST("/api/v1/orders", orderController::createOrder)
        .GET("/api/v1/orders/batch", orderController::getOrdersByIds)
        .GET("/api/v1/orders/status", orderController::getOrdersByStatuses)
        .GET("/api/v1/orders/{id}", orderController::getOrderById)
        .PATCH("/api/v1/orders/{id}/status", orderController::updateOrderStatus)
        .DELETE("/api/v1/orders/{id}", orderController::deleteOrder)

        .GET("/api/v1/cards/batch", cardController::getCardsByIds)
        .GET("/api/v1/cards/user/{id}", cardController::getCardByUserId)
        .GET("/api/v1/cards/{id}", cardController::getCardById)  // general route AFTER specific
        .PATCH("/api/v1/cards/{id}", cardController::updateCard)
        .POST("/api/v1/cards", cardController::createCard)
        .DELETE("/api/v1/cards/{id}", cardController::deleteCard)

        .filter(jwtFilter)
        .build();
  }
}
