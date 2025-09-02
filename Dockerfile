FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]