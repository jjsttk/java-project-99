FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN mkdir -p /app/src/main/resources/certs

RUN openssl genpkey -out /app/src/main/resources/certs/private.pem -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
    && openssl rsa -in /app/src/main/resources/certs/private.pem -pubout -out /app/src/main/resources/certs/public.pem

RUN ./gradlew build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]
