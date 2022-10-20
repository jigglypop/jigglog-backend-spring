echo "after deploy 시작" >> /home/ubuntu/log.txt

cd /home/ubuntu/jigglogkotlin/ >> /home/ubuntu/log.txt

docker build -t jigglog-backend .

echo "도커 빌드 완료" >> /home/ubuntu/log.txt

docker run -p 8080:8080 -t jigglog-backend

echo "도커 런 완료" >> /home/ubuntu/log.txt
