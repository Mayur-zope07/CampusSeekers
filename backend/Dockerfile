# Dockerfile for CampusSeekers Backend Foundation
# Stage 1: Maven Build
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and pre-fetch dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source tree and compile JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root security configuration (recommended for production)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build --chown=appuser:appgroup /app/target/campusseekers-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
