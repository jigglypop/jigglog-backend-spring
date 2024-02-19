echo "before deploy 시작" >> /home/ubuntu/log.txt

cd /home/ubuntu >> /home/ubuntu/log.txt

rm -rf jigglogkotlin >> /home/ubuntu/log.txt

echo "이전 폴더를 삭제" >> /home/ubuntu/log.txt

docker rm -f $(docker ps -aq)
docker rmi $(docker images -q)

echo "before deploy 완료 " >> /home/ubuntu/log.txt