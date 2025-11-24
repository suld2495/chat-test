# Chat Backend API 테스트 가이드

## 🚀 애플리케이션 실행

```bash
./gradlew bootRun
```

서버 주소: `http://localhost:8080`
- Claude 연동: .env에 CLAUDE_API_KEY=를 설정해야 챗봇 응답이 동작합니다. (기본 timeout 10초, 모델 claude-3-5-sonnet-20240620)
- 토큰 한도: 채팅방별 claude.token-limit-per-room(기본 2000토큰)을 초과하면 챗봇이 더 이상 응답하지 않고 한도 초과 안내 시스템 메시지를 보냅니다.

---

## 📌 API 엔드포인트 목록

### 1. Health Check API

#### 기본 헬스체크
```http
GET http://localhost:8080/api/health
```

#### 데이터베이스 연결 확인
```http
GET http://localhost:8080/api/health/db
```

#### 테이블 확인
```http
GET http://localhost:8080/api/health/tables
```

---

### 2. User API

#### 사용자 생성
```http
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "nickname": "Tester1",
  "profileImageUrl": "https://example.com/profile.jpg"
}
```

- `id`와 `email` 없이 닉네임만 보내면 서버가 UUID와 `guest` 이메일을 자동 생성합니다.


#### 사용자 조회 (ID)
```http
GET http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000
```

#### 사용자 조회 (이메일)
```http
GET http://localhost:8080/api/users/email/tester1-1234abcd@chat.local
```

#### 사용자 검색 (닉네임)
```http
GET http://localhost:8080/api/users/search?keyword=사용자
```

#### 사용자 상태 업데이트
```http
PATCH http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000/status
Content-Type: application/json

{
  "status": "ONLINE"
}
```
**Status 옵션**: `ONLINE`, `OFFLINE`, `AWAY`

#### 프로필 업데이트
```http
PATCH http://localhost:8080/api/users/550e8400-e29b-41d4-a716-446655440000/profile
Content-Type: application/json

{
  "nickname": "새로운닉네임",
  "profileImageUrl": "https://example.com/new-profile.jpg"
}
```

---

### 3. ChatRoom API

#### 채팅방 생성 (사용자 + 전용 챗봇)
```http
POST http://localhost:8080/api/chatrooms
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

- `userId`는 대화 참여자인 사용자 ID 하나만 전달합니다.
- 서버가 `chat.bot-nickname` 설정을 사용해 **채팅방 전용 봇 계정**을 새로 만들고, 매 호출마다 새로운 챗봇 방을 생성합니다. 봇 이메일은 `bot-{UUID}@{도메인}`으로 자동 생성되며, 도메인은 `chat.bot-email`의 도메인 부분(`@` 이후, 기본 `chat.local`)을 사용합니다.

#### 채팅방 조회 (ID)
```http
GET http://localhost:8080/api/chatrooms/750e8400-e29b-41d4-a716-446655440002
```

#### 사용자의 모든 채팅방 조회
```http
GET http://localhost:8080/api/chatrooms/user/550e8400-e29b-41d4-a716-446655440000
```

#### 읽지 않은 메시지가 있는 채팅방
```http
GET http://localhost:8080/api/chatrooms/user/550e8400-e29b-41d4-a716-446655440000/unread
```

#### 전체 읽지 않은 메시지 수
```http
GET http://localhost:8080/api/chatrooms/user/550e8400-e29b-41d4-a716-446655440000/unread-count
```

#### 읽지 않은 메시지 초기화
```http
PATCH http://localhost:8080/api/chatrooms/750e8400-e29b-41d4-a716-446655440002/read
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 4. Message API

#### 메시지 전송
```http
POST http://localhost:8080/api/messages
Content-Type: application/json

{
  "chatRoomId": "750e8400-e29b-41d4-a716-446655440002",
  "senderId": "550e8400-e29b-41d4-a716-446655440000",
  "content": "안녕하세요!",
  "messageType": "TEXT"
}
```

#### 채팅방 메시지 목록 조회 (페이징)
```http
GET http://localhost:8080/api/messages/chatroom/750e8400-e29b-41d4-a716-446655440002?userId=550e8400-e29b-41d4-a716-446655440000&page=0&size=20
```

#### 특정 시간 이후 메시지 조회
```http
GET http://localhost:8080/api/messages/chatroom/750e8400-e29b-41d4-a716-446655440002/since?userId=550e8400-e29b-41d4-a716-446655440000&since=2024-11-23T18:00:00
```

#### 읽지 않은 메시지 조회
```http
GET http://localhost:8080/api/messages/chatroom/750e8400-e29b-41d4-a716-446655440002/unread?userId=550e8400-e29b-41d4-a716-446655440000
```

#### 읽지 않은 메시지 수 조회
```http
GET http://localhost:8080/api/messages/chatroom/750e8400-e29b-41d4-a716-446655440002/unread-count?userId=550e8400-e29b-41d4-a716-446655440000
```

#### 메시지 읽음 처리
```http
PATCH http://localhost:8080/api/messages/850e8400-e29b-41d4-a716-446655440003/read
Content-Type: application/json

{
  "userId": "650e8400-e29b-41d4-a716-446655440001"
}
```

#### 모든 메시지 읽음 처리
```http
PATCH http://localhost:8080/api/messages/chatroom/750e8400-e29b-41d4-a716-446655440002/read-all
Content-Type: application/json

{
  "userId": "650e8400-e29b-41d4-a716-446655440001"
}
```

#### 메시지 삭제
```http
DELETE http://localhost:8080/api/messages/850e8400-e29b-41d4-a716-446655440003?userId=550e8400-e29b-41d4-a716-446655440000
```

---

## 🧪 전체 플로우 테스트 시나리오

### 1단계: 사용자 생성
```bash
# 사용자 생성 (봇은 자동 처리)
POST /api/users
{
  "nickname": "Alice"
}
```

- 응답에 포함된 `id`(사용자 ID)를 다음 단계 요청에 사용하세요.
- 봇 계정은 `chat.bot-nickname` 설정을 기반으로 **채팅방마다 새로 생성**됩니다. `chat.bot-email`은 봇 이메일 도메인 결정에만 사용됩니다.



### 2단계: 채팅방 생성
```bash
POST /api/chatrooms
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}

# 응답에서 chatRoomId 확인
```

- 같은 사용자라도 매 호출마다 새로운 챗봇 방을 만들 수 있습니다.

### 3단계: 메시지 전송
```bash
# 사용자가 메시지 전송
POST /api/messages
{
  "chatRoomId": "[채팅방ID]",
  "senderId": "550e8400-e29b-41d4-a716-446655440000",
  "content": "안녕 챗봇!"
}
```

### 4단계: 메시지 조회
```bash
GET /api/messages/chatroom/[채팅방ID]?userId=550e8400-e29b-41d4-a716-446655440000
```

### 5단계: 읽음 처리
```bash
PATCH /api/messages/chatroom/[채팅방ID]/read-all
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 📊 응답 예시

### 성공 응답
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "alice-guest@chat.local",
  "nickname": "Alice",
  "profileImageUrl": null,
  "status": "OFFLINE",
  "lastSeenAt": null,
  "createdAt": "2024-11-23 18:30:00"
}
```

### 에러 응답
```json
{
  "status": 404,
  "code": "U001",
  "message": "사용자를 찾을 수 없습니다.",
  "details": "사용자를 찾을 수 없습니다: 550e8400-...",
  "path": "/api/users/550e8400-...",
  "timestamp": "2024-11-23 18:30:00"
}
```

---

## 🔧 Postman 사용 팁

1. **Environment 변수 설정**
   ```
   base_url: http://localhost:8080
   user_id: [사용자 생성 응답에서 복사]
   chatroom_id: [생성 후 입력]
   chat_bot_email: [선택] ai-bot@chat.local (도메인 부분만 사용)
   chat_bot_nickname: [선택] AI Bot
   ```

2. **자동으로 ID 저장**
   - Tests 탭에서 응답의 ID를 변수로 저장
    - 같은 방식으로 `user_id`도 사용자 생성 응답에서 저장할 수 있습니다.
   ```javascript
   pm.environment.set("chatroom_id", pm.response.json().id);
   ```

---

## ✅ 체크리스트

- [ ] 애플리케이션 정상 실행
- [ ] Health Check API 테스트
- [ ] 사용자 생성 및 조회
- [ ] 채팅방 생성
- [ ] 메시지 전송
- [ ] 메시지 조회 및 페이징
- [ ] 읽음 처리
- [ ] 에러 핸들링 확인


