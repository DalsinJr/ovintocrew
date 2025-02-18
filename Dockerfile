FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/ovintocrew-0.0.1-SNAPSHOT.jar ovintocrew.jar

EXPOSE 8080

CMD ["java", "-jar", "ovintocrew.jar"]