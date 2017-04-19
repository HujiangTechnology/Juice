#!/bin/sh

cd /app

echo "environment=${environment}"

java -Dsystem.environment=${environment} -jar ocs-juice.jar