# Use Maven with OpenJDK for development
FROM maven:3.9.6-openjdk-21

# Set the working directory
WORKDIR /app

# Copy the entire project
COPY . /app

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Default command: run Spring Boot in dev mode
CMD ["mvn", "spring-boot:run"]
