# Chat Backend

Spring Boot 기반의 실시간 채팅 백엔드 시스템

## 환경 설정

### 1. Supabase 연결 설정

#### IntelliJ IDEA에서 환경변수 설정

1. **Run Configuration 열기**
   - 상단 메뉴: `Run` > `Edit Configurations...`
   - 또는 상단 툴바의 재생 버튼 옆 드롭다운 > `Edit Configurations...`

2. **환경변수 추가**
   - `ChatApplication` 선택
   - `Environment variables` 필드 찾기
   - 오른쪽 폴더 아이콘(📁) 클릭
   - 다음 환경변수들을 추가:

   ```
   DATABASE_URL=jdbc:postgresql://db.your-project-ref.supabase.co:5432/postgres
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=your-database-password
   ```

3. **적용 및 저장**
   - `Apply` > `OK` 클릭

#### 또는 .env 파일 사용 (EnvFile 플러그인)

1. **플러그인 설치**
   - `File` > `Settings` > `Plugins`
   - "EnvFile" 검색 및 설치
   - IntelliJ 재시작

2. **Run Configuration 설정**
   - `Run` > `Edit Configurations...`
   - `EnvFile` 탭 클릭
   - `Enable EnvFile` 체크
   - `+` 버튼 클릭 > `.env` 파일 선택
   - `Apply` > `OK`

### 2. Supabase 정보 확인 방법

#### Supabase Dashboard
1. https://app.supabase.com 접속
2. 프로젝트 선택
3. `Settings` (⚙️) > `Database`

#### Connection String
```
URI 탭에서 확인:
postgresql://postgres:[YOUR-PASSWORD]@db.xxxxxxxxxxxxx.supabase.co:5432/postgres

Spring Boot용으로 변환:
jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres
```

#### Database Password
- 프로젝트 생성 시 설정한 비밀번호
- 분실 시: `Database` > `Reset database password`

## 실행 방법

### Gradle로 실행
```bash
# Windows
gradlew.bat bootRun

# Mac/Linux
./gradlew bootRun
```

### IntelliJ에서 실행
1. `ChatApplication.java` 파일 열기
2. `main` 메서드 옆 재생 버튼(▶️) 클릭
3. 또는 `Shift + F10`

## 연결 테스트

애플리케이션이 정상적으로 실행되면:
- 콘솔에 Hibernate 로그가 표시됩니다
- `Started ChatApplication in X.XXX seconds` 메시지 확인
- 포트 8080에서 서버 실행 중

### 데이터베이스 연결 확인
콘솔에서 다음과 같은 로그를 확인하세요:
```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

## 프로젝트 구조

```
chat-back/
├── src/main/java/com/chat/chat/
│   ├── ChatApplication.java           # 메인 애플리케이션
│   ├── common/                        # 공통 컴포넌트
│   │   ├── dto/                       # 공통 DTO
│   │   ├── exception/                 # 예외 처리
│   │   └── handler/                   # 전역 핸들러
│   └── domain/                        # 도메인 계층
│       ├── user/                      # 사용자 도메인
│       ├── chatroom/                  # 채팅방 도메인
│       └── message/                   # 메시지 도메인
└── src/main/resources/
    ├── application.properties         # 기본 설정
    └── logback-spring.xml            # 로깅 설정
```

## 기술 스택

- Java 21
- Spring Boot 4.0.0
- Spring Data JPA
- PostgreSQL (Supabase)
- WebSocket
- Lombok
- Gradle
