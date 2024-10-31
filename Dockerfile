# Sử dụng JDK 17 để build
FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /app

# Copy mã nguồn và build dự án
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
# COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose port cho backend (thường là 8080)
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
