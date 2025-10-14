# Innowise Internship Project

Microservice for managing users and bank cards.

## Tech Stack

- Java, Maven, Spring Boot
- PostgreSQL, Redis, Liquibase
- Docker, Docker Compose
- MapStruct, TestContainers
- GitHub Actions, SonarQube


## Features

- CRUD operations for users and cards
- Redis caching
- Data validation and exception handling
- Integration and unit tests


## Authentication & Authorization

### JWT Security Features
- **JWT-based authentication** with access and refresh tokens
- **BCrypt password hashing** with unique salt for each password
- **Role-based authorization** with configurable permissions
- **Stateless security** with token validation
- **Automatic token refresh** mechanism

### Security Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|

| POST | `/api/v1/auth/register` | Register new user | Public |
| POST | `/api/v1/auth/login` | User login (returns JWT tokens) | Public |
| POST | `/api/v1/auth/refresh` | Refresh access token | Public (with valid refresh token) |
| POST | `/api/v1/auth/validate` | Validate JWT token | Public |
| ALL | `/api/v1/**` | All other endpoints require JWT authentication | Protected |


### Token Information
- **Access Token**: Short-lived (default: 1 hour) for API access
- **Refresh Token**: Long-lived (default: 24 hours) for token renewal
- **Enhanced security** with proactive token refresh before expiration


### Security Configuration
- Password storage with BCrypt hashing
- JWT token signing with HMAC-SHA256
- CORS configuration for cross-origin requests
- Comprehensive exception handling for security scenarios
- CSRF protection disabled for stateless REST API


## Docker Deployment

The service is containerized with Docker and includes:
- Multi-stage build for optimized image size
- Health checks for all services
- Environment-based configuration
- PostgreSQL and Redis as external dependencies


# Order Service

Microservice for order management and processing.

## 🎯 Service Purpose

Order Service  handles the complete order lifecycle:
- Creating new orders
- Order data validation
- Order status management
- Integration with User Service for customer verification

## 🏗️ Architecture and Core Components

### Core Entities

**Order**
- Содержит информацию о пользователе, статусе и товарах
- Автоматически рассчитывает общую стоимость

**OrderItem (Позиция заказа)**
- Contains user information, status, and items
- Stores the quantity of each product

**Item**
- Catalog of available products
- Catalog of available products

## 🔄 Business Logic

### Order Creation Process

1. **User Validation** → call User Service to verify user existence
2. **Item Verification** → validate all requested items exist in database
3. **Order Creation** → calculate total price and save to database
4. **Return Result** → return DTO with order information

### Order Status Lifecycle

- **PENDING** - order created, awaiting processing (initial status)
- **PROCESSING** - order being processed
- **COMPLETED** - order successfully completed
- **CANCELLED** - order cancelled

## 🔌 Integrations

### User Service
- **Purpose**: verify user existence before order creation
- **Protocol**: HTTP REST
- **Endpoint**: `GET /api/v1/users/{userId}`
- **Error Handling**: 
  - 404 → UserNotFoundException
  - 5xx/timeouts → UserServiceUnavailableException

## 🚀 API Endpoints
# Core Operations
Method	    Path	                    Description
POST    /api/v1/orders          	Создать новый заказ
GET	    /api/v1/orders/{id}     	Получить заказ по ID
GET	    /api/v1/orders?ids=1,2,3	Получить несколько заказов по IDs
GET     /api/v1/orders/status?statuses=PENDING,PROCESSING	Получить заказы по статусам
PUT	    /api/v1/orders/{id}	        Обновить статус заказа
DELETE	/api/v1/orders/{id}	        Удалить заказ


## 🧪 Testing
# Testing Strategy
Unit tests - service business logic
Integration tests - database operations using TestContainers
WireMock tests - mocking external HTTP services
