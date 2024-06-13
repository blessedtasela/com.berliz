# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the target directory to the container
COPY target/com.berliz.jar /usr/app/com.berliz.jar

# Expose the port the application will run on
EXPOSE 8001

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app/com.berliz.jar"]
