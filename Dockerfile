# ==========================================
# 1. Build Stage
# ==========================================
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Gradle Wrapper 및 설정 파일 먼저 복사 (레이어 캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Wrapper 실행 권한 부여 및 의존성 다운로드 (소스 변경과 무관하게 캐싱됨)
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 후 bootJar 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ==========================================
# 2. Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 보안: root 대신 전용 시스템 계정으로 애플리케이션 실행
RUN addgroup --system spring && adduser --system --ingroup spring spring

# 기본 Spring 프로파일 (컨테이너 실행 시 환경변수로 덮어쓰기 가능)
ENV SPRING_PROFILES_ACTIVE=dev

# 빌드 스테이지에서 생성된 실행 가능한 Fat Jar만 복사
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
