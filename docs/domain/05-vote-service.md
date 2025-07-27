# Vote Service (투표 서비스)

## 1. 서비스 개요

### 1.1 목적
Vote Service는 시그나이트 플랫폼의 모든 투표 기능을 관리합니다. 다양한 형태의 투표를 지원하며, 실시간 집계와 익명 투표를 보장합니다.

### 1.2 주요 책임
- 투표 생성 및 관리
- 투표 참여 및 집계
- 익명 투표 보장
- 실시간 결과 업데이트
- 투표 통계 제공
- 투표 검증 및 보안

## 2. 기술 스택

```yaml
language: Kotlin
framework: Spring Boot 3.x
build: Gradle (Kotlin DSL)
database: PostgreSQL
cache: Redis
realtime: WebSocket (STOMP)
encryption: AES-256 (익명 투표)
```

## 3. API 설계

### 3.1 투표 생성 API

#### POST /api/v1/votes
투표 생성
```json
// Request
{
  "sigId": "sig-uuid",
  "title": "다음 모임 장소를 투표해주세요",
  "description": "11월 정기 모임 장소를 결정합니다",
  "voteType": "SINGLE_CHOICE", // SINGLE_CHOICE, MULTIPLE_CHOICE, RANKING, YES_NO
  "options": [
    {
      "text": "강남역 스터디카페",
      "description": "지하철 접근성 좋음"
    },
    {
      "text": "판교 테크노밸리",
      "description": "주차 편리"
    }
  ],
  "settings": {
    "isAnonymous": true,
    "allowOptionAdd": false,
    "maxSelectCount": 1,
    "showRealtimeResult": true,
    "allowRevote": false
  },
  "startAt": "2024-11-01T09:00:00Z",
  "endAt": "2024-11-07T18:00:00Z"
}

// Response
{
  "id": "vote-uuid",
  "code": "VOTE-2024-001",
  "status": "SCHEDULED",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /api/v1/votes/{voteId}
투표 상세 조회
```json
// Response
{
  "id": "vote-uuid",
  "sigId": "sig-uuid",
  "sig": {
    "id": "sig-uuid",
    "name": "AI 연구 모임"
  },
  "creator": {
    "id": "user-uuid",
    "name": "홍길동"
  },
  "title": "다음 모임 장소를 투표해주세요",
  "description": "11월 정기 모임 장소를 결정합니다",
  "voteType": "SINGLE_CHOICE",
  "options": [
    {
      "id": "option-uuid-1",
      "text": "강남역 스터디카페",
      "description": "지하철 접근성 좋음",
      "voteCount": 15,
      "percentage": 60.0
    },
    {
      "id": "option-uuid-2",
      "text": "판교 테크노밸리",
      "description": "주차 편리",
      "voteCount": 10,
      "percentage": 40.0
    }
  ],
  "settings": {
    "isAnonymous": true,
    "showRealtimeResult": true
  },
  "stats": {
    "totalVoters": 25,
    "totalEligible": 45,
    "participationRate": 55.6
  },
  "myVote": {
    "hasVoted": true,
    "votedAt": "2024-11-02T10:30:00Z",
    "selectedOptions": ["option-uuid-1"]
  },
  "status": "IN_PROGRESS",
  "startAt": "2024-11-01T09:00:00Z",
  "endAt": "2024-11-07T18:00:00Z"
}
```

### 3.2 투표 참여 API

#### POST /api/v1/votes/{voteId}/cast
투표하기
```json
// Request
{
  "selectedOptions": ["option-uuid-1"], // 단일 선택
  // or
  "selectedOptions": ["option-uuid-1", "option-uuid-3"], // 복수 선택
  // or
  "rankedOptions": [ // 순위 투표
    {"optionId": "option-uuid-2", "rank": 1},
    {"optionId": "option-uuid-1", "rank": 2},
    {"optionId": "option-uuid-3", "rank": 3}
  ]
}

// Response
{
  "success": true,
  "message": "투표가 완료되었습니다",
  "voteToken": "encrypted-token" // 익명 투표 검증용
}
```

#### DELETE /api/v1/votes/{voteId}/cast
투표 취소 (재투표 허용 시)
```json
// Response
{
  "success": true,
  "message": "투표가 취소되었습니다"
}
```

### 3.3 투표 결과 API

#### GET /api/v1/votes/{voteId}/results
투표 결과 조회
```json
// Response
{
  "voteId": "vote-uuid",
  "status": "COMPLETED",
  "results": {
    "winner": {
      "optionId": "option-uuid-1",
      "text": "강남역 스터디카페",
      "voteCount": 20,
      "percentage": 57.1
    },
    "options": [
      {
        "optionId": "option-uuid-1",
        "text": "강남역 스터디카페",
        "voteCount": 20,
        "percentage": 57.1,
        "voters": [] // 익명이 아닌 경우만
      },
      {
        "optionId": "option-uuid-2",
        "text": "판교 테크노밸리",
        "voteCount": 15,
        "percentage": 42.9
      }
    ],
    "totalVotes": 35,
    "abstentions": 10
  },
  "analysis": {
    "participationRate": 77.8,
    "peakVotingTime": "2024-11-02T14:00:00Z",
    "averageResponseTime": "2days 3hours"
  }
}
```

#### GET /api/v1/votes/{voteId}/results/realtime
실시간 결과 스트리밍 (WebSocket)
```javascript
// WebSocket 연결
ws://api.signight.com/ws/votes/{voteId}/results

// 수신 메시지
{
  "type": "VOTE_UPDATE",
  "data": {
    "optionId": "option-uuid-1",
    "newCount": 21,
    "totalVotes": 36,
    "percentages": {
      "option-uuid-1": 58.3,
      "option-uuid-2": 41.7
    }
  },
  "timestamp": "2024-11-02T15:30:00Z"
}
```

### 3.4 투표 관리 API

#### PUT /api/v1/votes/{voteId}
투표 수정 (시작 전만 가능)
```json
// Request
{
  "title": "수정된 제목",
  "endAt": "2024-11-08T18:00:00Z"
}
```

#### POST /api/v1/votes/{voteId}/close
투표 조기 종료
```json
// Response
{
  "message": "투표가 종료되었습니다",
  "closedAt": "2024-11-05T12:00:00Z"
}
```

#### GET /api/v1/votes/{voteId}/voters
투표자 목록 (익명이 아닌 경우)
```json
// Response
{
  "voters": [
    {
      "userId": "user-uuid",
      "name": "홍길동",
      "votedAt": "2024-11-02T10:30:00Z",
      "option": "강남역 스터디카페"
    }
  ],
  "nonVoters": [
    {
      "userId": "user-uuid-2",
      "name": "김철수"
    }
  ]
}
```

### 3.5 투표 목록 API

#### GET /api/v1/sigs/{sigId}/votes
SIG별 투표 목록
```json
// Query Parameters
?status=IN_PROGRESS&page=0&size=20

// Response
{
  "content": [
    {
      "id": "vote-uuid",
      "title": "11월 모임 장소 투표",
      "voteType": "SINGLE_CHOICE",
      "status": "IN_PROGRESS",
      "stats": {
        "totalVoters": 25,
        "participationRate": 55.6
      },
      "endAt": "2024-11-07T18:00:00Z"
    }
  ],
  "totalElements": 15,
  "totalPages": 1
}
```

## 4. 데이터베이스 설계

### 4.1 주요 테이블

#### votes
```sql
CREATE TABLE votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    sig_id UUID NOT NULL,
    creator_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    vote_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT',
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

CREATE INDEX idx_votes_sig_id ON votes(sig_id);
CREATE INDEX idx_votes_status ON votes(status);
CREATE INDEX idx_votes_dates ON votes(start_at, end_at);
```

#### vote_options
```sql
CREATE TABLE vote_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vote_id UUID REFERENCES votes(id) ON DELETE CASCADE,
    text VARCHAR(200) NOT NULL,
    description TEXT,
    display_order INTEGER DEFAULT 0,
    added_by UUID, -- 사용자가 추가한 옵션
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vote_options_vote_id ON vote_options(vote_id);
```

#### vote_settings
```sql
CREATE TABLE vote_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vote_id UUID REFERENCES votes(id) ON DELETE CASCADE UNIQUE,
    is_anonymous BOOLEAN DEFAULT false,
    allow_option_add BOOLEAN DEFAULT false,
    max_select_count INTEGER DEFAULT 1,
    show_realtime_result BOOLEAN DEFAULT true,
    allow_revote BOOLEAN DEFAULT false,
    require_comment BOOLEAN DEFAULT false,
    min_select_count INTEGER DEFAULT 1
);
```

#### vote_casts
```sql
CREATE TABLE vote_casts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vote_id UUID REFERENCES votes(id),
    voter_id UUID NOT NULL,
    vote_token VARCHAR(255), -- 익명 투표용 토큰
    comment TEXT,
    voted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP,
    ip_address INET,
    UNIQUE(vote_id, voter_id)
);

CREATE INDEX idx_vote_casts_vote_id ON vote_casts(vote_id);
CREATE INDEX idx_vote_casts_voter_id ON vote_casts(voter_id);
```

#### vote_cast_options
```sql
CREATE TABLE vote_cast_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cast_id UUID REFERENCES vote_casts(id) ON DELETE CASCADE,
    option_id UUID REFERENCES vote_options(id),
    rank INTEGER, -- 순위 투표용
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vote_cast_options_cast_id ON vote_cast_options(cast_id);
CREATE INDEX idx_vote_cast_options_option_id ON vote_cast_options(option_id);
```

#### vote_results_cache
```sql
CREATE TABLE vote_results_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vote_id UUID REFERENCES votes(id) UNIQUE,
    results JSONB NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 5. 도메인 모델

### 5.1 투표 유형
```kotlin
enum class VoteType {
    SINGLE_CHOICE,    // 단일 선택
    MULTIPLE_CHOICE,  // 복수 선택
    RANKING,          // 순위 투표
    YES_NO,           // 찬반 투표
    RATING            // 평점 투표
}
```

### 5.2 투표 상태
```kotlin
enum class VoteStatus {
    DRAFT,        // 초안
    SCHEDULED,    // 예정됨
    IN_PROGRESS,  // 진행중
    COMPLETED,    // 완료
    CANCELLED     // 취소됨
}
```

### 5.3 투표 엔티티
```kotlin
@Entity
@Table(name = "votes")
data class Vote(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(unique = true, nullable = false)
    val code: String,
    
    @Column(nullable = false)
    val sigId: UUID,
    
    @Column(nullable = false)
    val creatorId: UUID,
    
    @Column(nullable = false)
    var title: String,
    
    var description: String? = null,
    
    @Enumerated(EnumType.STRING)
    val voteType: VoteType,
    
    @Enumerated(EnumType.STRING)
    var status: VoteStatus = VoteStatus.DRAFT,
    
    @Column(nullable = false)
    val startAt: LocalDateTime,
    
    @Column(nullable = false)
    var endAt: LocalDateTime,
    
    var closedAt: LocalDateTime? = null,
    
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
```

## 6. 익명 투표 보안

### 6.1 익명성 보장
```kotlin
class AnonymousVoteService {
    // 투표 토큰 생성
    fun generateVoteToken(voteId: String, voterId: String): String {
        val payload = "$voteId:$voterId:${Instant.now()}"
        return encrypt(payload)
    }
    
    // 투표 검증 (사용자 정보 없이)
    fun verifyVoteToken(token: String, voteId: String): Boolean {
        val decrypted = decrypt(token)
        val parts = decrypted.split(":")
        return parts[0] == voteId
    }
    
    // 익명 투표 저장
    fun castAnonymousVote(voteId: String, options: List<String>, token: String) {
        // 토큰만 저장, voter_id는 해시값으로 대체
        val hashedVoterId = hashVoterId(token)
        voteCastRepository.save(
            VoteCast(
                voteId = voteId,
                voterId = hashedVoterId,
                voteToken = token
            )
        )
    }
}
```

### 6.2 중복 투표 방지
```kotlin
// 블룸 필터를 사용한 효율적인 중복 체크
class VoteDuplicationChecker {
    private val bloomFilter = BloomFilter.create(
        Funnels.stringFunnel(Charsets.UTF_8),
        expectedInsertions = 10000,
        fpp = 0.01
    )
    
    fun hasVoted(voteId: String, voterId: String): Boolean {
        val key = "$voteId:$voterId"
        return bloomFilter.mightContain(key)
    }
    
    fun recordVote(voteId: String, voterId: String) {
        val key = "$voteId:$voterId"
        bloomFilter.put(key)
    }
}
```

## 7. 실시간 업데이트

### 7.1 WebSocket 설정
```kotlin
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }
    
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS()
    }
}
```

### 7.2 실시간 결과 발행
```kotlin
@Service
class VoteRealtimeService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun publishVoteUpdate(voteId: String, update: VoteUpdate) {
        messagingTemplate.convertAndSend(
            "/topic/votes/$voteId/results",
            update
        )
    }
    
    fun publishVoteStatus(voteId: String, status: VoteStatus) {
        messagingTemplate.convertAndSend(
            "/topic/votes/$voteId/status",
            VoteStatusUpdate(voteId, status)
        )
    }
}
```

## 8. 투표 집계 알고리즘

### 8.1 순위 투표 집계 (Borda Count)
```kotlin
class RankingVoteCalculator {
    fun calculateResults(votes: List<RankedVote>): List<RankingResult> {
        val scores = mutableMapOf<String, Int>()
        val optionCount = votes.firstOrNull()?.rankings?.size ?: 0
        
        votes.forEach { vote ->
            vote.rankings.forEach { ranking ->
                val points = optionCount - ranking.rank + 1
                scores[ranking.optionId] = 
                    scores.getOrDefault(ranking.optionId, 0) + points
            }
        }
        
        return scores.map { (optionId, score) ->
            RankingResult(optionId, score)
        }.sortedByDescending { it.score }
    }
}
```

### 8.2 즉시 결선 투표 (Instant Runoff)
```kotlin
class InstantRunoffCalculator {
    fun calculateWinner(votes: List<Vote>): String? {
        var remainingVotes = votes.toMutableList()
        var eliminatedOptions = mutableSetOf<String>()
        
        while (true) {
            val counts = countFirstChoices(remainingVotes, eliminatedOptions)
            val total = counts.values.sum()
            
            // 과반수 확인
            val winner = counts.entries.find { it.value > total / 2 }
            if (winner != null) return winner.key
            
            // 최하위 제거
            val loser = counts.minByOrNull { it.value }?.key ?: break
            eliminatedOptions.add(loser)
            
            if (counts.size <= 2) break
        }
        
        return counts.maxByOrNull { it.value }?.key
    }
}
```

## 9. 캐싱 전략

### 9.1 Redis 캐시 구조
```yaml
# 투표 결과 캐시
vote:results:{voteId}:
  - TTL: 5분 (진행중), 1시간 (완료)
  - 내용: 집계 결과

# 사용자 투표 상태 캐시
vote:user:{voteId}:{userId}:
  - TTL: 투표 종료시까지
  - 내용: 투표 여부, 선택 옵션

# 실시간 투표 수 캐시
vote:counts:{voteId}:
  - TTL: 1분
  - 내용: 각 옵션별 투표 수
```

## 10. 배치 작업

### 10.1 투표 상태 업데이트
```kotlin
@Scheduled(cron = "0 * * * * *") // 매분
fun updateVoteStatuses() {
    // 시작 예정 투표
    val toStart = voteRepository.findByStatusAndStartAtBefore(
        VoteStatus.SCHEDULED,
        LocalDateTime.now()
    )
    toStart.forEach { vote ->
        vote.status = VoteStatus.IN_PROGRESS
        voteRepository.save(vote)
        notificationService.notifyVoteStart(vote)
    }
    
    // 종료 예정 투표
    val toEnd = voteRepository.findByStatusAndEndAtBefore(
        VoteStatus.IN_PROGRESS,
        LocalDateTime.now()
    )
    toEnd.forEach { vote ->
        vote.status = VoteStatus.COMPLETED
        voteRepository.save(vote)
        calculateFinalResults(vote)
        notificationService.notifyVoteEnd(vote)
    }
}
```

### 10.2 결과 사전 계산
```kotlin
@Scheduled(cron = "0 */5 * * * *") // 5분마다
fun precalculateResults() {
    val activeVotes = voteRepository.findByStatus(VoteStatus.IN_PROGRESS)
    
    activeVotes.forEach { vote ->
        val results = calculateResults(vote.id)
        voteResultsCacheRepository.save(
            VoteResultsCache(
                voteId = vote.id,
                results = results,
                calculatedAt = Instant.now()
            )
        )
    }
}
```

## 11. 모니터링

### 11.1 비즈니스 메트릭
- 활성 투표 수
- 평균 참여율
- 투표 유형별 분포
- 평균 투표 기간
- 재투표 비율

### 11.2 기술 메트릭
- API 응답 시간
- WebSocket 연결 수
- 캐시 히트율
- 집계 처리 시간
- 데이터베이스 쿼리 성능 