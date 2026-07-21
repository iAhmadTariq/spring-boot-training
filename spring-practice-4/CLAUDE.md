# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A single-module Spring Boot 4.0.7 (Java 25) REST API for a "news" resource, backed by H2 with Liquibase-managed schema/seed data. It's a training/practice project (`spring-practice-4` per `pom.xml` artifactId), so expect a small, straightforward codebase without extensive layering.

## Commands

- Build: `./mvnw clean install` (or `mvn clean install` if using a system Maven)
- Run the app: `./mvnw spring-boot:run` (serves on port 8080 by default)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=NewsControllerTest`
- Run a single test method: `./mvnw test -Dtest=NewsControllerTest#findAll_withoutPageParams_shouldReturnFirstPageWithDefaults`
- Start observability stack (Jaeger + Prometheus): `docker-compose up`

## Architecture

**Package layout** follows a feature-package style under `com.redmath.training.news`: `controller` / `service` / `repository` / `model` / `exception`, each containing a single class today (`NewsController`, `NewsService`, `NewsRepository`, `News`, `GlobalExceptionHandler`). When adding a new resource, mirror this structure as a sibling package (e.g. `com.redmath.training.<feature>`) rather than nesting inside `news`.

**Request flow**: `NewsController` (`/api/v1/news`) → `NewsService` → `NewsRepository` (Spring Data JPA over H2). `NewsService` handles not-found cases by throwing `NoSuchElementException`, which `GlobalExceptionHandler` (`@RestControllerAdvice`) maps to 404; `MethodArgumentNotValidException` from `@Valid` request bodies is mapped to 400 with a field→message error map.

**Security** (`config/ApiSecurityConfiguration.java`) is role-based via `HttpSecurity`, not method-level annotations:
- `GET` requests are public.
- `POST` requires `reporter` or `editor` role.
- `PUT`/`PATCH`/`DELETE` require `editor` role.
- Any other request requires `admin` role.
- Auth is HTTP Basic/form-login backed by a single in-memory user defined in `application.yaml` (`spring.security.user`, currently role `editor`) — there's no user store or persistence for credentials.
- CSRF is disabled.

When adding new endpoints, update the matcher rules in `ApiSecurityConfiguration` explicitly — there's no default-permissive fallback for unlisted paths (they fall through to `anyRequest().hasRole("admin")`).

**Persistence**: schema is defined entirely via Liquibase changelogs under `src/main/resources/db/changelog/`, not JPA `ddl-auto` (which is set to `none`). `db.changelog-master.yaml` includes the DDL (`db.changelog-news-ddl.yaml`) and seed data (`db.changelog-seed-news-data-dml.yaml`) changesets. Any schema change must go through a new Liquibase changeset, not entity annotation changes alone.

**Observability**: the app exports OpenTelemetry traces via OTLP/gRPC (`http://localhost:4317`, 100% sampling) and Prometheus-scrapeable metrics (`/actuator/prometheus`). All actuator endpoints are exposed (`management.endpoints.web.exposure.include: "*"`). `docker-compose.yml` spins up Jaeger (trace UI at `:16686`) and Prometheus (`:9090`, scraping the host app via `host.docker.internal:8080`) to consume this locally. The app also registers as a Spring Boot Admin client against `http://localhost:8081`.

**Static frontend**: a minimal HTML/CSS/JS UI lives under `src/main/resources/static/` (served by Spring Boot's default static resource handling) and talks to the `/api/v1/news` endpoints directly.

**API docs**: springdoc-openapi is on the classpath, so Swagger UI/OpenAPI JSON are available at the standard `/swagger-ui.html` and `/v3/api-docs` paths once the app is running.
