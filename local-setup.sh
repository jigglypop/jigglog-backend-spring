#!/bin/bash

echo "=== Jigglog Backend 로컬 환경 설정 ==="

# 색상 정의
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Java 17 설치 확인
echo -e "${YELLOW}1. Java 17 설치 확인...${NC}"
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -eq "17" ]; then
        echo -e "${GREEN}✓ Java 17이 설치되어 있습니다.${NC}"
    else
        echo -e "${RED}✗ Java 17이 필요합니다. 현재 버전: $JAVA_VERSION${NC}"
        exit 1
    fi
else
    echo -e "${RED}✗ Java가 설치되어 있지 않습니다.${NC}"
    exit 1
fi

# Docker 설치 확인
echo -e "${YELLOW}2. Docker 설치 확인...${NC}"
if command -v docker &> /dev/null; then
    echo -e "${GREEN}✓ Docker가 설치되어 있습니다.${NC}"
else
    echo -e "${RED}✗ Docker가 설치되어 있지 않습니다.${NC}"
    exit 1
fi

# Docker Compose 설치 확인
echo -e "${YELLOW}3. Docker Compose 설치 확인...${NC}"
if command -v docker-compose &> /dev/null; then
    echo -e "${GREEN}✓ Docker Compose가 설치되어 있습니다.${NC}"
else
    echo -e "${RED}✗ Docker Compose가 설치되어 있지 않습니다.${NC}"
    exit 1
fi

# 필요한 서비스 시작
echo -e "${YELLOW}4. 필요한 서비스 시작 (MySQL, Redis, Elasticsearch)...${NC}"
docker-compose -f docker-compose-local.yml up -d

# 서비스가 준비될 때까지 대기
echo -e "${YELLOW}5. 서비스가 준비될 때까지 대기 중...${NC}"
sleep 10

# MySQL 연결 테스트
echo -e "${YELLOW}6. MySQL 연결 테스트...${NC}"
docker exec jigglog-mysql mysql -uroot -pyourMySQLPassword -e "SELECT 1" &> /dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ MySQL 연결 성공${NC}"
else
    echo -e "${RED}✗ MySQL 연결 실패${NC}"
fi

# Redis 연결 테스트
echo -e "${YELLOW}7. Redis 연결 테스트...${NC}"
docker exec jigglog-redis redis-cli -a yourRedisPassword ping &> /dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Redis 연결 성공${NC}"
else
    echo -e "${RED}✗ Redis 연결 실패${NC}"
fi

# Elasticsearch 연결 테스트
echo -e "${YELLOW}8. Elasticsearch 연결 테스트...${NC}"
curl -s http://localhost:9200/_cluster/health > /dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Elasticsearch 연결 성공${NC}"
else
    echo -e "${RED}✗ Elasticsearch 연결 실패${NC}"
fi

echo -e "\n${GREEN}=== 환경 설정 완료! ===${NC}"
echo -e "${YELLOW}Spring Boot 애플리케이션을 실행하려면:${NC}"
echo -e "  ${GREEN}./gradlew bootRun${NC}"
echo -e "\n${YELLOW}서비스 중지:${NC}"
echo -e "  ${GREEN}docker-compose -f docker-compose-local.yml down${NC}" 