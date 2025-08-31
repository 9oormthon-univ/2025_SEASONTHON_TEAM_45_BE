# 프리티어 t2.micro 최적화 Dockerfile
FROM openjdk:21-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 프리티어 메모리 제한 고려한 JVM 옵션
ENV JAVA_OPTS="-Xms128m -Xmx400m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]