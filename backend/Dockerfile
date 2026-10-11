# ---- Build ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
# Les dépendances sont téléchargées dans une couche séparée, réutilisée tant que le pom.xml ne change pas
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Exécution ----
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/storage/cv && chown -R app:app /app
COPY --from=build /build/target/jobboard-api-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
