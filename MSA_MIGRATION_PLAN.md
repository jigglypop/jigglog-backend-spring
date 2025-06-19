# 🚀 Jigglog MSA 전환 계획서

## 📋 프로젝트 개요

### 현재 상황
- **프로젝트**: Jigglog Backend (Spring Boot + Kotlin)
- **아키텍처**: MSA 전환 진행 중 (Phase 1 완료)
- **기술 스택**: Spring WebFlux, R2DBC, Redis, PostgreSQL, SPIFFE/SPIRE
- **배포 환경**: Docker + Kubernetes (목표)

### 목표
- ✅ **Phase 1 완료**: 인증 서비스 분리 (SPIFFE/SPIRE + OIDC)
- 🔄 **Phase 2 진행 예정**: 콘텐츠 서비스 분리
- 쿠버네티스 기반 컨테이너 오케스트레이션
- 독립적인 배포 및 확장 가능한 구조

### ✅ 완료된 작업 (Phase 1)
- **auth-service**: 독립적인 인증 마이크로서비스 구현
- **SPIFFE/SPIRE 인증**: 최신 Kubernetes 인증 표준 적용
- **OIDC 표준**: JWT + JWKS 엔드포인트 구현
- **Redis 토큰 캐싱**: 성능 최적화
- **Docker 컨테이너화**: 멀티스테이지 빌드
- **외부 인증 통합**: 메인 애플리케이션의 SecurityService 수정

---

## 🏗️ 현재 MSA 아키텍처

### Phase 1: 인증 서비스 분리 완료
```
📁 MSA 구조 (Phase 1)
├── 🔐 auth-service (포트: 8081) ✅ 완료
│   ├── 👤 User Management (SPIFFE ID 지원)
│   ├── 🔑 OIDC Authentication (RS256 JWT)
│   ├── 🔒 Token Validation & JWKS
│   ├── 📊 Redis Token Caching
│   └── 🐳 Docker + Health Check
│
├── 🏢 main-service (포트: 8080) ✅ 수정 완료
│   ├── 📝 Content (Post, Category, Tag)  
│   ├── 💬 Comment (Comment, ReComment)
│   ├── 🎨 Media (Upload, ImageUrl, IconSet)
│   ├── 👤 Portfolio (Portfolio, Resume)
│   ├── 🔗 External Auth Integration
│   └── 🌐 WebClient for Service Communication
│
└── 🗄️ 공유 인프라
    ├── PostgreSQL (포트: 5432)
    ├── Redis (포트: 6379)
    └── Docker Network
```

### 서비스 간 통신
- **auth-service ↔ main-service**: HTTP REST API (WebClient)
- **토큰 검증**: JWKS 엔드포인트 활용
- **헬스체크**: Actuator 기반 모니터링

---

## 🎯 MSA 분리 전략

### Phase 1: 도메인별 서비스 분리

#### 1.1 Auth Service (인증/사용자 관리) ✅ 완료
```
📦 auth-service (Port: 8081) ✅ 구현 완료
├── 🔐 SPIFFE/SPIRE Authentication
├── 👤 User Management (SPIFFE ID)
├── 🔑 OIDC JWT Token Management (RS256)
├── 📊 JWKS 엔드포인트
├── 🗄️ Redis Token Caching
└── 🐳 Docker 컨테이너화
```

**구현된 기능:**
- SPIFFE ID 기반 사용자 식별
- RS256 알고리즘 JWT 토큰 생성/검증
- OIDC Discovery 엔드포인트
- Redis 기반 토큰 캐싱
- 외부 서비스 토큰 검증 API

#### 1.2 Content Service (콘텐츠 관리)
```
📦 content-service (Port: 8082)
├── 📝 Post Management
├── 📂 Category Management  
├── 🏷️ Tag Management
└── 🔗 Post-Tag Relations
```

#### 1.3 Comment Service (댓글 관리)
```
📦 comment-service (Port: 8083)
├── 💬 Comment CRUD
├── 💭 ReComment Management
├── 📊 Comment Statistics
└── 🔔 Comment Notifications
```

#### 1.4 Media Service (미디어 관리)
```
📦 media-service (Port: 8084)
├── 📤 File Upload/Download
├── 🖼️ Image Processing
├── 📁 File Storage Management
└── 🎨 Icon & Thumbnail Management
```

#### 1.5 SIG Service (특별 관심 그룹)
```
📦 sig-service (Port: 8085)
├── 👥 SIG Group Management
├── 🎪 SIG Event Management
├── 💬 SIG Discussion
├── 📚 SIG Resource Sharing
└── 📊 SIG Analytics
```

---

## 🛠️ 기술 스택 및 인프라

### 마이크로서비스 기술 스택
- **Framework**: Spring Boot 3.x + Kotlin
- **Reactive**: Spring WebFlux + R2DBC
- **Database**: MySQL (서비스별 독립 DB)
- **Cache**: Redis Cluster
- **Message Queue**: RabbitMQ / Apache Kafka
- **Search**: ElasticSearch
- **Monitoring**: Micrometer + Prometheus + Grafana

### 컨테이너 및 오케스트레이션
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **Service Mesh**: Istio
- **API Gateway**: Spring Cloud Gateway / Kong
- **Config Management**: Kubernetes ConfigMap/Secret

### CI/CD 파이프라인
- **Source Control**: Git + GitHub
- **CI**: GitHub Actions
- **Registry**: Docker Hub / AWS ECR
- **CD**: ArgoCD + Helm
- **Infrastructure**: Terraform

---

## 📊 데이터베이스 분리 전략

### 서비스별 데이터베이스

#### User Service DB
```sql
-- users, user_profiles, user_settings
-- user_sessions, user_roles
```

#### Content Service DB  
```sql
-- posts, categories, tags, post_to_tag
-- post_statistics, post_versions
```

#### Comment Service DB
```sql
-- comments, re_comments, comment_likes
-- comment_reports, comment_statistics
```

#### Media Service DB
```sql
-- uploads, image_urls, icon_sets
-- file_metadata, storage_locations
```

#### SIG Service DB
```sql
-- sigs, sig_members, sig_events
-- sig_discussions, sig_resources
```

### 데이터 일관성 전략
- **Saga Pattern**: 분산 트랜잭션 관리
- **Event Sourcing**: 중요 도메인 이벤트 관리
- **CQRS**: 읽기/쓰기 분리
- **Eventual Consistency**: 최종 일관성 보장

---

## 🌐 API Gateway 및 라우팅

### Spring Cloud Gateway 설정
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/auth/**, /api/users/**
          filters:
            - name: AuthenticationFilter
        
        - id: content-service
          uri: lb://content-service
          predicates:
            - Path=/api/posts/**, /api/categories/**, /api/tags/**
          
        - id: comment-service
          uri: lb://comment-service
          predicates:
            - Path=/api/comments/**
            
        - id: media-service
          uri: lb://media-service
          predicates:
            - Path=/api/uploads/**, /api/media/**
            
        - id: sig-service
          uri: lb://sig-service
          predicates:
            - Path=/api/sigs/**, /api/sig-events/**
```

### 서비스 간 통신
- **Synchronous**: WebClient (REST API)
- **Asynchronous**: RabbitMQ / Kafka (Event-driven)
- **Service Discovery**: Kubernetes Service Discovery
- **Load Balancing**: Kubernetes Service + Ingress

---

## 📅 단계별 구현 로드맵

### Phase 1: 기반 인프라 구축 (4-6주)

#### Week 1-2: 개발 환경 구성
- [ ] 멀티 모듈 Gradle 프로젝트 구성
- [ ] Docker 컨테이너화
- [ ] Local Kubernetes 클러스터 구성 (minikube/k3s)
- [ ] API Gateway 기본 설정

#### Week 3-4: 공통 라이브러리 구축
- [ ] Shared Domain Models
- [ ] Common Security Components
- [ ] Common Exception Handling
- [ ] Shared Utilities & Configurations

#### Week 5-6: CI/CD 파이프라인 구축
- [ ] GitHub Actions 워크플로우
- [ ] Docker Registry 설정
- [ ] Helm Charts 작성
- [ ] ArgoCD 설정

### Phase 2: 핵심 서비스 분리 (6-8주)

#### Week 1-2: User Service 분리
- [ ] 사용자 인증/인가 로직 분리
- [ ] JWT 토큰 관리 독립화
- [ ] 사용자 프로필 관리
- [ ] 데이터베이스 마이그레이션

#### Week 3-4: Content Service 분리  
- [ ] 포스트 관리 로직 분리
- [ ] 카테고리/태그 관리 독립화
- [ ] 검색 기능 연동
- [ ] 캐싱 전략 구현

#### Week 5-6: Comment Service 분리
- [ ] 댓글/대댓글 로직 분리
- [ ] 알림 시스템 연동
- [ ] 댓글 통계 기능
- [ ] 스팸 필터링

#### Week 7-8: Media Service 분리
- [ ] 파일 업로드/다운로드 분리
- [ ] 이미지 처리 파이프라인
- [ ] CDN 연동
- [ ] 스토리지 최적화

### Phase 3: SIG 서비스 구축 (4-6주)

#### Week 1-2: SIG 도메인 모델링
- [ ] SIG 엔티티 설계
- [ ] SIG 멤버십 관리
- [ ] SIG 권한 모델
- [ ] 데이터베이스 스키마

#### Week 3-4: SIG 핵심 기능 구현
- [ ] SIG 생성/관리
- [ ] 멤버 가입/탈퇴
- [ ] SIG 이벤트 관리
- [ ] SIG 토론 게시판

#### Week 5-6: SIG 고급 기능
- [ ] SIG 추천 시스템
- [ ] SIG 통계 대시보드
- [ ] SIG 알림 시스템
- [ ] SIG 자료 공유

### Phase 4: 최적화 및 모니터링 (4주)

#### Week 1-2: 성능 최적화
- [ ] 서비스 간 통신 최적화
- [ ] 캐싱 전략 고도화
- [ ] 데이터베이스 쿼리 최적화
- [ ] 로드 밸런싱 최적화

#### Week 3-4: 모니터링 및 운영
- [ ] 메트릭 수집 (Prometheus)
- [ ] 대시보드 구성 (Grafana)
- [ ] 로그 집중화 (ELK Stack)
- [ ] 알람 시스템 구축

---

## 🔧 개발 가이드라인

### 코드 구조 표준
```kotlin
// 마이크로서비스 표준 구조
{service-name}/
├── src/main/kotlin/com/jigglog/{service}/
│   ├── {Service}Application.kt
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   │   ├── entity/
│   │   ├── dto/
│   │   └── event/
│   ├── config/
│   └── client/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
├── Dockerfile
├── helm/
└── k8s/
```

### API 설계 원칙
- RESTful API 표준 준수
- OpenAPI 3.0 문서화
- 버전 관리 (/api/v1/)
- 일관된 응답 형식
- 적절한 HTTP 상태 코드

### 데이터 관리 원칙
- 서비스별 데이터 소유권
- 이벤트 기반 데이터 동기화
- 읽기 모델 최적화
- 백업 및 복구 전략

---

## 📈 확장성 고려사항

### 수평 확장 전략
- **Auto Scaling**: HPA (Horizontal Pod Autoscaler)
- **Database Sharding**: 사용자/지역별 샤딩
- **Cache Scaling**: Redis Cluster 확장
- **CDN Integration**: 정적 자원 분산

### 성능 최적화
- **Connection Pooling**: 데이터베이스 연결 최적화
- **Caching Strategy**: 다단계 캐싱 구조
- **Async Processing**: 비동기 처리 확대
- **Query Optimization**: 인덱스 최적화

### 보안 강화
- **Service Mesh Security**: mTLS 통신
- **API Rate Limiting**: 요청 제한
- **Data Encryption**: 전송/저장 데이터 암호화
- **Audit Logging**: 보안 감사 로그

---

## 💰 비용 및 리소스 추정

### 개발 리소스
- **Backend Developer**: 2-3명 (6개월)
- **DevOps Engineer**: 1명 (3개월)
- **QA Engineer**: 1명 (2개월)

### 인프라 비용 (월 기준)
- **Kubernetes Cluster**: $200-500
- **Database Instances**: $300-800  
- **Cache & Storage**: $100-300
- **Monitoring Tools**: $50-150

### 예상 총 비용
- **개발 비용**: $100,000-150,000
- **인프라 비용**: $8,000-20,000 (연간)
- **운영 비용**: $30,000-50,000 (연간)

---

## 📊 성공 지표 (KPI)

### 기술 지표
- **서비스 가용성**: 99.9% 이상
- **응답 시간**: 평균 200ms 이하
- **처리량**: 초당 1000 요청 처리
- **에러율**: 0.1% 이하

### 비즈니스 지표
- **배포 빈도**: 주 2-3회
- **복구 시간**: 평균 5분 이하
- **개발 생산성**: 기능 개발 50% 단축
- **확장성**: 트래픽 10배 증가 대응

---

## 🎯 마일스톤 및 체크포인트

### Milestone 1: 인프라 구축 완료
- **목표일**: 6주 후
- **체크포인트**: 
  - [ ] Local K8s 클러스터 동작
  - [ ] CI/CD 파이프라인 구축
  - [ ] API Gateway 설정 완료

### Milestone 2: 핵심 서비스 분리 완료
- **목표일**: 14주 후  
- **체크포인트**:
  - [ ] User/Content/Comment/Media 서비스 독립 동작
  - [ ] 서비스 간 통신 정상 동작
  - [ ] 데이터 일관성 보장

### Milestone 3: SIG 서비스 런칭
- **목표일**: 20주 후
- **체크포인트**:
  - [ ] SIG 서비스 완전 동작
  - [ ] 사용자 테스트 완료
  - [ ] 성능 기준 충족

### Milestone 4: 프로덕션 배포 완료
- **목표일**: 24주 후
- **체크포인트**:
  - [ ] 모든 서비스 프로덕션 배포
  - [ ] 모니터링 시스템 가동
  - [ ] 성공 지표 달성

---

## 🚨 리스크 관리

### 기술적 리스크
- **복잡성 증가**: 단계적 접근으로 리스크 완화
- **데이터 일관성**: 철저한 테스트 및 모니터링
- **성능 저하**: 프로파일링 및 최적화
- **서비스 장애**: Circuit Breaker 패턴 적용

### 운영 리스크  
- **배포 복잡성**: Blue-Green 배포 전략
- **모니터링 부족**: 포괄적 관찰성 도구 도입
- **팀 학습 곡선**: 교육 및 문서화 강화
- **비용 초과**: 정기적인 비용 검토

---

## 📚 참고 자료

### 아키텍처 패턴
- [Microservices Patterns by Chris Richardson](https://microservices.io/)
- [Building Microservices by Sam Newman](https://samnewman.io/books/building_microservices/)

### 기술 문서
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Istio Service Mesh](https://istio.io/latest/docs/)

### 모니터링 도구
- [Prometheus Monitoring](https://prometheus.io/)
- [Grafana Dashboard](https://grafana.com/)
- [Jaeger Tracing](https://www.jaegertracing.io/)

---

**문서 버전**: 1.0  
**작성일**: 2024년 12월  
**검토자**: 개발팀  
**승인자**: 프로젝트 매니저 