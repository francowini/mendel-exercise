FROM gradle:8.5-jdk17 AS build
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew ./
COPY src ./src

RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/build/libs/mendel-transactions-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
