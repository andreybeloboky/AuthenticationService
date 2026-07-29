FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/authentication-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]