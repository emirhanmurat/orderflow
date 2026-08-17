# OrderFlow

Spring Boot microservice order processing system.

Services:
- order-api :8080
- order-worker :8081
- notification-service :8082

Dependencies:
- PostgreSQL
- RabbitMQ
- Redis

Flow:
POST /orders -> PostgreSQL + Redis -> RabbitMQ(order.created)
-> order-worker -> PostgreSQL -> RabbitMQ(order.processed)
-> notification-service

Health:
orders/health

Environment variables are supported for Kubernetes deployment.

# CI/CD Flow
                        ┌──────────────┐
                        │    GitHub    │
                        │ source code  │
                        └──────┬───────┘
                               │
                            webhook
                               │
                               ▼
                        ┌──────────────┐
                        │    Jenkins   │
                        └──────┬───────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
             Maven           Docker        Helm values
                │              │              │
                ▼              ▼              │
             Nexus         DockerHub           │
                │              │              │
                │              │              ▼
                │              │           GitHub
                │              │              │
                │              │              ▼
                │              │          Argo CD
                │              │              │
                │              │              ▼
                │              │       Local Kubernetes
                │              │
                │              └── image pull
                │
                └── JAR/artifacts
