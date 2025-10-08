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


## Quick Start

```bash
docker-compose up -d 

docker exec -it innowise-postgres psql -U postgres -d innowise_db - сonnecting to a database in Docker