# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache dependencies first
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Build the app (skip tests for faster/robust deploy)
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/fingerbot-0.0.1-SNAPSHOT.jar app.jar

# Railway/Render/Koyeb inject $PORT; app reads server.port=${PORT:8080}
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
