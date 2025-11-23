# Render 배포 가이드 (Docker)

이 가이드는 Spring Boot 채팅 애플리케이션을 Render에 Docker를 사용하여 배포하는 방법을 설명합니다.

## 📋 사전 준비사항

1. **Render 계정**: [render.com](https://render.com) 가입
2. **GitHub 저장소**: 프로젝트 코드가 GitHub에 푸시되어 있어야 함
3. **PostgreSQL 데이터베이스**: Supabase 또는 Render PostgreSQL 설정

## 🐳 Docker 파일 설명

### Dockerfile

프로젝트 루트에 생성된 `Dockerfile`은 다음과 같은 멀티 스테이지 빌드를 사용합니다:

#### Stage 1: Build Stage
- **베이스 이미지**: `eclipse-temurin:21-jdk-jammy`
- Gradle wrapper를 사용하여 의존성 다운로드
- 소스 코드 빌드 및 JAR 파일 생성
- **Java 21** 버전 사용 (프로젝트 요구사항)

#### Stage 2: Runtime Stage
- **베이스 이미지**: `eclipse-temurin:21-jre-jammy` (JRE만 포함하여 이미지 크기 최적화)
- 보안을 위한 non-root 사용자 생성
- Health check 설정
- 컨테이너 최적화 JVM 설정

### .dockerignore

불필요한 파일을 Docker 이미지에서 제외하여 빌드 속도를 향상시킵니다.

## 🚀 Render 배포 방법

### 방법 1: GitHub 저장소에서 직접 배포 (권장)

1. **Render 대시보드 접속**
   - [dashboard.render.com](https://dashboard.render.com) 로그인

2. **새 Web Service 생성**
   - `New +` 버튼 클릭
   - `Web Service` 선택

3. **저장소 연결**
   - GitHub 저장소 연결
   - `chat-back` 저장소 선택

4. **서비스 설정**
   ```
   Name: chat-backend (또는 원하는 이름)
   Region: Singapore (가장 가까운 지역 선택)
   Branch: master (또는 main)
   Runtime: Docker
   ```

5. **빌드 설정**
   ```
   Root Directory: . (프로젝트 루트)
   Dockerfile Path: ./Dockerfile

   Build Command: (비워두기 - Docker가 처리)
   Start Command: (비워두기 - Docker가 처리)
   ```

6. **환경 변수 설정**
   ```
   DB_URL=your-supabase-connection-url
   DB_USERNAME=your-database-username
   DB_PASSWORD=your-database-password
   DB_POOL_SIZE=5
   DB_MIN_IDLE=2
   ```

7. **인스턴스 타입 선택**
   - Free tier 또는 필요에 따라 유료 플랜 선택
   - Free tier는 512MB RAM, 공유 CPU 제공

8. **생성 및 배포**
   - `Create Web Service` 클릭
   - Render가 자동으로 Docker 이미지를 빌드하고 배포

### 방법 2: Docker Hub를 통한 배포

1. **로컬에서 Docker 이미지 빌드**
   ```bash
   docker build -t your-dockerhub-username/chat-backend:latest .
   ```

2. **Docker Hub에 푸시**
   ```bash
   docker login
   docker push your-dockerhub-username/chat-backend:latest
   ```

3. **Render에서 배포**
   - `New +` → `Web Service`
   - `Deploy an existing image from a registry` 선택
   - Image URL: `your-dockerhub-username/chat-backend:latest`
   - 환경 변수 설정 (위와 동일)

## 🔧 주요 설정 포인트

### 포트 설정
- Render는 자동으로 `PORT` 환경 변수를 제공하지만, 현재 애플리케이션은 `8080` 포트 사용
- Dockerfile에서 `EXPOSE 8080` 설정으로 Render가 자동 감지

### 데이터베이스 연결
- Supabase를 사용하는 경우: Connection Pooler URL 사용 권장
- Render PostgreSQL을 사용하는 경우:
  1. `New +` → `PostgreSQL` 생성
  2. 자동 생성된 `DATABASE_URL`을 `DB_URL`로 매핑

### 메모리 설정
- Dockerfile의 JVM 옵션: `-XX:MaxRAMPercentage=75.0`
- Free tier (512MB)에서도 안정적으로 작동하도록 최적화

### Health Check
- 엔드포인트: `/actuator/health`
- Spring Boot Actuator를 사용하는 경우 자동으로 health check 제공
- Render는 health check를 통해 애플리케이션 상태 모니터링

## 📊 배포 후 확인사항

1. **로그 확인**
   - Render 대시보드에서 `Logs` 탭 확인
   - 애플리케이션 시작 로그 및 오류 확인

2. **URL 접속**
   - Render가 제공하는 URL로 접속 (예: `https://your-service.onrender.com`)
   - API 테스트: `https://your-service.onrender.com/actuator/health`

3. **WebSocket 연결 테스트**
   - WebSocket 엔드포인트 확인
   - 클라이언트에서 연결 테스트

## 🔄 자동 배포 설정

Render는 GitHub 저장소의 특정 브랜치에 푸시될 때 자동으로 재배포합니다:

1. **자동 배포 활성화**
   - Service 설정에서 `Auto-Deploy` 옵션 확인
   - 기본적으로 활성화되어 있음

2. **배포 트리거**
   ```bash
   git add .
   git commit -m "Update application"
   git push origin master
   ```
   - 푸시 후 자동으로 Render에서 빌드 및 배포 시작

## ⚠️ 주의사항

### Free Tier 제한사항
- 15분 동안 요청이 없으면 서비스가 sleep 상태로 전환
- Sleep 상태에서 깨어나는데 30초~1분 소요
- 월 750시간 무료 사용 가능

### 성능 최적화
- 프로덕션 환경에서는 유료 플랜 고려
- Database connection pool 크기 조정 (`DB_POOL_SIZE`)
- JVM 메모리 설정 최적화

### 보안
- 환경 변수로 민감한 정보 관리 (절대 코드에 하드코딩 금지)
- HTTPS는 Render에서 자동 제공
- 데이터베이스 연결은 SSL 사용 권장

## 🐛 문제 해결

### 빌드 실패
- Render 로그에서 오류 메시지 확인
- Gradle wrapper 실행 권한 확인: `chmod +x gradlew`
- 로컬에서 Docker 빌드 테스트

### 데이터베이스 연결 오류
- 환경 변수 확인 (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- Supabase IP 화이트리스트 확인 (필요한 경우)
- Connection pool 설정 검토

### Out of Memory 오류
- Free tier 메모리 제한 (512MB)
- JVM 메모리 설정 조정: `-XX:MaxRAMPercentage` 값 감소
- 유료 플랜으로 업그레이드 고려

## 📚 참고 자료

- [Deploying a Spring Boot Application on Render](https://medium.com/@pmanaktala/deploying-a-spring-boot-application-on-render-4e757dfe92ed)
- [How to host a Spring Boot application for free with Render](https://hostingtutorials.dev/blog/free-spring-boot-host-with-render)
- [Free Hosting Bliss: Deploying Your Spring Boot App on Render](https://medium.com/spring-boot/free-hosting-bliss-deploying-your-spring-boot-app-on-render-d0ebd9713b9d)
- [Running Java Spring Boot in Docker container on Web Services](https://community.render.com/t/running-java-spring-boot-in-docker-container-on-web-services/3232)
- [Deploying a Spring Boot Application with Docker Image on render.com](https://medium.com/@nithinsudarsan/deploying-a-spring-boot-application-with-docker-image-on-render-com-9a87f5ce5f72)
- [Deploying a Production-Ready Spring Boot on Render with Docker](https://medium.com/@chirag.rathod.dev/deploying-a-production-ready-spring-boot-on-render-with-docker-d9fa8f43dd80)

## 🎯 다음 단계

1. ✅ Docker 파일 생성 완료
2. ✅ .dockerignore 파일 생성 완료
3. 🔄 GitHub에 코드 푸시
4. 🚀 Render에서 서비스 생성 및 배포
5. 📊 배포 후 모니터링 및 테스트
6. 🔧 필요시 설정 최적화

---

**배포 성공을 기원합니다! 🎉**
