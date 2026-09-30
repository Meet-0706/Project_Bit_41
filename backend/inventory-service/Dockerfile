FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
# Convert CRLF to LF for mvnw just in case
RUN sed -i 's/\r$//' mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
