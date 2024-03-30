PROJECT_NAME=jigglogkotlin-spring-boot

echo "before deploy 시작" >> /home/ubuntu/log.txt

cd /home/ubuntu >> /home/ubuntu/log.txt || exit

# 폴더 여부 확인하고 있으면 삭제
if [ -d ./jigglogkotlin ]; then
  rm -rf ./jigglogkotlin >> /home/ubuntu/log.txt
  echo "이전 폴더를 삭제" >> /home/ubuntu/log.txt
fi

echo "before deploy 완료 " >> /home/ubuntu/log.txt