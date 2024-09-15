FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=builder /app/target/trading.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
