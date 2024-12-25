# Étape 1 : Compiler le projet avec Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Utiliser une image légère de JDK 17 pour exécuter le JAR
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copier le fichier JAR généré depuis l'étape précédente
COPY --from=build /app/target/polynome-0.0.1-SNAPSHOT.jar app.jar

# Exposer le port défini dans Spring Boot
EXPOSE 8082

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
