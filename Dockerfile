# Etapa 1: Construir la aplicación
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar los archivos del proyecto
COPY . .

# Dar permisos de ejecución a gradlew y compilar el proyecto (omitiendo los tests para acelerar la construcción)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# Etapa 2: Ejecutar la aplicación
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde la etapa de construcción
COPY --from=builder /app/build/libs/*.jar app.jar

# Exponer el puerto en el que correrá la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
