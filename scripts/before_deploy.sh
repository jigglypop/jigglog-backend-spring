HOME=/home/ubuntu
REPOSITORY=jigglogkotlin


now_time=`date`
echo "[$now_time] --- 백엔드 배포 BEFORE ---" >> /home/ubuntu/log.txt

# 1. 이전 폴더 삭제
now_time=`date`
echo "[$now_time] 1) 이전 폴더 확인 후 있으면 삭제" >> /home/ubuntu/log.txt

cd ${HOME} >> /home/ubuntu/log.txt
# 폴더 여부 확인하고 있으면 삭제
if [ -d ./$HOME ]; then
    rm -rf ./${HOME} >> /home/ubuntu/log.txt
    now_time=`date`
    echo "[$now_time] 1-1) 이전 폴더 삭제" >> /home/ubuntu/log.txt
fi

# 2. 이전 도커 컨테이너 삭제
now_time=`date`
echo "[$now_time] 2) 이전 도커 컨테이너 삭제" >> /home/ubuntu/log.txt
docker rm -f ${REPOSITORY} >> /home/ubuntu/log.txt
docker rmi -f ${REPOSITORY} >> /home/ubuntu/log.txt

now_time=`date`
echo "[$now_time] --- 백엔드 before deploy 완료 --- " >> /home/ubuntu/log.txt