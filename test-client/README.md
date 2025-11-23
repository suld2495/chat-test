# WebSocket 테스트 클라이언트 사용 가이드

## 개요
이 HTML 파일은 Spring Boot WebSocket 채팅 서버를 테스트하기 위한 클라이언트입니다.

## 사용 방법

### 1. 서버 실행
먼저 Spring Boot 애플리케이션을 실행하세요:
```bash
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 2. 테스트 클라이언트 열기
`websocket-test.html` 파일을 웹 브라우저에서 직접 열거나, 다음 명령어로 간단한 HTTP 서버를 실행할 수 있습니다:

```bash
# Python 3를 사용하는 경우
cd test-client
python -m http.server 8000

# 브라우저에서 http://localhost:8000/websocket-test.html 접속
```

### 3. 연결 설정

#### 기본 설정값
- **WebSocket URL**: `http://localhost:8080/ws-chat`
- **User ID**: 자동 생성된 UUID (수정 가능)
- **Nickname**: 사용자 닉네임 입력
- **Chat Room ID**: 자동 생성된 UUID (수정 가능)

#### 연결 순서
1. User ID와 Nickname을 입력 (또는 자동 생성된 값 사용)
2. **Connect** 버튼 클릭하여 WebSocket 연결
3. Chat Room ID 입력 (또는 자동 생성된 값 사용)
4. **Join Room** 버튼 클릭하여 채팅방 입장

### 4. 채팅 기능 테스트

#### 메시지 전송
- 메시지 입력 창에 텍스트 입력
- **Send** 버튼 클릭 또는 Enter 키 입력
- 전송된 메시지가 채팅 화면에 표시됨

#### 타이핑 표시
- 메시지 입력 시 자동으로 타이핑 상태 전송
- 2초간 입력이 없으면 타이핑 중지 상태로 변경
- 다른 사용자의 타이핑 상태가 하단에 표시됨

#### 읽음 확인
- **Mark as Read** 버튼 클릭
- 채팅방의 모든 메시지를 읽음 처리
- READ 이벤트가 다른 사용자에게 전송됨

#### 입장/퇴장 메시지
- Join Room 시 자동으로 입장 메시지 전송
- Disconnect 또는 다른 방 입장 시 퇴장 메시지 전송
- 시스템 메시지로 표시됨 (노란색 배경)

### 5. 다중 사용자 테스트

여러 사용자 간의 채팅을 테스트하려면:

1. **브라우저 창을 여러 개 열기**
   - 같은 브라우저에서 새 탭 열기
   - 또는 다른 브라우저 사용 (Chrome, Firefox 등)

2. **각 창에서 다른 사용자로 설정**
   - 창 1: User ID = UUID1, Nickname = "사용자1"
   - 창 2: User ID = UUID2, Nickname = "사용자2"

3. **같은 채팅방 ID 사용**
   - 두 창 모두 같은 Chat Room ID 입력
   - 각각 Connect → Join Room

4. **실시간 채팅 테스트**
   - 한쪽에서 메시지 전송 → 다른 쪽에 즉시 표시
   - 타이핑 표시 확인
   - 읽음 확인 기능 테스트

## 주요 기능

### WebSocket 이벤트 타입

1. **CHAT** - 일반 채팅 메시지
   - 데이터베이스에 저장됨
   - 발신자 정보, 내용, 시간 포함

2. **JOIN** - 사용자 입장
   - "{닉네임}님이 입장했습니다." 메시지 전송
   - 시스템 메시지로 표시

3. **LEAVE** - 사용자 퇴장
   - "{닉네임}님이 퇴장했습니다." 메시지 전송
   - 시스템 메시지로 표시

4. **READ** - 읽음 확인
   - 모든 메시지를 읽음 처리
   - UI에는 표시되지 않고 로그에만 기록

5. **TYPING** - 타이핑 표시
   - 별도 엔드포인트 사용 (`/app/typing/{chatRoomId}`)
   - 실시간으로 타이핑 상태 전송

### STOMP 엔드포인트

- **연결**: `ws://localhost:8080/ws-chat` (SockJS)
- **메시지 전송**: `/app/chat/{chatRoomId}`
- **타이핑 알림**: `/app/typing/{chatRoomId}`
- **메시지 구독**: `/topic/chatroom/{chatRoomId}`
- **타이핑 구독**: `/topic/chatroom/{chatRoomId}/typing`

### UI 기능

- **연결 상태 표시**: 상단에 연결/해제 상태 표시
- **메시지 구분**: 내 메시지(초록), 상대 메시지(흰색), 시스템 메시지(노랑)
- **타이핑 표시**: 하단에 타이핑 중인 사용자 표시
- **이벤트 로그**: 하단에 모든 WebSocket 이벤트 기록
- **자동 스크롤**: 새 메시지 도착 시 자동으로 스크롤

## 문제 해결

### 연결 실패
- Spring Boot 서버가 실행 중인지 확인
- WebSocket URL이 올바른지 확인 (`http://localhost:8080/ws-chat`)
- 브라우저 콘솔(F12)에서 에러 확인

### 메시지가 전송되지 않음
- WebSocket이 연결되어 있는지 확인 (상태가 "Connected"인지)
- 채팅방에 입장했는지 확인 (Join Room 완료)
- 네트워크 탭에서 WebSocket 프레임 확인

### CORS 오류
- `WebSocketConfig.java`에서 `.setAllowedOriginPatterns("*")` 설정 확인
- 필요시 특정 origin만 허용하도록 변경

### 타이핑 표시가 안됨
- 타이핑 구독이 정상적으로 되었는지 로그 확인
- 다른 사용자의 ID와 현재 사용자 ID가 다른지 확인
- 2초간 입력이 없으면 자동으로 타이핑 중지됨

## 추가 테스트 시나리오

### 시나리오 1: 기본 채팅 흐름
1. 사용자A 연결 → 방 입장
2. 사용자B 연결 → 같은 방 입장
3. A가 메시지 전송 → B에게 즉시 표시
4. B가 답장 전송 → A에게 즉시 표시
5. A가 읽음 확인 → B에게 READ 이벤트 전송

### 시나리오 2: 타이핑 표시
1. 사용자A와 B 모두 같은 방에 입장
2. A가 메시지 입력 시작 → B 화면에 "사용자A is typing..." 표시
3. A가 입력 중지 → 2초 후 타이핑 표시 사라짐
4. A가 메시지 전송 → 타이핑 표시 즉시 사라짐

### 시나리오 3: 입장/퇴장
1. 사용자A 방 입장 → "사용자A님이 입장했습니다." 표시
2. 사용자B 방 입장 → 두 사용자 모두에게 입장 메시지 표시
3. 사용자A 연결 해제 → B에게 "사용자A님이 퇴장했습니다." 표시

### 시나리오 4: 읽음 확인
1. 사용자A가 여러 메시지 전송
2. 사용자B가 "Mark as Read" 클릭
3. 서버에서 해당 채팅방의 모든 메시지를 읽음 처리
4. A의 로그에 READ 이벤트 기록

## 참고사항

- UUID는 페이지 로드 시 자동으로 생성되지만 수동으로 변경 가능
- 실제 서비스에서는 Supabase Auth에서 제공하는 UUID를 사용해야 함
- 이 테스트 클라이언트는 개발 목적으로만 사용
- 프로덕션에서는 적절한 인증/인가 메커니즘 필요

## REST API와의 통합

WebSocket 채팅 전에 REST API로 준비 작업:

1. **사용자 생성**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@test.com",
    "nickname": "사용자1"
  }'
```

2. **채팅방 생성**
```bash
curl -X POST http://localhost:8080/api/chatrooms \
  -H "Content-Type: application/json" \
  -d '{
    "user1Id": "{생성된 사용자1 ID}",
    "user2Id": "{생성된 사용자2 ID}"
  }'
```

3. **메시지 히스토리 조회**
```bash
curl http://localhost:8080/api/messages/chatroom/{chatRoomId}?page=0&size=50
```

4. **WebSocket으로 실시간 채팅**
   - 이 테스트 클라이언트 사용
