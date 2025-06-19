#!/bin/bash

export JAVA_HOME="/c/Program Files/Java/jdk-23"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Cleaning up existing containers and images..."
docker compose -f docker-compose.yml down

if [ "$(docker ps -aq)" ]; then
    docker rm -f $(docker ps -aq)
fi

if [ "$(docker images -q)" ]; then
    docker rmi -f $(docker images -q) 2>/dev/null || true
fi

echo "Cleaning build directory..."
rm -rf ./build

echo "Building application with Java $(java -version 2>&1 | head -n 1)..."
./gradlew build

echo "Building Docker image..."
docker build -t jigglog-backend .

echo "Starting services..."
docker compose -f docker-compose.yml up -d --build