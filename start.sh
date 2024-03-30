docker rm -f $(docker ps -aq)
docker rmi $(docker images -q)
rm -rf ./build &&
./gradlew build &&
docker build -t jigglog-backend . &&
docker compose -f docker-compose.yml up -d --build