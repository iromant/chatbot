FROM openjdk:11-jdk-slim

WORKDIR /app

COPY . .

RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

CMD ["java", "-jar", "target/*.jar"]