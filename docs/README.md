# Jigglog Backend Spring 프로젝트 분석

## 프로젝트 개요

**프로젝트명**: jigglog-backend-spring  
**기술 스택**: Spring Boot 2.7.4, Kotlin, WebFlux, R2DBC, Redis, Elasticsearch  
**아키텍처**: Reactive Programming 기반의 RESTful API 백엔드

## 프로젝트 구조

```
jigglog-backend-spring/
├── src/main/kotlin/com/ydh/jigglog/
│   ├── config/           # 설정 클래스
│   ├── domain/           # 도메인 모델
│   │   ├── dto/         # 데이터 전송 객체
│   │   ├── entity/      # 엔티티 클래스
│   │   └── domain/      # 도메인 서비스
│   ├── handler/          # 요청 핸들러
│   ├── repository/       # 데이터 액세스 계층
│   ├── router/           # 라우팅 설정
│   └── service/          # 비즈니스 로직
├── docker-compose.yml    # 컨테이너 오케스트레이션
├── Dockerfile           # 컨테이너 빌드 설정
└── build.gradle.kts     # 빌드 설정
```

## 주요 기능

1. **사용자 인증/인가**: JWT 기반 인증 시스템
2. **포스트 관리**: 블로그 포스트 CRUD 기능
3. **카테고리/태그**: 포스트 분류 시스템
4. **댓글 시스템**: 계층형 댓글 구조
5. **캐싱**: Redis를 활용한 데이터 캐싱
6. **검색**: Elasticsearch 기반 검색 기능
7. **파일 업로드**: AWS S3 연동

## 분석 문서 목록

- [보안 문제점 분석](./security-issues.md)
- [코드 품질 문제점](./code-quality-issues.md)
- [아키텍처 문제점](./architecture-issues.md)
- [성능 최적화 포인트](./performance-issues.md)
- [개선 권장사항](./improvement-recommendations.md)

## 중요도별 문제점 요약

### 🔴 심각 (즉시 수정 필요)
- application.yml 보안 정보 노출
- CORS 설정 보안 취약점
- 예외 처리 미흡

### 🟡 보통 (개선 권장)
- 코드 중복 제거
- 캐싱 전략 개선
- 테스트 코드 보강

### 🟢 경미 (향후 개선)
- 네이밍 컨벤션 통일
- 로깅 전략 개선
- 문서화 보강 