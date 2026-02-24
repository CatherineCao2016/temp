# Multi-stage build for Spring Boot application
# Stage 1: Build the application
FROM maven:3.8.6-openjdk-11-slim AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Create the runtime image using distroless
FROM gcr.io/distroless/java11-debian11:nonroot

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/payment-app-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8080

# Distroless images run as non-root by default (user 65532)
# No shell available, so HEALTHCHECK is removed (use Kubernetes probes instead)

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Made with Bob