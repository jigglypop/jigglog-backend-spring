HOME=/home/ubuntu
REPOSITORY=jigglogkotlin

now_time=`date`
echo "[$now_time] --- 백엔드 배포 AFTER ---" >> /home/ubuntu/log.txt
echo "[$now_time] --- 1) Build 파일 복사 ---" >> /home/ubuntu/log.txt

# 시작 로그 기록
now_time=`date`
echo "[$now_time] 2) after deploy 시작, 도커 컴포즈" >> /home/ubuntu/log.txt

cd ${HOME}/${REPOSITORY} || exit >> /home/ubuntu/log.txt
sudo docker build -t jigglogkotlin -f /home/ubuntu/jigglogkotlin/Dockerfile . &&
sudo docker compose -f ${HOME}/${REPOSITORY} up -d --build