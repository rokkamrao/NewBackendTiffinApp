# Production Dockerfile for TiffinApp Backend
FROM openjdk:21-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Create directory for logs and data
RUN mkdir -p /app/logs /app/data

# Expose the port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "-Dspring.profiles.active=prod", "target/tiffin-api-1.0.0.jar"]