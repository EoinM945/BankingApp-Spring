[![Build](https://img.shields.io/github/actions/workflow/status/EoinM945/BankingApp-Spring/ci.yml?branch=main)](https://github.com/EoinM945/BankingApp-Spring/actions) [![License](https://img.shields.io/github/license/EoinM945/BankingApp-Spring)](https://github.com/EoinM945/BankingApp-Spring/blob/main/LICENSE) [![Java](https://img.shields.io/badge/java-17-blue)](https://www.oracle.com/java/) [![Codecov](https://img.shields.io/codecov/c/gh/EoinM945/BankingApp-Spring)](https://codecov.io/gh/EoinM945/BankingApp-Spring)

# BankingApp-Spring (MasterBank)

A Spring Boot banking application (backend + server-side UI) built with Java 17. Provides account, transaction and user management with Spring Security, JWT auth, PostgreSQL persistence, email support and AWS S3 integration for attachments.

---

## Key features

- User registration, authentication and authorization (Spring Security + JWT)
- Accounts and transactions backed by JPA/Hibernate
- Server-rendered UI using Thymeleaf
- Email sending support (Spring Mail)
- File/object storage support via AWS S3 SDK
- PostgreSQL as primary production datastore
- Unit and integration tests using Spring Boot test starters

---

## Technologies & Libraries (detected from pom.xml)

- Java 17
- Spring Boot 4.1 (starters used):
  - spring-boot-starter-webmvc
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-thymeleaf
  - spring-boot-starter-validation
  - spring-boot-starter-mail
- Thymeleaf extras for Spring Security (thymeleaf-extras-springsecurity6)
- PostgreSQL JDBC driver (runtime)
- Lombok (optional)
- JJWT (io.jsonwebtoken) for JWT handling (api/impl/jackson)
- ModelMapper for DTO mapping
- AWS SDK v2 (software.amazon.awssdk:s3)
- Test starters for the above Spring modules

---

## Prerequisites

- JDK 17+
- Maven 3.8+
- PostgreSQL (local or managed)
- AWS credentials (if using S3 features)
- (Optional) Docker for containerized deployment

---

## Running locally

1. Copy or create `src/main/resources/application.properties` or set environment variables.
2. Set database connection properties (examples):

```
spring.datasource.url=jdbc:postgresql://localhost:5432/masterbank
spring.datasource.username=masterbank_user
spring.datasource.password=secret
spring.jpa.hibernate.ddl-auto=update
```

3. (If using JWT or mail) set secrets and mail settings:

```
# JWT
app.jwt.secret=your-jwt-secret
app.jwt.expiration-ms=3600000

# Mail
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=you@example.com
spring.mail.password=mailpassword

# AWS S3 (if used)
aws.accessKeyId=...
aws.secretAccessKey=...
aws.region=eu-west-1
aws.s3.bucket=your-bucket
```

4. Build & run:

```
mvn clean package
mvn spring-boot:run
```

5. Run tests:

```
mvn test
```

---

## Docker (recommended for deployment)

A basic Dockerfile for a Spring Boot fat JAR:

```
FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:

```
mvn -DskipTests package
docker build -t masterbank:latest .
docker run -e SPRING_PROFILES_ACTIVE=prod -p 8080:8080 masterbank:latest
```

---

## Backend deployment options

- Docker + AWS ECS / Fargate — push image to ECR and run as a service (recommended for production scalability).
- Kubernetes (EKS/GKE/AKS) — containerized microservice deployment with Secrets and ConfigMaps for credentials.
- AWS Elastic Beanstalk — fast platform deployment using the JAR/zip artifact.
- Traditional VM or on-premise server — run the packaged JAR with systemd or other process manager.

Notes:
- Use a managed PostgreSQL service (RDS, Cloud SQL) for production.
- Store secrets in AWS Secrets Manager / HashiCorp Vault / Kubernetes secrets, not in properties files.
- Configure HTTPS (e.g., through a load balancer or ingress controller) and set secure cookie flags.

---

## Configuration & Secrets

- Move all sensitive values (DB credentials, JWT secret, AWS keys) to environment variables or a secret manager.
- Example env-based overrides (Windows PowerShell / Linux):
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `APP_JWT_SECRET` (map to your app property)

---

## Contributing

- Fork the repo, create a feature branch, open a PR with tests covering changes.
- Keep changes focused and update README if you add configuration or new services.

---

## Where to look in the codebase

- `src/main/java` — application source packages
- `src/main/resources` — properties, templates (Thymeleaf), static assets
- `pom.xml` — build, dependencies and Java version (Java 17)

---

## Contact / License

- Author: project repository (see Git history)
- License: Add a LICENSE file or update README with your chosen license.

---

## Example .env (local)

A copyable example of common environment variables. Save as `.env` or map these into your environment before running.

```
# Copy of .env.example
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/masterbank
SPRING_DATASOURCE_USERNAME=masterbank_user
SPRING_DATASOURCE_PASSWORD=secret
SPRING_JPA_HIBERNATE_DDL_AUTO=update

APP_JWT_SECRET=change-me
APP_JWT_EXPIRATION_MS=3600000

SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=you@example.com
SPRING_MAIL_PASSWORD=mailpassword

AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY
AWS_REGION=eu-west-1
AWS_S3_BUCKET=your-bucket
```

## Docker Compose (development)

A docker-compose file is included to run PostgreSQL + the app locally. It expects a `.env` file in the repo root for credentials (or set environment variables directly).

Start services:

```
docker compose up --build
```

This starts:
- `db` (Postgres 15) on port 5432
- `app` (built from the repo) on port 8080

Adjust `docker-compose.yml` for production readiness (secrets, networks, healthchecks, non-root images, resource limits).

If any of the above assumptions (e.g., property names or deployment preferences) should be tailored, say which items to adjust and README will be updated accordingly.
