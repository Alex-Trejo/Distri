# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

# Copiamos los archivos del Maven Wrapper y el directorio .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

COPY src ./src

# Empaquetamos la aplicación en un archivo JAR ejecutable.
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copiamos el JAR compilado desde la etapa de construcción anterior.
# El nombre del JAR es ms-api-gateway-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/ms-api-gateway-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto en el que la aplicación escucha.
EXPOSE 8000

# Definimos el comando que se ejecutará cuando el contenedor se inicie.
# Spring Boot puede recibir variables de entorno para sobrescribir la configuración del application.yml
# Usaremos variables de entorno para la URL de Eureka y AuthService.
ENTRYPOINT ["java", "-jar", "app.jar"]