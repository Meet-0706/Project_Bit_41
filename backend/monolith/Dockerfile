FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/monolith-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
