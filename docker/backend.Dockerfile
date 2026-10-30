FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw -B dependency:go-offline
COPY backend/src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system bookora && mkdir -p /app/logs /app/storage && chown -R bookora /app
COPY --from=build /app/target/bookora-backend-*.jar app.jar
USER bookora
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
