# 시그나이트 MSA 아키텍처 개요

## 1. 시스템 전체 구조

### 1.1 마이크로서비스 구성
```
시그나이트 플랫폼
├── Core Services (핵심 서비스)
│   ├── Auth Service (인증/인가)
│   ├── User Service (사용자 관리)
│   └── SIG Service (SIG 관리)
├── Business Services (비즈니스 서비스)
│   ├── Community Service (커뮤니티)
│   ├── Vote Service (투표)
│   ├── Fund Service (재정)
│   └── Activity Service (활동)
├── Support Services (지원 서비스)
│   ├── Notification Service (알림)
│   ├── File Service (파일)
│   └── Search Service (검색)
└── Infrastructure (인프라)
    ├── API Gateway
    ├── Service Mesh
    └── Message Queue
```

### 1.2 서비스 간 통신
- **동기 통신**: REST API / gRPC
- **비동기 통신**: RabbitMQ / Kafka
- **서비스 디스커버리**: Consul / Eureka
- **API 게이트웨이**: Kong / Spring Cloud Gateway

## 2. 각 서비스별 책임

### 2.1 Core Services

#### Auth Service
- JWT 토큰 발급/검증
- OAuth2.0 소셜 로그인
- 권한 관리 (RBAC)
- 세션 관리

#### User Service
- 회원 정보 관리
- 프로필 관리
- 회원 등급 관리
- 멘사 회원 인증

#### SIG Service
- SIG 생성/관리
- SIG 정보 조회
- SIG 회원 관리
- SIG 카테고리 관리

### 2.2 Business Services

#### Community Service
- 게시판 관리
- 피드 시스템
- 댓글/좋아요
- 해시태그

#### Vote Service
- 투표 생성/관리
- 투표 참여
- 결과 집계
- 익명 투표

#### Fund Service
- 지원금 신청
- 예산 관리
- 영수증 처리
- 재정 보고서

#### Activity Service
- 활동 일정 관리
- 활동 기록
- 출석 체크
- 활동 보고서

### 2.3 Support Services

#### Notification Service
- 이메일 발송
- 푸시 알림
- SMS 발송
- 알림 히스토리

#### File Service
- 파일 업로드
- 이미지 리사이징
- CDN 연동
- 파일 메타데이터

#### Search Service
- Elasticsearch 연동
- 통합 검색
- 검색 인덱싱
- 검색 분석

## 3. 데이터베이스 전략

### 3.1 Database per Service
각 서비스는 독립적인 데이터베이스를 가집니다:

```yaml
services:
  auth-service:
    database: PostgreSQL
    purpose: 사용자 인증 정보
    
  user-service:
    database: PostgreSQL
    purpose: 사용자 프로필, 회원 정보
    
  sig-service:
    database: PostgreSQL
    purpose: SIG 정보, 회원 관계
    
  community-service:
    database: MongoDB
    purpose: 게시글, 댓글, 피드
    
  vote-service:
    database: PostgreSQL
    purpose: 투표 데이터
    
  fund-service:
    database: PostgreSQL
    purpose: 재정 데이터
    
  activity-service:
    database: PostgreSQL
    purpose: 활동 일정, 기록
    
  notification-service:
    database: MongoDB
    purpose: 알림 로그
    
  file-service:
    database: MongoDB
    storage: AWS S3
    purpose: 파일 메타데이터
```

### 3.2 데이터 일관성
- **Saga Pattern**: 분산 트랜잭션 처리
- **Event Sourcing**: 이벤트 기반 데이터 동기화
- **CQRS**: 명령과 조회 분리

## 4. 인프라 구성

### 4.1 컨테이너화
```yaml
# Docker Compose 예시
version: '3.8'
services:
  auth-service:
    build: ./auth-service
    ports:
      - "8001:8080"
    environment:
      - DB_HOST=auth-db
      
  user-service:
    build: ./user-service
    ports:
      - "8002:8080"
    environment:
      - DB_HOST=user-db
```

### 4.2 쿠버네티스 배포
- **Deployment**: 각 서비스별 독립 배포
- **Service**: 서비스 디스커버리
- **Ingress**: 외부 트래픽 라우팅
- **ConfigMap/Secret**: 설정 관리

## 5. 보안 전략

### 5.1 서비스 간 통신 보안
- mTLS (mutual TLS)
- API Key 인증
- Service Mesh (Istio)

### 5.2 외부 통신 보안
- API Gateway에서 인증/인가
- Rate Limiting
- CORS 정책

## 6. 모니터링 및 로깅

### 6.1 모니터링
- **Prometheus**: 메트릭 수집
- **Grafana**: 시각화
- **Jaeger**: 분산 트레이싱

### 6.2 로깅
- **ELK Stack**: 중앙 집중식 로깅
- **Correlation ID**: 요청 추적

## 7. 개발 가이드라인

### 7.1 API 설계 원칙
- RESTful API 설계
- API 버저닝
- 에러 핸들링 표준화
- OpenAPI 문서화

### 7.2 코드 구조
```
service-name/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/signight/[service]/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       └── domain/
│   │   └── resources/
│   └── test/
├── Dockerfile
├── build.gradle.kts
└── README.md
```

## 8. 배포 전략

### 8.1 CI/CD 파이프라인
1. 코드 커밋
2. 자동 테스트
3. 도커 이미지 빌드
4. 이미지 레지스트리 푸시
5. 쿠버네티스 배포
6. 헬스 체크

### 8.2 배포 방식
- **Blue-Green Deployment**
- **Canary Deployment**
- **Rolling Update**

## 9. 확장성 고려사항

### 9.1 수평적 확장
- 각 서비스 독립적 스케일링
- 로드 밸런싱
- 오토 스케일링

### 9.2 캐싱 전략
- Redis 캐싱
- CDN 활용
- 브라우저 캐싱

## 10. 장애 대응

### 10.1 Circuit Breaker
- Hystrix / Resilience4j
- Fallback 메커니즘
- 타임아웃 설정

### 10.2 재시도 정책
- Exponential Backoff
- 최대 재시도 횟수
- Dead Letter Queue 