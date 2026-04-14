# Build stage
FROM maven:3-eclipse-temurin-21-alpine AS build 
# Note: Use a Maven image that supports your target Java version. 
# As of now, you might need to build the JAR locally first using ./mvnw package
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:25-jre-alpine
COPY --from=build /target/DB_Management-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]