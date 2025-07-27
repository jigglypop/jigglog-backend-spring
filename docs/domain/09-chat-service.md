# Chat Service (채팅 서비스)

## 1. 서비스 개요

### 1.1 목적
Chat Service는 시그나이트 플랫폼의 모든 실시간 메시징 기능을 관리합니다. 개인 쪽지, SIG 그룹 채팅, 시그장 전용 채팅 등 다양한 형태의 커뮤니케이션을 지원합니다.

### 1.2 주요 책임
- 실시간 메시징 (1:1, 그룹)
- 채팅방 생성 및 관리
- 메시지 이력 관리
- 파일 및 미디어 공유
- 읽음 상태 관리
- 메시지 검색

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: MongoDB (메시지), PostgreSQL (채팅방 메타데이터)
cache: Redis
realtime: WebSocket (STOMP)
storage: AWS S3 (파일 첨부)
search: Elasticsearch
```

## 3. 채팅 유형

### 3.1 개인 쪽지 (Direct Message)
- 1:1 개인 메시지
- 사용자 간 직접 소통
- 기본적으로 모든 멘사 회원 간 가능

### 3.2 SIG 채팅 (Group Chat)
- SIG 회원들 간 그룹 채팅
- SIG별 독립된 채팅방
- 회원만 참여 가능

### 3.3 시그장 채팅 (Leader Chat)
- 시그장들만 참여하는 전용 채팅
- 운영 관련 논의
- 경험 공유 및 조언

## 4. API 설계

### 4.1 채팅방 관리 API

#### GET /api/v1/chats/rooms
사용자 채팅방 목록 조회
```json
// Response
{
  "rooms": [
    {
      "id": "room-uuid",
      "type": "DIRECT",
      "name": "홍길동",
      "participants": [
        {
          "userId": "user-uuid",
          "name": "홍길동",
          "profileImage": "url",
          "lastSeenAt": "2024-11-15T10:30:00Z"
        }
      ],
      "lastMessage": {
        "id": "msg-uuid",
        "content": "안녕하세요",
        "senderId": "user-uuid",
        "sentAt": "2024-11-15T10:30:00Z",
        "type": "TEXT"
      },
      "unreadCount": 3,
      "updatedAt": "2024-11-15T10:30:00Z"
    },
    {
      "id": "room-uuid-2",
      "type": "SIG_GROUP",
      "name": "AI 연구 모임",
      "sigId": "sig-uuid",
      "participantCount": 25,
      "lastMessage": {
        "content": "다음 모임 일정 공유드립니다",
        "senderName": "김철수",
        "sentAt": "2024-11-15T09:15:00Z"
      },
      "unreadCount": 1
    }
  ]
}
```

#### POST /api/v1/chats/rooms
채팅방 생성
```json
// Request (Direct Message)
{
  "type": "DIRECT",
  "participantIds": ["target-user-uuid"]
}

// Request (SIG Group)
{
  "type": "SIG_GROUP",
  "sigId": "sig-uuid"
}

// Response
{
  "roomId": "room-uuid",
  "type": "DIRECT",
  "createdAt": "2024-11-15T10:00:00Z"
}
```

#### GET /api/v1/chats/rooms/{roomId}
채팅방 상세 정보
```json
// Response
{
  "id": "room-uuid",
  "type": "SIG_GROUP",
  "name": "AI 연구 모임",
  "description": "AI 관련 정보 공유 및 토론",
  "sigId": "sig-uuid",
  "participants": [
    {
      "userId": "user-uuid",
      "name": "홍길동",
      "role": "ADMIN",
      "joinedAt": "2024-10-01T00:00:00Z",
      "lastSeenAt": "2024-11-15T10:30:00Z"
    }
  ],
  "settings": {
    "allowFileUpload": true,
    "maxFileSize": 10485760,
    "muteAll": false,
    "adminOnly": false
  },
  "createdAt": "2024-10-01T00:00:00Z"
}
```

### 4.2 메시지 API

#### GET /api/v1/chats/rooms/{roomId}/messages
메시지 목록 조회
```json
// Query Parameters
?page=0&size=50&before=msg-uuid

// Response
{
  "messages": [
    {
      "id": "msg-uuid",
      "senderId": "user-uuid",
      "sender": {
        "name": "홍길동",
        "profileImage": "url"
      },
      "content": "안녕하세요!",
      "type": "TEXT",
      "sentAt": "2024-11-15T10:30:00Z",
      "editedAt": null,
      "readBy": [
        {
          "userId": "user-uuid-2",
          "readAt": "2024-11-15T10:31:00Z"
        }
      ],
      "reactions": [
        {
          "emoji": "👍",
          "users": ["user-uuid-2"],
          "count": 1
        }
      ]
    },
    {
      "id": "msg-uuid-2",
      "senderId": "user-uuid-2",
      "content": null,
      "type": "FILE",
      "fileInfo": {
        "fileName": "회의자료.pdf",
        "fileSize": 2048576,
        "mimeType": "application/pdf",
        "downloadUrl": "https://cdn.signight.com/files/..."
      },
      "sentAt": "2024-11-15T10:25:00Z"
    }
  ],
  "hasMore": true,
  "nextCursor": "msg-cursor"
}
```

#### POST /api/v1/chats/rooms/{roomId}/messages
메시지 전송
```json
// Request (Text Message)
{
  "type": "TEXT",
  "content": "안녕하세요!",
  "replyTo": "msg-uuid" // 답장인 경우
}

// Request (File Message)
{
  "type": "FILE",
  "fileId": "file-uuid",
  "content": "파일 설명" // 선택적
}

// Response
{
  "messageId": "msg-uuid",
  "sentAt": "2024-11-15T10:30:00Z",
  "status": "SENT"
}
```

#### PUT /api/v1/chats/messages/{messageId}
메시지 수정
```json
// Request
{
  "content": "수정된 메시지 내용"
}

// Response
{
  "success": true,
  "editedAt": "2024-11-15T10:35:00Z"
}
```

#### DELETE /api/v1/chats/messages/{messageId}
메시지 삭제
```json
// Response
{
  "success": true,
  "deletedAt": "2024-11-15T10:40:00Z"
}
```

### 4.3 실시간 API (WebSocket)

#### 메시지 수신
```javascript
// WebSocket 연결
ws://api.signight.com/ws/chat

// 메시지 수신
{
  "type": "MESSAGE_RECEIVED",
  "data": {
    "roomId": "room-uuid",
    "message": {
      "id": "msg-uuid",
      "senderId": "user-uuid",
      "content": "새 메시지",
      "sentAt": "2024-11-15T10:30:00Z"
    }
  }
}

// 읽음 상태 업데이트
{
  "type": "MESSAGE_READ",
  "data": {
    "roomId": "room-uuid",
    "messageId": "msg-uuid",
    "userId": "user-uuid",
    "readAt": "2024-11-15T10:31:00Z"
  }
}

// 타이핑 상태
{
  "type": "TYPING_STATUS",
  "data": {
    "roomId": "room-uuid",
    "userId": "user-uuid",
    "isTyping": true
  }
}
```

### 4.4 읽음 상태 API

#### POST /api/v1/chats/rooms/{roomId}/read
메시지 읽음 처리
```json
// Request
{
  "messageId": "msg-uuid" // 읽은 마지막 메시지
}

// Response
{
  "success": true,
  "readAt": "2024-11-15T10:31:00Z"
}
```

#### GET /api/v1/chats/rooms/{roomId}/unread-count
읽지 않은 메시지 수
```json
// Response
{
  "unreadCount": 5,
  "lastReadMessageId": "msg-uuid"
}
```

### 4.5 검색 API

#### GET /api/v1/chats/search
메시지 검색
```json
// Query Parameters
?query=회의&roomId=room-uuid&startDate=2024-11-01&endDate=2024-11-30

// Response
{
  "results": [
    {
      "message": {
        "id": "msg-uuid",
        "content": "내일 회의 일정을 알려드립니다",
        "senderId": "user-uuid",
        "sentAt": "2024-11-14T15:00:00Z"
      },
      "room": {
        "id": "room-uuid",
        "name": "AI 연구 모임"
      },
      "highlight": "내일 <em>회의</em> 일정을 알려드립니다"
    }
  ],
  "totalCount": 15
}
```

## 5. 데이터베이스 설계

### 5.1 PostgreSQL 테이블 (메타데이터)

#### chat_rooms
```sql
CREATE TABLE chat_rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL, -- DIRECT, SIG_GROUP, LEADER_CHAT
    name VARCHAR(200),
    description TEXT,
    sig_id UUID, -- SIG_GROUP인 경우
    creator_id UUID NOT NULL,
    settings JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_rooms_type ON chat_rooms(type);
CREATE INDEX idx_chat_rooms_sig_id ON chat_rooms(sig_id);
```

#### chat_participants
```sql
CREATE TABLE chat_participants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID REFERENCES chat_rooms(id),
    user_id UUID NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER', -- ADMIN, MEMBER
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    last_seen_at TIMESTAMP,
    is_muted BOOLEAN DEFAULT false,
    UNIQUE(room_id, user_id)
);

CREATE INDEX idx_participants_room_id ON chat_participants(room_id);
CREATE INDEX idx_participants_user_id ON chat_participants(user_id);
```

#### message_read_status
```sql
CREATE TABLE message_read_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id UUID REFERENCES chat_rooms(id),
    user_id UUID NOT NULL,
    last_read_message_id VARCHAR(24), -- MongoDB ObjectId
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(room_id, user_id)
);

CREATE INDEX idx_read_status_room_user ON message_read_status(room_id, user_id);
```

### 5.2 MongoDB 컬렉션 (메시지)

#### messages
```javascript
{
  "_id": ObjectId("..."),
  "roomId": "room-uuid",
  "senderId": "user-uuid",
  "type": "TEXT", // TEXT, FILE, IMAGE, SYSTEM
  "content": "메시지 내용",
  "fileInfo": {
    "fileName": "파일명.pdf",
    "fileSize": 1024,
    "mimeType": "application/pdf",
    "fileId": "file-uuid"
  },
  "replyTo": ObjectId("..."), // 답장 메시지 ID
  "reactions": [
    {
      "emoji": "👍",
      "users": ["user-uuid"],
      "count": 1
    }
  ],
  "editedAt": null,
  "deletedAt": null,
  "sentAt": ISODate("2024-11-15T10:30:00Z"),
  "isSystemMessage": false
}
```

## 6. 도메인 모델

### 6.1 채팅방 유형
```kotlin
enum class ChatRoomType {
    DIRECT,       // 개인 쪽지
    SIG_GROUP,    // SIG 그룹 채팅
    LEADER_CHAT   // 시그장 전용 채팅
}
```

### 6.2 메시지 유형
```kotlin
enum class MessageType {
    TEXT,        // 텍스트 메시지
    FILE,        // 파일 첨부
    IMAGE,       // 이미지
    SYSTEM,      // 시스템 메시지
    STICKER,     // 스티커
    LOCATION     // 위치 정보
}
```

### 6.3 참가자 역할
```kotlin
enum class ParticipantRole {
    ADMIN,   // 관리자 (채팅방 설정 변경 가능)
    MEMBER   // 일반 멤버
}
```

## 7. 실시간 통신

### 7.1 WebSocket 설정
```kotlin
@Configuration
@EnableWebSocketMessageBroker
class ChatWebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue")
        config.setApplicationDestinationPrefixes("/app")
        config.setUserDestinationPrefix("/user")
    }
    
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/chat")
            .setAllowedOrigins("*")
            .withSockJS()
    }
}
```

### 7.2 메시지 브로드캐스팅
```kotlin
@Service
class ChatMessageBroadcaster(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun broadcastMessage(roomId: String, message: ChatMessage) {
        messagingTemplate.convertAndSend(
            "/topic/chat/rooms/$roomId",
            MessageEvent(
                type = "MESSAGE_RECEIVED",
                data = message
            )
        )
    }
    
    fun sendTypingStatus(roomId: String, userId: String, isTyping: Boolean) {
        messagingTemplate.convertAndSend(
            "/topic/chat/rooms/$roomId/typing",
            TypingEvent(userId, isTyping)
        )
    }
    
    fun notifyMessageRead(roomId: String, messageId: String, userId: String) {
        messagingTemplate.convertAndSend(
            "/topic/chat/rooms/$roomId/read",
            ReadEvent(messageId, userId, Instant.now())
        )
    }
}
```

## 8. 파일 공유

### 8.1 파일 업로드 제한
```kotlin
class ChatFileValidator {
    companion object {
        val ALLOWED_MIME_TYPES = setOf(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
        )
        
        const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        const val MAX_IMAGE_SIZE = 5 * 1024 * 1024  // 5MB
    }
    
    fun validateFile(file: MultipartFile): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (file.size > MAX_FILE_SIZE) {
            errors.add("파일 크기는 ${MAX_FILE_SIZE / 1024 / 1024}MB를 초과할 수 없습니다")
        }
        
        if (!ALLOWED_MIME_TYPES.contains(file.contentType)) {
            errors.add("지원하지 않는 파일 형식입니다")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
```

## 9. 메시지 검색

### 9.1 Elasticsearch 인덱스
```json
{
  "mappings": {
    "properties": {
      "messageId": { "type": "keyword" },
      "roomId": { "type": "keyword" },
      "senderId": { "type": "keyword" },
      "content": { 
        "type": "text", 
        "analyzer": "korean",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "type": { "type": "keyword" },
      "fileName": { "type": "text", "analyzer": "korean" },
      "sentAt": { "type": "date" },
      "roomType": { "type": "keyword" }
    }
  }
}
```

### 9.2 검색 쿼리
```kotlin
@Service
class ChatSearchService(
    private val elasticsearchClient: ElasticsearchClient
) {
    fun searchMessages(query: ChatSearchQuery): SearchResult {
        val searchRequest = SearchRequest.Builder()
            .index("chat_messages")
            .query { q ->
                q.bool { b ->
                    // 키워드 검색
                    b.must { m ->
                        m.multiMatch { mm ->
                            mm.query(query.keyword)
                                .fields("content^2", "fileName")
                        }
                    }
                    
                    // 필터
                    query.roomId?.let {
                        b.filter { f -> f.term { t -> t.field("roomId").value(it) } }
                    }
                    
                    query.senderId?.let {
                        b.filter { f -> f.term { t -> t.field("senderId").value(it) } }
                    }
                    
                    query.startDate?.let { start ->
                        query.endDate?.let { end ->
                            b.filter { f ->
                                f.range { r ->
                                    r.field("sentAt")
                                        .gte(JsonData.of(start))
                                        .lte(JsonData.of(end))
                                }
                            }
                        }
                    }
                }
            }
            .sort { s -> s.field { f -> f.field("sentAt").order(SortOrder.Desc) } }
            .from(query.page * query.size)
            .size(query.size)
            .highlight { h ->
                h.fields("content") { f ->
                    f.preTags("<em>").postTags("</em>")
                }
            }
            .build()
            
        return elasticsearchClient.search(searchRequest, ChatMessage::class.java)
    }
}
```

## 10. 알림 연동

### 10.1 채팅 알림
```kotlin
@Service
class ChatNotificationService(
    private val notificationService: NotificationService
) {
    fun notifyNewMessage(message: ChatMessage, room: ChatRoom) {
        val participants = chatParticipantRepository.findByRoomIdAndNotUserId(
            room.id, 
            message.senderId
        )
        
        participants.forEach { participant ->
            // 사용자가 온라인이 아닌 경우에만 알림
            if (!isUserOnline(participant.userId)) {
                notificationService.send(
                    userId = participant.userId,
                    type = NotificationType.CHAT_MESSAGE,
                    channels = listOf(NotificationChannel.PUSH),
                    data = mapOf(
                        "roomName" to getRoomDisplayName(room, participant.userId),
                        "senderName" to getUserName(message.senderId),
                        "messagePreview" to truncateMessage(message.content)
                    )
                )
            }
        }
    }
}
```

## 11. 성능 최적화

### 11.1 메시지 페이징
```kotlin
@Service
class ChatMessageService {
    fun getMessages(roomId: String, pageable: MessagePageable): Page<ChatMessage> {
        // 커서 기반 페이징으로 성능 최적화
        val query = Query()
            .addCriteria(Criteria.where("roomId").`is`(roomId))
            
        pageable.before?.let {
            query.addCriteria(Criteria.where("_id").lt(ObjectId(it)))
        }
        
        query.with(Sort.by(Sort.Direction.DESC, "_id"))
            .limit(pageable.size)
            
        return mongoTemplate.find(query, ChatMessage::class.java)
            .let { messages -> 
                PageImpl(
                    messages.reversed(), // 시간순 정렬
                    pageable.toPageable(),
                    getTotalCount(roomId)
                )
            }
    }
}
```

### 11.2 읽음 상태 최적화
```kotlin
@Service
class ReadStatusService {
    // Redis를 사용한 읽음 상태 캐싱
    fun updateReadStatus(roomId: String, userId: String, messageId: String) {
        // DB 업데이트
        messageReadStatusRepository.upsert(roomId, userId, messageId)
        
        // Redis 캐시 업데이트
        redisTemplate.opsForHash<String, String>()
            .put("read_status:$roomId", userId, messageId)
            
        // 실시간 알림
        chatMessageBroadcaster.notifyMessageRead(roomId, messageId, userId)
    }
    
    fun getUnreadCount(roomId: String, userId: String): Int {
        val lastReadMessageId = redisTemplate.opsForHash<String, String>()
            .get("read_status:$roomId", userId)
            
        return messageRepository.countUnreadMessages(roomId, lastReadMessageId)
    }
}
```

## 12. 모니터링

### 12.1 비즈니스 메트릭
- 일일 활성 채팅방 수
- 평균 메시지 수
- 파일 공유 빈도
- 읽음률
- 응답 시간

### 12.2 기술 메트릭
- WebSocket 연결 수
- 메시지 전송 지연시간
- 검색 쿼리 성능
- Redis 캐시 히트율
- MongoDB 쿼리 성능 