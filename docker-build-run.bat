@echo off
echo Building FocusAdapt production Docker image and starting stack...
docker compose -f compose.prod.yaml down --remove-orphans
docker compose --env-file .env.prod -f compose.prod.yaml up --build -d
echo Production stack started successfully!
echo API is available at http://localhost:8080/api
echo Health check is available at http://localhost:8080/api/actuator/health
