# Stage 1: Build stage
FROM maven:3.9.16-eclipse-temurin-11 AS build
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Hardened runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a secure non-root system user/group to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy built artifact from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Expose server port
EXPOSE 8080

# Mount standard temp directory volume
VOLUME /tmp

# Configure JVM flags optimized for Docker environments
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]
