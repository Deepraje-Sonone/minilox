
# Stage 1: Build the application
FROM maven:3.9.16-eclipse-temurin-25 AS build

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY minilox-core/pom.xml minilox-core/pom.xml
COPY minilox-web/pom.xml minilox-web/pom.xml

# Copy application source code
COPY minilox-core/src minilox-core/src
COPY minilox-web/src minilox-web/src

# Build the executable Spring Boot JAR
RUN mvn -B -pl minilox-web -am clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/minilox-web/target/minilox-web-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
