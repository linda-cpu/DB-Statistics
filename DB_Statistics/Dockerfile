# 1. Wir nutzen ein "Early Access" Image für Java 25
FROM openjdk:25-ea-jdk-oracle

# 2. Arbeitsverzeichnis im Container festlegen
WORKDIR /app

# 3. Das JAR File (das wir gleich bauen) in den Container kopieren
# Hinweis: Der Name "*.jar" nimmt automatisch das gebaute File, egal wie die Version heißt
COPY target/*.jar app.jar

# 4. Port 8080 freigeben
EXPOSE 8080

# 5. Die Anwendung starten
ENTRYPOINT ["java", "-jar", "app.jar"]