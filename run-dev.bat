@echo off

set SPRING_PROFILES_ACTIVE=dev
set SPRING_DOCKER_COMPOSE_ENABLED=false

docker compose -f compose.yaml down --remove-orphans
docker compose --env-file .env.dev -f compose.yaml up --build -d

call mvnw spring-boot:run