#!/bin/bash
echo "Starting FocusAdapt API in DEVELOPMENT mode..."
export SPRING_PROFILES_ACTIVE=dev
chmod +x ./mvnw
./mvnw spring-boot:run
