# Tech Stack

Java 17 / Spring Boot 3.5.7 / MyBatis-Plus 3.5.14 / MySQL 8.0 / Redis (Lettuce) / Elasticsearch 7.17 / RocketMQ 5.1.4 / MinIO / jjwt 0.13.0 / Hutool 5.8.42 / Lombok

# Infrastructure

Docker Compose for local infra services is at `docs/docker/docker-compose.yml` (MySQL, Redis, ES, RocketMQ namesrv+broker, MinIO). Application config profiles: `dev` (shared framework config) and `env`/`local` (environment-specific values).
