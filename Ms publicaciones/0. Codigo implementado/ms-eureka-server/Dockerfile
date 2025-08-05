# Stage 1: Build the application
# Usamos una imagen de Maven con Java 17 para compilar el código.
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Establecemos el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copiamos el archivo pom.xml primero para aprovechar el cache de Docker.
# Esto reconstruirá esta capa solo si el pom.xml cambia.
COPY pom.xml .

# Si tienes un pom.xml global/parent, cópialo también si es necesario para la resolución de dependencias.
# Si el pom.xml de ms-eureka-server ya tiene todo, esta línea no es estrictamente necesaria.
# Sin embargo, si el parent POM global tiene dependencias críticas para la compilación, se copiaría así:
# COPY ../pom.xml /app/parent-pom.xml
# Y luego podrías necesitar modificar el comando de build si Maven necesita el parent POM explícitamente.
# Para este caso, asumiremos que con el pom.xml del módulo es suficiente para `mvnw package`.

# Copiamos los archivos del Maven Wrapper y el directorio .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn


# Copiamos el código fuente de la aplicación.
COPY src ./src

# Empaquetamos la aplicación en un archivo JAR ejecutable.
# Usamos el wrapper Maven (mvnw) y saltamos los tests para una construcción más rápida.
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final lightweight image
# Usamos una imagen de Java Runtime Environment (JRE) para ejecutar la aplicación.
FROM eclipse-temurin:17-jre-focal

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos el JAR compilado desde la etapa de construcción anterior.
# El nombre del JAR se deriva del artifactId y version de tu pom.xml,
# que en este caso es ms-eureka-server-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/ms-eureka-server-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto en el que la aplicación escucha.
EXPOSE 8761

# Definimos el comando que se ejecutará cuando el contenedor se inicie.
ENTRYPOINT ["java", "-jar", "app.jar"]