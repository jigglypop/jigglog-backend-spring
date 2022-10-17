## spring boot MSA

```shell
./gradlew :microservices:post:build


docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" post
```

### 1) docker 명령어

```shell
// 메모리 정리
docker system prune --volumes

// 도커 캐시 삭제
docker system prune --volumes

// 도커 컨테이너 확인
docker ps

// 도커 log 커맨드
docker logs -f

// 도커 모든 이미지 삭제
docker rmi -f $(docker images -q) 

// 도커 모든 컨테이너 삭제
docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
```

### 2) build
```shell
./gradlew build
// 또는
source ./start.sh
```

### 3) docker-compose up
```shell
docker-compose up -d

// 도커 로그 모니터링
docker-compose logs -f
```

### kafka 연결
```shell
docker-compose exec kafka /opt/kafka/bin/kafka-topics.sh --zookeeper zookeeper --list

// 도커 로그 모니터링
docker-compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic posts --from-beginning --timeout-ms 1000 --partition1

```

### 키 생성
```shell
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ./spring-cloud/gateway/src/main/resources/keystore/edge-test.p12 -validity 3650
```



# 쿠버네티스
```shell
kubectl apply -f k8s/nginx/nginx-deployment.yaml

kubectl apply -f k8s/nginx/nginx-service.yaml



# 생성 확인

kubectl get svc

kubectl create namespace kongkong

kubectl config set-context $(kubectl config current-context) --namespace=kongkong 

```


# minikube

```shell
# 프로필 생성
minikube start --memory=4084 --cpus=2 --disk-size=30g \
  --vm-driver=virtualbox -p kongkong

# 프로필 전환
minikube profile list
minikube profile kongkong

# 네임스페이스
kubectl create namespace kong-namespace
kubectl config set-context $(kubectl config current-context) --namespace=kong-namespace

minikube addons enable ingress

kubectl expose deployment nginx-deploy --type=LoadBalancer --port=8080

```
https://velog.io/@haeny01/AWS-Jenkins%EB%A5%BC-%ED%99%9C%EC%9A%A9%ED%95%9C-Docker-x-SpringBoot-CICD-%EA%B5%AC%EC%B6%95

## 젠킨스
1. 패키지 업데이트
```shell
wget -q -O - https://pkg.jenkins.io/debian/jenkins.io.key | sudo apt-key add -
echo deb http://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list

sudo apt update
## openjdk 설치
sudo apt install openjdk-11-jre

sudo apt install jenkins
sudo systemctl status jenkins
```


### 암호 얻기
```shell
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 재시작
```shell
jenkins service restart 
service jenkins restart 

```