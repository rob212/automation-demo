package com.example.automationdemo.automationdemo;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class HelloApiContainerIT {

  // Retrieve pre-build image for use in our tests
  @Container
  public static GenericContainer<?> container =
      new GenericContainer<>("demo-api:latest").withExposedPorts(8080);

  @BeforeEach
  public void setUp() {
    baseURI = "http://localhost";
    port = container.getMappedPort(8080);
    System.out.println("Container started at port: " + port);
  }

  @Test
  public void testHelloEndpoint() {
    given().when().get("/api/hello").then().statusCode(200).body(equalTo("Hello, World!"));
  }

  @Test
  public void testHealthEndpoint() {
    given().when().get("/actuator/health").then().statusCode(200).body("status", equalTo("UP"));
  }
}
