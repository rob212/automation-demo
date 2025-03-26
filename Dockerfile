# Use OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk-alpine as build

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the mvnw script executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Create a smaller runtime image
FROM eclipse-temurin:17-jre-alpine

# Install PostgreSQL client for health checks and debugging
RUN apk add --no-cache postgresql-client

# Set working directory
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Create a non-root user for running the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Health check (checks if the application is responding)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

# Environment variables (can be overridden at runtime)
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Database connection (these will be overridden at runtime with the actual values)
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=bookdb
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]