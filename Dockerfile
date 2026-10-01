# ============================================================
# ESTÁGIO 1 — BUILD (compila o JAR com Maven)
# ============================================================
FROM maven:3.8.6-openjdk-11 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# ESTÁGIO 2 — RUNTIME (executa o JAR)
# ============================================================
FROM amazoncorretto:11-alpine-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Timezone via variável de ambiente (Alpine já suporta)
ENV TZ=America/Sao_Paulo
ENV JAVA_OPTS="-Xms512m -Xmx2g"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]