# Imagen del backend para producción (Render la construye desde el repositorio).

# Etapa 1: compila el .jar con el Maven Wrapper del proyecto
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src/ src/
# Las pruebas corren en el CI; aquí solo se empaqueta
RUN ./mvnw -q -B clean package -DskipTests

# Etapa 2: solo el JRE y el .jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/orlo-api-0.1.0.jar app.jar
COPY docker-entrypoint.sh docker-entrypoint.sh
RUN chmod +x docker-entrypoint.sh
ENV SPRING_PROFILES_ACTIVE=prod
# Render asigna el puerto con la variable PORT (server.port=${PORT:8090})
EXPOSE 8090
ENTRYPOINT ["/app/docker-entrypoint.sh"]
