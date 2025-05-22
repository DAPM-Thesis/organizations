#!/bin/bash

if ! docker network inspect kafka-cluster >/dev/null 2>&1; then
    echo "Creating docker network: kafka-cluster"
    docker network create --driver bridge kafka-cluster
else
    echo "Docker network 'kafka-cluster' already exists."
fi

for dir in orgA orgB orgC; do
    echo "==============================="
    echo "Building Maven project in $dir"
    (
        cd "$dir" || { echo "Directory $dir not found, skipping."; exit 0; }
        mvn clean install
        echo "Stopping containers in $dir..."
        docker compose down
        echo "Starting containers in $dir..."
        docker compose up -d
    )
done

echo "==============================="
echo "All Maven builds and docker-compose complete."