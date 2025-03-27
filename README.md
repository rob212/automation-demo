# Automation Demo

A Spring Boot REST API application demonstrating a modern CI/CD pipeline with multi-environment deployment (dev/staging/prod).

## Project Overview

This is a sample Book API service built with Spring Boot that demonstrates:

- RESTful API development with Spring Boot
- PostgreSQL database integration
- Comprehensive testing strategy (unit and container-based integration tests)
- CI/CD pipeline using GitHub Actions
- Infrastructure as Code with Terraform
- Multi-environment deployment to Google Cloud Platform

## Prerequisites

- Java 17 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- Docker and Docker Compose
- PostgreSQL (for local development without Docker)
- Git

## Local Development Setup

### Option 1: Running with local PostgreSQL

1. Ensure you have PostgreSQL installed and running locally
2. Create a database named `bookdb`:
   ```sql
   CREATE DATABASE bookdb;
   ```
3. The default configuration uses:
   - Host: localhost
   - Port: 5432
   - Database: bookdb
   - Username: postgres
   - Password: postgres

4. Build and run the application:
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

### Option 2: Running with Docker

1. Create a `docker-compose.yml` file in the project root:
   ```yaml
   version: '3.8'
   
   services:
     app:
       build: .
       ports:
         - "8080:8080"
       depends_on:
         - postgres
       environment:
         - SPRING_PROFILES_ACTIVE=dev
         - DB_HOST=postgres
         - DB_PORT=5432
         - DB_NAME=bookdb
         - DB_USER=postgres
         - DB_PASSWORD=postgres
     
     postgres:
       image: postgres:13.3
       ports:
         - "5432:5432"
       environment:
         - POSTGRES_DB=bookdb
         - POSTGRES_USER=postgres
         - POSTGRES_PASSWORD=postgres
       volumes:
         - postgres-data:/var/lib/postgresql/data
   
   volumes:
     postgres-data:
   ```

2. Build and run with Docker Compose:
   ```bash
   docker-compose up --build
   ```

## API Endpoints

The application exposes the following REST endpoints:

- `GET /api/books` - Get all books
- `GET /api/books/{id}` - Get book by ID
- `GET /api/books/isbn/{isbn}` - Get book by ISBN
- `POST /api/books` - Create a new book
- `PUT /api/books/{id}` - Update a book
- `DELETE /api/books/{id}` - Delete a book
- `GET /api/books/search` - Search books by filters (title, author, genre, publisher, isbn)

## Testing

### Running Unit Tests

Unit tests are run by default when building the application:

```bash
./mvnw clean test
```

This will run all tests except those ending with `*IT.java` or `*ContainerIT.java`.

### Running Integration Tests with Testcontainers

Integration tests use Testcontainers to spin up a PostgreSQL container:

```bash
./mvnw clean verify -P container-tests
```

This will run all tests with the `*ContainerIT.java` naming pattern.

### Running All Tests

To run both unit and integration tests:

```bash
./mvnw clean verify -P unit-tests,container-tests
```

## CI/CD Pipeline

This project uses GitHub Actions for CI/CD with the following stages:
1. Build and Test (Maven, Java 17)
2. Docker Image Build/Push to GCP Artifact Registry
3. Terraform Infrastructure Management
4. Cloud Run Deployment

The pipeline deploys to three environments:
- Development (dev)
- Staging
- Production (prod)

Each environment has its own GCP project and configuration.

## Infrastructure

The infrastructure is managed using Terraform with the following components:
- GCP Projects for each environment
- Service accounts with appropriate IAM permissions
- Artifact Registry for Docker images
- Cloud Run services
- Storage buckets for Terraform state

## Required GCP Setup

Before running the CI/CD pipeline, ensure:
1. GCP projects are created for each environment
2. Billing is enabled for all projects
3. Required GitHub secrets are configured:
   - Service Account Keys (GCP_SA_KEY_DEV, GCP_SA_KEY_STAGING, GCP_SA_KEY_PROD)
   - Project IDs (GCP_PROJECT_DEV, GCP_PROJECT_STAGING, GCP_PROJECT_PROD)

## License

[Add your license information here]
