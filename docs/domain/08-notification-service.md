# Notification Service (알림 서비스)

## 1. 서비스 개요

### 1.1 목적
Notification Service는 시그나이트 플랫폼의 모든 알림을 중앙에서 관리합니다. 이메일, 푸시 알림, SMS, 인앱 알림 등 다양한 채널을 통한 알림 발송을 담당합니다.

### 1.2 주요 책임
- 멀티채널 알림 발송
- 알림 템플릿 관리
- 사용자 알림 설정 관리
- 알림 이력 관리
- 알림 스케줄링
- 대량 발송 처리

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
cache: Redis
queue: RabbitMQ
email: AWS SES
sms: AWS SNS
push: Firebase Cloud Messaging
template: Thymeleaf
```

## 3. API 설계

### 3.1 알림 발송 API

#### POST /api/v1/notifications/send
즉시 알림 발송
```json
// Request
{
  "recipients": ["user-uuid-1", "user-uuid-2"],
  "type": "ACTIVITY_REMINDER",
  "channels": ["EMAIL", "PUSH"],
  "priority": "HIGH",
  "template": "activity-reminder",
  "data": {
    "activityName": "AI 스터디",
    "activityDate": "2024-11-15",
    "activityTime": "14:00",
    "location": "강남 스터디카페"
  },
  "options": {
    "respectUserPreferences": true,
    "deduplicationKey": "activity-123-reminder"
  }
}

// Response
{
  "notificationId": "notif-uuid",
  "status": "QUEUED",
  "estimatedSendTime": "2024-11-14T10:00:05Z",
  "recipientCount": 2
}
```

#### POST /api/v1/notifications/schedule
알림 예약
```json
// Request
{
  "recipients": ["user-uuid-1"],
  "type": "VOTE_DEADLINE",
  "sendAt": "2024-11-20T09:00:00Z",
  "template": "vote-deadline",
  "data": {
    "voteName": "12월 모임 장소 투표",
    "deadline": "2024-11-20T18:00:00Z"
  },
  "repeatRule": {
    "frequency": "DAILY",
    "until": "2024-11-20T00:00:00Z"
  }
}

// Response
{
  "scheduleId": "schedule-uuid",
  "status": "SCHEDULED",
  "nextSendTime": "2024-11-18T09:00:00Z"
}
```

#### POST /api/v1/notifications/broadcast
브로드캐스트 알림
```json
// Request
{
  "target": {
    "type": "SIG_MEMBERS",
    "sigIds": ["sig-uuid-1", "sig-uuid-2"]
  },
  "type": "ANNOUNCEMENT",
  "template": "general-announcement",
  "data": {
    "title": "멘사코리아 연말 행사 안내",
    "content": "12월 연말 행사가 예정되어 있습니다..."
  },
  "sendingOptions": {
    "batchSize": 100,
    "delayBetweenBatches": 1000,
    "maxSendRate": 50
  }
}

// Response
{
  "broadcastId": "broadcast-uuid",
  "estimatedRecipients": 500,
  "status": "PROCESSING",
  "trackingUrl": "/api/v1/notifications/broadcasts/broadcast-uuid/status"
}
```

### 3.2 알림 설정 API

#### GET /api/v1/notifications/preferences/{userId}
사용자 알림 설정 조회
```json
// Response
{
  "userId": "user-uuid",
  "globalSettings": {
    "enableEmail": true,
    "enablePush": true,
    "enableSms": false,
    "quietHours": {
      "enabled": true,
      "start": "22:00",
      "end": "08:00"
    }
  },
  "channelSettings": {
    "email": {
      "address": "user@example.com",
      "verified": true,
      "frequency": "IMMEDIATE"
    },
    "push": {
      "deviceTokens": ["token1", "token2"],
      "sound": true,
      "vibration": true
    }
  },
  "typeSettings": [
    {
      "type": "ACTIVITY_REMINDER",
      "enabled": true,
      "channels": ["EMAIL", "PUSH"],
      "timing": {
        "value": 24,
        "unit": "HOURS"
      }
    },
    {
      "type": "VOTE_RESULT",
      "enabled": true,
      "channels": ["PUSH"]
    }
  ]
}
```

#### PUT /api/v1/notifications/preferences/{userId}
사용자 알림 설정 수정
```json
// Request
{
  "globalSettings": {
    "enableSms": true
  },
  "typeSettings": [
    {
      "type": "ACTIVITY_REMINDER",
      "enabled": true,
      "channels": ["EMAIL", "PUSH", "SMS"],
      "timing": {
        "value": 48,
        "unit": "HOURS"
      }
    }
  ]
}
```

### 3.3 알림 이력 API

#### GET /api/v1/notifications/history
알림 이력 조회
```json
// Query Parameters
?userId=user-uuid&startDate=2024-11-01&endDate=2024-11-30&page=0&size=20

// Response
{
  "content": [
    {
      "id": "notif-uuid",
      "type": "ACTIVITY_REMINDER",
      "title": "내일 활동 알림",
      "content": "AI 스터디가 내일 14:00에 있습니다",
      "channels": [
        {
          "type": "EMAIL",
          "status": "DELIVERED",
          "sentAt": "2024-11-14T10:00:00Z"
        },
        {
          "type": "PUSH",
          "status": "DELIVERED",
          "sentAt": "2024-11-14T10:00:01Z",
          "readAt": "2024-11-14T12:30:00Z"
        }
      ],
      "createdAt": "2024-11-14T09:59:55Z"
    }
  ],
  "totalElements": 45,
  "totalPages": 3
}
```

#### POST /api/v1/notifications/{notificationId}/read
알림 읽음 처리
```json
// Response
{
  "success": true,
  "readAt": "2024-11-14T12:30:00Z"
}
```

### 3.4 템플릿 관리 API

#### GET /api/v1/notifications/templates
템플릿 목록 조회
```json
// Response
{
  "templates": [
    {
      "id": "activity-reminder",
      "name": "활동 알림",
      "type": "ACTIVITY_REMINDER",
      "channels": {
        "email": {
          "subject": "{{activityName}} 알림",
          "bodyHtml": "<p>안녕하세요, {{userName}}님...</p>",
          "bodyText": "안녕하세요, {{userName}}님..."
        },
        "push": {
          "title": "활동 알림",
          "body": "{{activityName}}이(가) {{activityTime}}에 시작됩니다"
        },
        "sms": {
          "message": "[멘사] {{activityName}} {{activityDate}} {{activityTime}} 장소: {{location}}"
        }
      },
      "variables": ["userName", "activityName", "activityDate", "activityTime", "location"]
    }
  ]
}
```

### 3.5 디바이스 관리 API

#### POST /api/v1/notifications/devices
디바이스 토큰 등록
```json
// Request
{
  "userId": "user-uuid",
  "deviceToken": "firebase-token",
  "deviceType": "ANDROID", // IOS, ANDROID, WEB
  "deviceInfo": {
    "model": "Galaxy S21",
    "osVersion": "Android 12",
    "appVersion": "1.2.0"
  }
}

// Response
{
  "deviceId": "device-uuid",
  "registered": true
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### notifications
```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    status VARCHAR(20) DEFAULT 'PENDING',
    template_id VARCHAR(50),
    data JSONB,
    recipient_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
```

#### notification_recipients
```sql
CREATE TABLE notification_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID REFERENCES notifications(id),
    user_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    channels JSONB, -- [{type: "EMAIL", status: "SENT", sentAt: "..."}]
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_recipients_notification_id ON notification_recipients(notification_id);
CREATE INDEX idx_recipients_user_id ON notification_recipients(user_id);
CREATE INDEX idx_recipients_status ON notification_recipients(status);
```

#### notification_preferences
```sql
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    global_email BOOLEAN DEFAULT true,
    global_push BOOLEAN DEFAULT true,
    global_sms BOOLEAN DEFAULT false,
    quiet_hours_enabled BOOLEAN DEFAULT false,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    email_frequency VARCHAR(20) DEFAULT 'IMMEDIATE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### notification_type_preferences
```sql
CREATE TABLE notification_type_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT true,
    channels VARCHAR(20)[] DEFAULT ARRAY['EMAIL', 'PUSH'],
    timing_value INTEGER,
    timing_unit VARCHAR(20),
    UNIQUE(user_id, notification_type)
);

CREATE INDEX idx_type_preferences_user_id ON notification_type_preferences(user_id);
```

#### notification_templates
```sql
CREATE TABLE notification_templates (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    email_subject VARCHAR(500),
    email_body_html TEXT,
    email_body_text TEXT,
    push_title VARCHAR(200),
    push_body VARCHAR(500),
    sms_message VARCHAR(160),
    variables VARCHAR(50)[],
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### device_tokens
```sql
CREATE TABLE device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_token VARCHAR(500) UNIQUE NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    device_info JSONB,
    is_active BOOLEAN DEFAULT true,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_device_token ON device_tokens(device_token);
```

#### notification_logs
```sql
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID,
    recipient_id UUID,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(50), -- AWS_SES, FCM, AWS_SNS
    provider_message_id VARCHAR(200),
    error_message TEXT,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    bounced_at TIMESTAMP,
    complained_at TIMESTAMP
);

CREATE INDEX idx_logs_notification_id ON notification_logs(notification_id);
CREATE INDEX idx_logs_sent_at ON notification_logs(sent_at);
```

## 5. 도메인 모델

### 5.1 알림 유형
```kotlin
enum class NotificationType {
    // 활동 관련
    ACTIVITY_CREATED,
    ACTIVITY_REMINDER,
    ACTIVITY_CANCELLED,
    ACTIVITY_UPDATED,
    REGISTRATION_CONFIRMED,
    WAITING_LIST_PROMOTION,
    
    // 투표 관련
    VOTE_CREATED,
    VOTE_DEADLINE,
    VOTE_RESULT,
    
    // 재정 관련
    FUND_APPLICATION_STATUS,
    EXPENSE_APPROVED,
    BUDGET_ALERT,
    
    // 커뮤니티 관련
    POST_REPLY,
    POST_MENTION,
    POST_LIKED,
    
    // 시스템 관련
    ANNOUNCEMENT,
    MAINTENANCE,
    SECURITY_ALERT
}
```

### 5.2 채널 유형
```kotlin
enum class NotificationChannel {
    EMAIL,
    PUSH,
    SMS,
    IN_APP
}
```

### 5.3 알림 상태
```kotlin
enum class NotificationStatus {
    PENDING,      // 대기중
    QUEUED,       // 큐에 추가됨
    SENDING,      // 발송중
    SENT,         // 발송됨
    DELIVERED,    // 전달됨
    FAILED,       // 실패
    CANCELLED     // 취소됨
}
```

## 6. 메시지 큐 처리

### 6.1 RabbitMQ 설정
```kotlin
@Configuration
class RabbitMQConfig {
    @Bean
    fun notificationQueue() = Queue("notifications", true)
    
    @Bean
    fun emailQueue() = Queue("notifications.email", true)
    
    @Bean
    fun pushQueue() = Queue("notifications.push", true)
    
    @Bean
    fun smsQueue() = Queue("notifications.sms", true)
    
    @Bean
    fun notificationExchange() = TopicExchange("notifications.exchange")
    
    @Bean
    fun emailBinding() = BindingBuilder
        .bind(emailQueue())
        .to(notificationExchange())
        .with("notification.email.*")
}
```

### 6.2 메시지 처리
```kotlin
@Component
class NotificationProcessor {
    @RabbitListener(queues = ["notifications"])
    fun processNotification(notification: NotificationMessage) {
        // 사용자 설정 확인
        val preferences = getPreferences(notification.userId)
        
        // 채널별 발송
        if (preferences.isEmailEnabled && notification.channels.contains(EMAIL)) {
            sendToQueue("notifications.email", notification)
        }
        
        if (preferences.isPushEnabled && notification.channels.contains(PUSH)) {
            sendToQueue("notifications.push", notification)
        }
        
        if (preferences.isSmsEnabled && notification.channels.contains(SMS)) {
            sendToQueue("notifications.sms", notification)
        }
    }
    
    @RabbitListener(queues = ["notifications.email"])
    fun processEmail(notification: NotificationMessage) {
        val template = templateService.getTemplate(notification.templateId)
        val rendered = templateEngine.render(template, notification.data)
        
        emailService.send(
            to = notification.recipient.email,
            subject = rendered.subject,
            body = rendered.body
        )
    }
}
```

## 7. 템플릿 엔진

### 7.1 템플릿 렌더링
```kotlin
@Service
class TemplateService(
    private val templateEngine: TemplateEngine
) {
    fun renderTemplate(templateId: String, data: Map<String, Any>): RenderedTemplate {
        val template = templateRepository.findById(templateId)
        
        return RenderedTemplate(
            emailSubject = renderString(template.emailSubject, data),
            emailBodyHtml = renderHtml(template.emailBodyHtml, data),
            emailBodyText = renderString(template.emailBodyText, data),
            pushTitle = renderString(template.pushTitle, data),
            pushBody = renderString(template.pushBody, data),
            smsMessage = renderString(template.smsMessage, data)
        )
    }
    
    private fun renderString(template: String?, data: Map<String, Any>): String? {
        return template?.let {
            val context = Context()
            context.setVariables(data)
            templateEngine.process(it, context)
        }
    }
}
```

## 8. 전송 서비스

### 8.1 이메일 발송
```kotlin
@Service
class EmailService(
    private val sesClient: AmazonSimpleEmailService
) {
    suspend fun sendEmail(
        to: String,
        subject: String,
        htmlBody: String?,
        textBody: String?
    ): SendResult {
        val request = SendEmailRequest()
            .withDestination(Destination().withToAddresses(to))
            .withMessage(Message()
                .withSubject(Content(subject))
                .withBody(Body()
                    .withHtml(htmlBody?.let { Content(it) })
                    .withText(textBody?.let { Content(it) })
                )
            )
            .withSource(senderEmail)
            
        return try {
            val result = sesClient.sendEmail(request)
            SendResult(
                success = true,
                messageId = result.messageId
            )
        } catch (e: Exception) {
            SendResult(
                success = false,
                error = e.message
            )
        }
    }
}
```

### 8.2 푸시 알림 발송
```kotlin
@Service
class PushNotificationService(
    private val firebaseMessaging: FirebaseMessaging
) {
    suspend fun sendPush(
        deviceToken: String,
        title: String,
        body: String,
        data: Map<String, String>
    ): SendResult {
        val message = Message.builder()
            .setToken(deviceToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .putAllData(data)
            .build()
            
        return try {
            val response = firebaseMessaging.send(message)
            SendResult(
                success = true,
                messageId = response
            )
        } catch (e: FirebaseMessagingException) {
            handlePushError(e, deviceToken)
            SendResult(
                success = false,
                error = e.message
            )
        }
    }
    
    private fun handlePushError(error: FirebaseMessagingException, token: String) {
        if (error.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
            // 토큰 무효화
            deviceTokenRepository.invalidateToken(token)
        }
    }
}
```

## 9. 대량 발송 처리

### 9.1 배치 처리
```kotlin
@Service
class BulkNotificationService {
    fun sendBulkNotification(
        recipientIds: List<String>,
        notification: NotificationRequest,
        options: BulkSendOptions
    ) {
        // 배치로 나누기
        recipientIds.chunked(options.batchSize).forEach { batch ->
            coroutineScope.launch {
                delay(options.delayBetweenBatches)
                
                batch.forEach { recipientId ->
                    rateLimiter.acquire() // Rate limiting
                    
                    sendNotification(recipientId, notification)
                }
            }
        }
    }
}
```

## 10. 알림 분석

### 10.1 메트릭 수집
```kotlin
@Service
class NotificationMetricsService {
    fun collectMetrics(period: Period): NotificationMetrics {
        return NotificationMetrics(
            totalSent = countTotalSent(period),
            byChannel = countByChannel(period),
            byType = countByType(period),
            deliveryRate = calculateDeliveryRate(period),
            openRate = calculateOpenRate(period),
            clickRate = calculateClickRate(period),
            unsubscribeRate = calculateUnsubscribeRate(period)
        )
    }
    
    fun getUserEngagement(userId: String): UserEngagement {
        val logs = notificationLogRepository.findByUserId(userId)
        
        return UserEngagement(
            totalReceived = logs.size,
            totalOpened = logs.count { it.openedAt != null },
            totalClicked = logs.count { it.clickedAt != null },
            preferredChannel = findPreferredChannel(logs),
            bestSendTime = calculateBestSendTime(logs)
        )
    }
}
```

## 11. 모니터링

### 11.1 비즈니스 메트릭
- 일일 발송량
- 채널별 전달률
- 오픈율/클릭률
- 구독 해지율
- 평균 응답 시간

### 11.2 기술 메트릭
- 큐 처리 속도
- API 응답 시간
- 제공자 API 성공률
- 템플릿 렌더링 시간
- 데이터베이스 쿼리 성능 