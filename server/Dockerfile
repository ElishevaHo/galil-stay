# שלב 1: בניית קובץ ה-jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# שלב 2: הרצה
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/server-*.jar galilstay.jar
EXPOSE 8080
CMD ["java", "-Xmx384m", "-jar", "galilstay.jar"]
