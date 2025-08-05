# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos pom.xml y mvnw/mvnw.cmd/.mvn
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copiamos el código fuente de la aplicación.
COPY src ./src

# Empaquetamos la aplicación en un archivo JAR ejecutable.
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final lightweight image
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copiamos el JAR compilado. El nombre del JAR es catalogo-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/catalogo-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto que configuramos en application.yml
EXPOSE 8083

# Definimos el comando de inicio.
ENTRYPOINT ["java", "-jar", "app.jar"]