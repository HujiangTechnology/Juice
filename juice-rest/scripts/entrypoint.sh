#!/bin/sh

cd /app

echo "environment=${environment}"

java -Dspring.profiles.active=${environment} -jar juice-rest.jar