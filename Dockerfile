# -------------------------
# Stage 1: Build the project
# -------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to download dependencies (better caching)
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the jar (skip tests compilation)
RUN mvn clean package -Dmaven.test.skip=true


# -------------------------
# Stage 2: Run the application
# -------------------------
FROM eclipse-temurin:21-jre-jammy

# Set working directory
WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Default Spring profile (can override with ENV in Render)
ENV SPRING_PROFILES_ACTIVE=postgres

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
