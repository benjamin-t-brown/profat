# Build stage
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -B

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app

# Default: store SQLite in /data (mount a volume here for host access)
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/data/profat.db
ENV SPRING_PROFILES_ACTIVE=prod
# Required at docker run: -e PROFAT_API_KEY=... (prod profile enforces a non-blank API key)

EXPOSE 9002

COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
