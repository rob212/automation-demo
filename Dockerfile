FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add a spring user to run our application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR file
COPY --chown=spring:spring target/*.jar app.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod

# This is important - Cloud Run sets a PORT environment variable
# and your application needs to listen on that port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]