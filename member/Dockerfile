# Use Eclipse Temurin JDK 21 as base image
FROM eclipse-temurin:21-jdk

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/member-0.0.1-SNAPSHOT.jar app.jar

# Expose the gateway port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
