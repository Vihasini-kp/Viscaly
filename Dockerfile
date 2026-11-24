
FROM maven:3.10-eclipse-temurin-25-noble AS build

WORKDIR /app

COPY pom.xml .
COPY settings.xml .
COPY src ./src

RUN mvn -s settings.xml clean package -DskipTests

FROM eclipse-temurin:25-jre-noble

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
