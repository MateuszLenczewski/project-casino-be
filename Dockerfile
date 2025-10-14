# --- Stage 1: The Builder ---
# Use a base image with Maven and JDK 21 to build our application
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy pom.xml first to leverage Docker's layer caching
# If pom.xml doesn't change, dependencies won't be re-downloaded
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests for a faster build
RUN mvn clean install -DskipTests


# --- Stage 2: The Runner ---
# Use a minimal, secure base image with only the Java Runtime Environment
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]