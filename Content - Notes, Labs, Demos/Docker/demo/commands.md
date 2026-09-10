# Demo Commands — in order

Type these live. Don't paste blind.

## 001 - What Docker Is

```bash
docker version
docker run hello-world
docker ps
docker ps -a
docker run -it alpine sh
  cat /etc/os-release
  ls /
  ps aux
  exit
docker images
```

## 002 - Docker Architecture

```bash
docker info
docker pull nginx:alpine
docker pull nginx:latest
docker image inspect nginx:alpine
```

## 003 - Images and Docker Hub

Browser: hub.docker.com -> search nginx -> Tags tab.

```bash
docker images
docker pull nginx
docker images
docker rmi nginx:latest
```

## 004 - Running and Managing Containers

```bash
docker run -d nginx:alpine
# browser: localhost -> fails
docker ps

docker run -d --name web -p 8080:80 nginx:alpine
# browser: localhost:8080 -> works
docker ps

docker logs web
# reload browser
docker logs web

docker exec -it web sh
  ls /usr/share/nginx/html
  echo "<h1>edited inside the container</h1>" > /usr/share/nginx/html/index.html
  exit
# reload browser -> changed

docker stop web
docker ps
docker ps -a
docker start web
# reload -> edit survived

docker rm -f web
docker run -d --name web -p 8080:80 nginx:alpine
# reload -> edit is GONE

docker run --rm alpine echo hi
docker ps -a
```

## 005 - Dockerfile and Building an Image

```bash
cd app
npm start                      # baseline on the host, then Ctrl-C
```

Write `app/Dockerfile` on screen. Then:

```bash
docker build -t myapp:1 .
docker run -d --name app -p 3000:3000 myapp:1
# browser: localhost:3000 -> note the hostname field is the container ID

docker build -t myapp:1 .      # all CACHED
# edit one line of server.js (change the default GREETING)
docker build -t myapp:1 .      # cache breaks at COPY . .

# swap in instructor/Dockerfile.bad-order, rebuild after a source edit
# -> npm install re-runs. Restore the good one.

cat .dockerignore
docker images
```

## 006 - Volumes and Persistence

```bash
docker run -d --name db -e POSTGRES_PASSWORD=secret postgres:16-alpine
docker exec -it db psql -U postgres
  CREATE TABLE demo (note text);
  INSERT INTO demo VALUES ('this will not survive');
  SELECT * FROM demo;
  \q
docker rm -f db
docker run -d --name db -e POSTGRES_PASSWORD=secret postgres:16-alpine
docker exec -it db psql -U postgres -c "SELECT * FROM demo;"   # gone

docker rm -f db
docker run -d --name db -e POSTGRES_PASSWORD=secret -v pgdata:/var/lib/postgresql/data postgres:16-alpine
docker exec -it db psql -U postgres -c "CREATE TABLE demo (note text);"
docker exec -it db psql -U postgres -c "INSERT INTO demo VALUES ('this survives');"
docker rm -f db
docker run -d --name db -e POSTGRES_PASSWORD=secret -v pgdata:/var/lib/postgresql/data postgres:16-alpine
docker exec -it db psql -U postgres -c "SELECT * FROM demo;"   # still there

docker volume ls
docker volume inspect pgdata

cd ..
docker run -d --name web -p 8080:80 -v $(pwd)/site:/usr/share/nginx/html nginx:alpine
# edit site/index.html in the editor, reload browser -> changes, no rebuild
docker rm -f web db
```

## 007 - Docker Compose

Put the topic 005 and 006 `docker run` commands on screen together first.
Then write `compose.yaml` on screen.

```bash
docker compose up -d
docker compose ps
docker compose logs -f app
# browser: localhost:3000 -> database line is now green

docker exec -it docker-demo-app-1 sh
  nc -zv db 5432          # resolves by service name
  exit

docker compose down
docker volume ls          # pgdata survived
docker compose down -v
docker volume ls          # gone
docker compose up -d      # whole stack, one command
```
