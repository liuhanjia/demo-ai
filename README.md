# 打包发布
## 本地
```
docker build -t movier/yeebo-ai .
docker save -o yeebo_ai.tar movier/yeebo-ai
scp yeebo_ai.tar root@47.113.103.241:/srv
```
## 服务器
```
cd /srv
docker load -i yeebo_ai.tar
docker stop yeebo-ai
docker rm yeebo-ai
docker run -d -p 8081:8081 --name yeebo-ai movier/yeebo-ai
```