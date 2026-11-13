# Imagen del backend para Render (o cualquier servicio que ejecute contenedores Docker).
# Se construye en dos etapas: la primera compila con Maven y la segunda solo ejecuta el .jar,
# así la imagen final no lleva Maven, el código fuente ni el JDK completo (pesa mucho menos).

# ---- Etapa 1: compilar ----
FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /app

# Primero solo el pom.xml: Docker guarda esta capa y, si el pom no cambió, no vuelve a descargar las dependencias
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
# Las pruebas ya se corren en la PC; aquí no hay base de datos ni variables para la de contextLoads
RUN mvn -B -q package -DskipTests

# ---- Etapa 2: ejecutar ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Por seguridad, la app no corre como administrador (root) del contenedor
RUN addgroup -S gams && adduser -S gams -G gams
USER gams

COPY --from=compilacion /app/target/finanzas-0.0.1-SNAPSHOT.jar app.jar

# El plan gratuito de Render tiene 512 MB de RAM y poca CPU:
# - MaxRAMPercentage=60: el heap usa hasta ~300 MB y deja espacio al resto de la JVM (si se pasa, Render la reinicia)
# - SerialGC y TieredStopAtLevel=1: menos hilos y compilación más simple, para que arranque más rápido
# - Xss512k: cada hilo reserva menos memoria para su pila
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
# Pocos usuarios a la vez: menos hilos de Tomcat y menos conexiones a Neon.
# (Spring lee server.tomcat.threads.max y spring.datasource.hikari.maximum-pool-size desde estas
# variables: los puntos pasan a "_" y los guiones se quitan)
ENV SERVER_TOMCAT_THREADS_MAX=20
ENV SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=5

# Render indica el puerto en la variable PORT (application.properties ya la lee); 8080 es el de respaldo
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
