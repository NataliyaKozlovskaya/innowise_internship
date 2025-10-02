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

## Quick Start

```bash
docker-compose up -d 

docker exec -it innowise-postgres psql -U postgres -d innowise_db - сonnecting to a database in Docker