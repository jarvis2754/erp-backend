# -------------------------
# Stage 1: Build the project
# -------------------------
FROM maven:3.9.2-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the jar (skip tests for faster build)
RUN mvn clean package -DskipTests

# -------------------------
# Stage 2: Run the jar
# -------------------------
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variable for Spring profile (default)
ENV SPRING_PROFILES_ACTIVE=postgres

# Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
