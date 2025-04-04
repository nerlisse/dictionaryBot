FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/dictionaryBot-1.0.jar ./app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]