# Stage 1: Build dự án bằng Maven và JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy file pom.xml và tải trước các dependency để tối ưu bộ nhớ đệm (Cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và tiến hành đóng gói (bỏ qua chạy test để build nhanh)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Tạo image chạy thành phẩm siêu nhẹ (chỉ chứa JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file .jar đã build từ Stage 1 sang Stage 2
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8086 của container
EXPOSE 8086

# Lệnh kích hoạt ứng dụng khi container khởi chạy
ENTRYPOINT ["java", "-jar", "app.jar"]