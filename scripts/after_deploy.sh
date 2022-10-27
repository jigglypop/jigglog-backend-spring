REPOSITORY=/home/ubuntu/jigglogkotlin
PROJECT_NAME=jigglogkotlin

echo "> Build 파일 복사" >> /home/ubuntu/log.txt

cp $REPOSITORY/build/libs/*.jar $REPOSITORY/

echo "> 현재 구동중인 애플리케이션 pid 확인" >> /home/ubuntu/log.txt

CURRENT_PID=$(pgrep -f ${PROJECT_NAME}.*.jar)

echo "> 현재 구동중인 애플리케이션 pid: $CURRENT_PID" >> /home/ubuntu/log.txt

if [ -z "$CURRENT_PID" ]; then
    echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ubuntu/log.txt
else
    echo "> kill -9 $CURRENT_PID" >> /home/ubuntu/log.txt
    kill -9 $CURRENT_PID
    sleep 2
fi

echo "> 새 애플리케이션 배포" >> /home/ubuntu/log.txt

JAR_NAME=$(ls -tr $REPOSITORY/ | grep jar | tail -n 1)

echo "> JAR Name: $JAR_NAME" >> /home/ubuntu/log.txt

#java -jar $REPOSITORY/$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
#nohup java -jar $REPOSITORY/$JAR_NAME > $REPOSITORY/nohup.out 2>&1 &