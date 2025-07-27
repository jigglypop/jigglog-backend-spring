# Activity Service (활동 관리 서비스)

## 1. 서비스 개요

### 1.1 목적
Activity Service는 시그나이트 플랫폼의 모든 SIG 활동을 관리합니다. 활동 일정 생성, 참가자 관리, 출석 체크, 활동 기록 등을 담당합니다.

### 1.2 주요 책임
- 활동 일정 생성 및 관리
- 참가자 신청 및 관리
- 출석 체크 시스템
- 활동 장소 관리
- 활동 사진 및 자료 관리
- 활동 보고서 생성

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
cache: Redis
storage: AWS S3 (사진, 자료)
maps: Google Maps API (장소 정보)
calendar: Google Calendar API (일정 동기화)
```

## 3. API 설계

### 3.1 활동 일정 API

#### POST /api/v1/activities
활동 생성
```json
// Request
{
  "sigId": "sig-uuid",
  "title": "11월 AI 스터디",
  "description": "ChatGPT API 활용법 학습",
  "activityType": "STUDY", // STUDY, SOCIAL, WORKSHOP, SEMINAR
  "startAt": "2024-11-15T14:00:00Z",
  "endAt": "2024-11-15T17:00:00Z",
  "location": {
    "name": "강남 스터디카페",
    "address": "서울시 강남구 테헤란로 123",
    "latitude": 37.123456,
    "longitude": 127.123456,
    "detailInfo": "3층 세미나룸 A"
  },
  "capacity": {
    "min": 5,
    "max": 20
  },
  "registrationDeadline": "2024-11-13T23:59:59Z",
  "requirements": {
    "memberOnly": true,
    "requireApproval": false,
    "prerequisites": ["노트북 지참"]
  },
  "fees": {
    "amount": 10000,
    "description": "장소 대여료"
  }
}

// Response
{
  "id": "activity-uuid",
  "code": "ACT-2024-001",
  "status": "SCHEDULED",
  "createdAt": "2024-10-01T10:00:00Z"
}
```

#### GET /api/v1/activities/{activityId}
활동 상세 조회
```json
// Response
{
  "id": "activity-uuid",
  "sig": {
    "id": "sig-uuid",
    "name": "AI 연구 모임"
  },
  "title": "11월 AI 스터디",
  "description": "ChatGPT API 활용법 학습",
  "activityType": "STUDY",
  "status": "SCHEDULED",
  "organizer": {
    "id": "user-uuid",
    "name": "홍길동",
    "profileImage": "url"
  },
  "startAt": "2024-11-15T14:00:00Z",
  "endAt": "2024-11-15T17:00:00Z",
  "location": {
    "name": "강남 스터디카페",
    "address": "서울시 강남구 테헤란로 123",
    "mapUrl": "https://maps.google.com/...",
    "transportInfo": "2호선 강남역 5번 출구"
  },
  "registration": {
    "currentCount": 12,
    "minCount": 5,
    "maxCount": 20,
    "deadline": "2024-11-13T23:59:59Z",
    "waitingCount": 3
  },
  "myRegistration": {
    "isRegistered": true,
    "status": "CONFIRMED",
    "registeredAt": "2024-11-01T10:00:00Z"
  },
  "materials": [
    {
      "id": "material-uuid",
      "name": "스터디 자료.pdf",
      "url": "download-url",
      "uploadedAt": "2024-11-10T10:00:00Z"
    }
  ]
}
```

#### PUT /api/v1/activities/{activityId}
활동 수정
```json
// Request
{
  "description": "ChatGPT API 활용법 및 프롬프트 엔지니어링",
  "capacity": {
    "max": 25
  },
  "location": {
    "detailInfo": "3층 세미나룸 B (변경됨)"
  }
}
```

### 3.2 참가 신청 API

#### POST /api/v1/activities/{activityId}/registrations
활동 참가 신청
```json
// Request
{
  "message": "참가 신청합니다",
  "requirements": {
    "hasLaptop": true,
    "dietaryRestrictions": "채식"
  }
}

// Response
{
  "registrationId": "reg-uuid",
  "status": "CONFIRMED", // CONFIRMED, WAITING, PENDING_APPROVAL
  "waitingNumber": null,
  "message": "참가 신청이 완료되었습니다"
}
```

#### DELETE /api/v1/activities/{activityId}/registrations
참가 신청 취소
```json
// Request
{
  "reason": "일정 변경으로 인한 취소"
}

// Response
{
  "message": "참가 신청이 취소되었습니다",
  "refundInfo": {
    "amount": 10000,
    "expectedDate": "2024-11-10"
  }
}
```

#### GET /api/v1/activities/{activityId}/registrations
참가자 목록 조회 (주최자용)
```json
// Response
{
  "confirmed": [
    {
      "userId": "user-uuid",
      "name": "홍길동",
      "profileImage": "url",
      "registeredAt": "2024-11-01T10:00:00Z",
      "status": "CONFIRMED",
      "attendance": null
    }
  ],
  "waiting": [
    {
      "userId": "user-uuid-2",
      "name": "김철수",
      "waitingNumber": 1,
      "registeredAt": "2024-11-10T15:00:00Z"
    }
  ],
  "summary": {
    "confirmedCount": 20,
    "waitingCount": 3,
    "cancelledCount": 2
  }
}
```

### 3.3 출석 체크 API

#### POST /api/v1/activities/{activityId}/attendance/check-in
체크인 (QR 코드)
```json
// Request
{
  "qrCode": "encrypted-qr-data",
  "location": {
    "latitude": 37.123456,
    "longitude": 127.123456
  }
}

// Response
{
  "success": true,
  "checkInTime": "2024-11-15T13:55:00Z",
  "message": "체크인이 완료되었습니다"
}
```

#### POST /api/v1/activities/{activityId}/attendance/bulk
일괄 출석 체크 (주최자용)
```json
// Request
{
  "attendees": [
    {
      "userId": "user-uuid-1",
      "status": "PRESENT"
    },
    {
      "userId": "user-uuid-2",
      "status": "ABSENT"
    },
    {
      "userId": "user-uuid-3",
      "status": "LATE"
    }
  ]
}

// Response
{
  "processed": 3,
  "message": "출석 체크가 완료되었습니다"
}
```

#### GET /api/v1/activities/{activityId}/attendance
출석 현황 조회
```json
// Response
{
  "statistics": {
    "total": 20,
    "present": 17,
    "late": 2,
    "absent": 1,
    "attendanceRate": 85.0
  },
  "attendees": [
    {
      "userId": "user-uuid",
      "name": "홍길동",
      "status": "PRESENT",
      "checkInTime": "2024-11-15T13:55:00Z"
    }
  ]
}
```

### 3.4 활동 후 관리 API

#### POST /api/v1/activities/{activityId}/photos
활동 사진 업로드
```
// Request
Content-Type: multipart/form-data
files: [photo1.jpg, photo2.jpg]
captions: ["그룹 사진", "스터디 진행 모습"]

// Response
{
  "uploadedPhotos": [
    {
      "id": "photo-uuid-1",
      "url": "https://cdn.signight.com/photos/...",
      "thumbnailUrl": "https://cdn.signight.com/thumbs/...",
      "caption": "그룹 사진"
    }
  ]
}
```

#### POST /api/v1/activities/{activityId}/report
활동 보고서 작성
```json
// Request
{
  "summary": "11월 AI 스터디 성공적으로 진행",
  "content": "ChatGPT API 활용법에 대해 학습하고...",
  "achievements": [
    "API 기본 사용법 학습",
    "프롬프트 엔지니어링 실습"
  ],
  "feedback": {
    "positive": ["실습 위주 진행이 좋았음"],
    "improvement": ["시간이 부족했음"]
  },
  "nextPlan": "다음 모임에서는 고급 기능 학습 예정"
}

// Response
{
  "reportId": "report-uuid",
  "status": "SUBMITTED",
  "submittedAt": "2024-11-15T20:00:00Z"
}
```

### 3.5 활동 검색 API

#### GET /api/v1/activities/search
활동 검색
```json
// Query Parameters
?sigId=sig-uuid&type=STUDY&startDate=2024-11-01&endDate=2024-11-30&status=SCHEDULED

// Response
{
  "content": [
    {
      "id": "activity-uuid",
      "sig": {
        "name": "AI 연구 모임"
      },
      "title": "11월 AI 스터디",
      "activityType": "STUDY",
      "startAt": "2024-11-15T14:00:00Z",
      "location": {
        "name": "강남 스터디카페"
      },
      "registration": {
        "currentCount": 12,
        "maxCount": 20
      }
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

#### GET /api/v1/activities/calendar
캘린더 뷰용 활동 목록
```json
// Query Parameters
?year=2024&month=11&sigIds=sig-uuid-1,sig-uuid-2

// Response
{
  "activities": [
    {
      "id": "activity-uuid",
      "title": "AI 스터디",
      "sigName": "AI 연구 모임",
      "date": "2024-11-15",
      "startTime": "14:00",
      "endTime": "17:00",
      "color": "#FF5733",
      "isRegistered": true
    }
  ],
  "holidays": [
    {
      "date": "2024-11-09",
      "name": "한글날 대체휴일"
    }
  ]
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### activities
```sql
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    sig_id UUID NOT NULL,
    organizer_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    activity_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    registration_deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT
);

CREATE INDEX idx_activities_sig_id ON activities(sig_id);
CREATE INDEX idx_activities_start_at ON activities(start_at);
CREATE INDEX idx_activities_status ON activities(status);
```

#### activity_locations
```sql
CREATE TABLE activity_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id) UNIQUE,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    detail_info TEXT,
    transport_info TEXT,
    parking_info TEXT,
    map_url VARCHAR(500)
);
```

#### activity_capacities
```sql
CREATE TABLE activity_capacities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id) UNIQUE,
    min_participants INTEGER DEFAULT 1,
    max_participants INTEGER NOT NULL,
    current_participants INTEGER DEFAULT 0,
    waiting_participants INTEGER DEFAULT 0,
    member_only BOOLEAN DEFAULT false,
    require_approval BOOLEAN DEFAULT false
);
```

#### activity_registrations
```sql
CREATE TABLE activity_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id),
    user_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    waiting_number INTEGER,
    message TEXT,
    requirements JSONB,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    UNIQUE(activity_id, user_id)
);

CREATE INDEX idx_registrations_activity_id ON activity_registrations(activity_id);
CREATE INDEX idx_registrations_user_id ON activity_registrations(user_id);
CREATE INDEX idx_registrations_status ON activity_registrations(status);
```

#### activity_attendance
```sql
CREATE TABLE activity_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id),
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL, -- PRESENT, LATE, ABSENT
    check_in_time TIMESTAMP,
    check_in_location POINT,
    check_in_method VARCHAR(20), -- QR, MANUAL, AUTO
    checked_by UUID,
    notes TEXT,
    UNIQUE(activity_id, user_id)
);

CREATE INDEX idx_attendance_activity_id ON activity_attendance(activity_id);
```

#### activity_photos
```sql
CREATE TABLE activity_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id),
    uploaded_by UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    caption TEXT,
    display_order INTEGER DEFAULT 0,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_photos_activity_id ON activity_photos(activity_id);
```

#### activity_materials
```sql
CREATE TABLE activity_materials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id),
    uploaded_by UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size INTEGER,
    file_type VARCHAR(100),
    description TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_materials_activity_id ON activity_materials(activity_id);
```

#### activity_reports
```sql
CREATE TABLE activity_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id UUID REFERENCES activities(id) UNIQUE,
    author_id UUID NOT NULL,
    summary VARCHAR(500),
    content TEXT,
    achievements JSONB,
    feedback JSONB,
    next_plan TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by UUID
);
```

## 5. 도메인 모델

### 5.1 활동 유형
```kotlin
enum class ActivityType {
    STUDY,      // 스터디
    SOCIAL,     // 친목 모임
    WORKSHOP,   // 워크샵
    SEMINAR,    // 세미나
    VOLUNTEER,  // 봉사 활동
    SPORTS,     // 스포츠
    CULTURAL    // 문화 활동
}
```

### 5.2 활동 상태
```kotlin
enum class ActivityStatus {
    DRAFT,       // 초안
    SCHEDULED,   // 예정됨
    ONGOING,     // 진행중
    COMPLETED,   // 완료됨
    CANCELLED    // 취소됨
}
```

### 5.3 참가 상태
```kotlin
enum class RegistrationStatus {
    PENDING,           // 대기중
    CONFIRMED,         // 확정
    WAITING,           // 대기자
    CANCELLED,         // 취소
    PENDING_APPROVAL   // 승인 대기
}
```

## 6. QR 코드 체크인 시스템

### 6.1 QR 코드 생성
```kotlin
@Service
class QRCodeService {
    fun generateActivityQR(activityId: String): QRCodeData {
        val payload = QRPayload(
            activityId = activityId,
            timestamp = Instant.now(),
            nonce = UUID.randomUUID().toString()
        )
        
        val encrypted = encrypt(payload)
        val qrCode = QRCodeGenerator.generate(encrypted)
        
        return QRCodeData(
            qrImage = qrCode,
            validUntil = Instant.now().plus(30, ChronoUnit.MINUTES)
        )
    }
    
    fun validateCheckIn(qrData: String, location: Location?): CheckInResult {
        val payload = decrypt(qrData)
        
        // 시간 검증
        if (payload.timestamp.isBefore(Instant.now().minus(30, ChronoUnit.MINUTES))) {
            return CheckInResult(false, "QR 코드가 만료되었습니다")
        }
        
        // 위치 검증 (선택적)
        location?.let {
            val activityLocation = getActivityLocation(payload.activityId)
            val distance = calculateDistance(location, activityLocation)
            if (distance > 100) { // 100미터 이상
                return CheckInResult(false, "활동 장소에서 너무 멀리 있습니다")
            }
        }
        
        return CheckInResult(true, "체크인 가능")
    }
}
```

## 7. 일정 동기화

### 7.1 Google Calendar 연동
```kotlin
@Service
class CalendarSyncService(
    private val googleCalendarClient: GoogleCalendarClient
) {
    suspend fun syncToGoogleCalendar(activity: Activity, userEmail: String) {
        val event = GoogleCalendarEvent(
            summary = "[${activity.sig.name}] ${activity.title}",
            description = activity.description,
            start = EventDateTime().setDateTime(activity.startAt),
            end = EventDateTime().setDateTime(activity.endAt),
            location = activity.location.fullAddress,
            reminders = listOf(
                EventReminder().setMethod("email").setMinutes(1440), // 1일 전
                EventReminder().setMethod("popup").setMinutes(60)    // 1시간 전
            )
        )
        
        googleCalendarClient.createEvent(userEmail, event)
    }
    
    suspend fun syncFromGoogleCalendar(sigId: String, calendarId: String) {
        val events = googleCalendarClient.getEvents(calendarId)
        
        events.forEach { event ->
            if (shouldImport(event)) {
                createActivityFromCalendarEvent(sigId, event)
            }
        }
    }
}
```

## 8. 활동 추천 시스템

### 8.1 추천 알고리즘
```kotlin
@Service
class ActivityRecommendationService {
    fun recommendActivities(userId: String): List<ActivityRecommendation> {
        // 사용자 선호도 분석
        val userPreferences = analyzeUserPreferences(userId)
        
        // 과거 참여 패턴 분석
        val participationPattern = analyzeParticipationPattern(userId)
        
        // 추천 점수 계산
        val recommendations = activityRepository.findUpcoming()
            .map { activity ->
                val score = calculateRecommendationScore(
                    activity,
                    userPreferences,
                    participationPattern
                )
                ActivityRecommendation(activity, score)
            }
            .filter { it.score > 0.5 }
            .sortedByDescending { it.score }
            .take(10)
            
        return recommendations
    }
    
    private fun calculateRecommendationScore(
        activity: Activity,
        preferences: UserPreferences,
        pattern: ParticipationPattern
    ): Double {
        val typeScore = preferences.activityTypes[activity.type] ?: 0.0
        val timeScore = calculateTimeScore(activity.startAt, pattern.preferredTimes)
        val locationScore = calculateLocationScore(activity.location, preferences.locations)
        val socialScore = calculateSocialScore(activity.sigId, pattern.frequentSigs)
        
        return (typeScore * 0.3 + timeScore * 0.2 + 
                locationScore * 0.2 + socialScore * 0.3)
    }
}
```

## 9. 통계 및 분석

### 9.1 활동 통계
```kotlin
@Service
class ActivityAnalyticsService {
    fun generateActivityStats(sigId: String, period: Period): ActivityStatistics {
        val activities = activityRepository.findBySigAndPeriod(sigId, period)
        
        return ActivityStatistics(
            totalActivities = activities.size,
            byType = activities.groupBy { it.type }.mapValues { it.value.size },
            averageAttendance = calculateAverageAttendance(activities),
            popularDays = findPopularDays(activities),
            popularLocations = findPopularLocations(activities),
            memberEngagement = calculateMemberEngagement(activities),
            growthRate = calculateGrowthRate(activities, period)
        )
    }
    
    fun generateMemberActivityReport(userId: String, year: Int): MemberActivityReport {
        val registrations = registrationRepository.findByUserAndYear(userId, year)
        
        return MemberActivityReport(
            totalActivities = registrations.size,
            attendedActivities = registrations.count { it.attended },
            attendanceRate = calculateAttendanceRate(registrations),
            favoriteTypes = findFavoriteTypes(registrations),
            totalHours = calculateTotalHours(registrations),
            monthlyDistribution = groupByMonth(registrations)
        )
    }
}
```

## 10. 알림 시스템

### 10.1 활동 알림
```kotlin
@Component
class ActivityNotificationService {
    // 활동 1일 전 알림
    @Scheduled(cron = "0 0 10 * * *")
    fun sendDayBeforeReminders() {
        val tomorrow = LocalDate.now().plusDays(1)
        val activities = activityRepository.findByDate(tomorrow)
        
        activities.forEach { activity ->
            val participants = registrationRepository.findConfirmedByActivity(activity.id)
            
            participants.forEach { participant ->
                notificationService.send(
                    userId = participant.userId,
                    title = "내일 활동 알림",
                    message = "${activity.title}이(가) 내일 ${activity.startAt.toLocalTime()}에 있습니다",
                    type = NotificationType.ACTIVITY_REMINDER
                )
            }
        }
    }
    
    // 대기자 승급 알림
    fun notifyWaitingListPromotion(registration: Registration) {
        notificationService.send(
            userId = registration.userId,
            title = "대기자 승급 알림",
            message = "${registration.activity.title} 참가가 확정되었습니다",
            type = NotificationType.WAITING_LIST_PROMOTION
        )
    }
}
```

## 11. 모니터링

### 11.1 비즈니스 메트릭
- 월간 활동 수
- 평균 참가율
- 인기 활동 유형
- 활동별 만족도
- 취소율

### 11.2 기술 메트릭
- API 응답 시간
- QR 체크인 성공률
- 파일 업로드 성능
- 캘린더 동기화 성공률 