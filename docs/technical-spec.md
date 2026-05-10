# TECHNICAL_SPEC — `idempiere-reimagined`

> Architecture, conventions, and NFRs. The companion to `docs/design-spec.md`.

## 1. Architecture style

**Modular Spring Boot microservices.** Each service is independently deployable, owns its database (no shared schema), and communicates with peers exclusively via:

- **REST through the gateway** — no direct service-to-service HTTP except through `api-gateway`.
- **Apache Artemis JMS topics** for event-driven async — fan-out via topic subscriptions, no point-to-point.

Single auth boundary at the gateway. JWT validated once at the edge; downstream services trust the gateway's headers.

## 2. Package structure (per service)

Every Spring Boot service follows the same package layout:

```
com.corestory.idempiere.<service>/
├── <Service>Application.java       # @SpringBootApplication
├── api/
│   ├── <Resource>Controller.java   # @RestController, thin
│   └── dto/                         # Java records — request/response shapes
├── service/
│   ├── <Resource>Service.java      # business logic
│   └── mapper/                      # MapStruct DTO ↔ entity
├── repo/
│   └── <Entity>Repository.java     # Spring Data JPA
├── model/
│   └── <Entity>.java               # JPA entity
├── events/
│   ├── <Service>EventPublisher.java # JmsTemplate wrapper
│   └── <Resource>EventListener.java # @JmsListener
├── config/
│   ├── JmsConfig.java              # topic-mode JmsTemplate + listener factory
│   ├── JpaAuditingConfig.java      # @EnableJpaAuditing + AuditorAware
│   └── (other @Configuration beans)
├── exception/
│   ├── <Service>Exception.java     # domain exceptions
│   └── RestExceptionHandler.java   # @RestControllerAdvice → ApiError
└── scheduler/                       # optional, e.g., reservation expiry
```

Test layout mirrors with `src/test/java/.../<service>/{api,service,...}` plus an `integration/` subpackage for Testcontainers tests.

## 3. Frameworks / libraries

| Concern | Choice |
|---|---|
| Application framework | Spring Boot 3.4.1 |
| Java version | 17 (LTS) |
| Build | Maven (multi-module, parent POM mirrors `petstore-modernized` pattern internally) |
| Persistence | Spring Data JPA + Hibernate |
| Migrations | Flyway 10.x — `flyway-core` + `flyway-database-postgresql` |
| Database | PostgreSQL 16 |
| Messaging | Apache Artemis (`spring-boot-starter-artemis`) |
| Gateway | Spring Cloud Gateway 2024.0.0 |
| HTTP server | Netty (Spring Boot default for reactive gateway) / Tomcat (servlet for services) |
| JWT | jjwt 0.12.6 |
| Mapping | MapStruct 1.6.x with Lombok plugin chain |
| Lombok | optional dependency |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Testing | JUnit 5, Mockito, Testcontainers (Postgres + embedded Artemis), AssertJ |
| Frontend | Next.js 14, React 18, TypeScript strict, Tailwind 3, TanStack Query 5, Zustand |
| Container | Docker / Docker Compose |
| Mail (test) | MailHog |

## 4. Coding conventions

### Naming

- `*Controller` — REST entry point. Thin; delegates to a service.
- `*Service` — business logic. `@Service`, `@Transactional` on writes.
- `*Repository` — Spring Data interface. `extends JpaRepository<E, Long>`.
- `*Event` — record implementing `DomainEvent`.
- `*Listener` — JMS consumer. `@JmsListener`.
- `*Publisher` — JMS producer. Wraps `JmsTemplate.convertAndSend()`.
- `*Mapper` — MapStruct interface, `@Mapper(componentModel = "spring")`.
- DTOs: `XxxDto` (response), `CreateXxxRequest` / `UpdateXxxRequest` (write).

### DTO vs entity boundary

- **Never expose JPA entities in controllers.** Map to DTO.
- DTOs are `record`s — immutable by default.
- Entities use Lombok `@Getter @Setter` (mutable, since JPA needs it). Equals/hashCode by `id` only.

### Money / qty

- **Always `BigDecimal`.** Never `double` or `float`.
- Schema columns: `NUMERIC(19, 4)` for money, `NUMERIC(19, 4)` for qty (or `NUMERIC(19, 8)` where higher precision is required).

### Audit columns

Every entity has:

- `created_at TIMESTAMP WITH TIME ZONE NOT NULL` (`@CreatedDate`)
- `updated_at TIMESTAMP WITH TIME ZONE NOT NULL` (`@LastModifiedDate`)
- `created_by VARCHAR(64) NOT NULL` (`@CreatedBy`)
- `updated_by VARCHAR(64) NOT NULL` (`@LastModifiedBy`)
- `version BIGINT NOT NULL` (`@Version`)
- `tenant_id BIGINT NOT NULL DEFAULT 1`
- `org_id BIGINT NOT NULL DEFAULT 1`

`AuditorAware<String>` returns `"system"` for now. Real implementation would consult the JWT subject.

### Validation

- Jakarta Bean Validation on request DTOs: `@NotNull`, `@NotBlank`, `@Positive`, `@Size`, etc.
- Method-level `@Validated` on services that accept primitives.

### Transactions

- `@Transactional` on service methods that write.
- Read-only services use `@Transactional(readOnly = true)` at class level.
- Flyway runs at startup before JPA validate.

## 5. Design patterns

- **Port / adapter** for cross-cutting integrations (`NotificationSender`, `TemplateRenderer`, `CarrierClient`). Adapters implement the port, services depend on the port.
- **Event-driven inter-service communication.** No synchronous service-to-service HTTP. All cross-service interactions via Artemis topics.
- **Repository per aggregate.** Controllers and services depend on repository interfaces, not concrete classes.
- **State machine encapsulation.** Order/Receipt/Shipment status transitions go through a service method that validates the transition and emits the event in one transaction.
- **Optimistic locking** via `@Version` on `stock_levels`, `reservations`, `orders`.

## 6. Test strategy

- **Unit tests** with Mockito. State-machine logic, pricing, tax. Target ratio: 1.5× production code for full services.
- **Integration tests** with Testcontainers Postgres + embedded Artemis. Full create→confirm→reserve flow per service.
- **Concurrent tests** for reservation race conditions.
- **Contract tests** via OpenAPI shape assertions (out of scope for stubbed services).
- **Failure-mode tests** for the SHIP-101 acceptance criterion: notification send failure does not roll back the shipment.

## 7. NFRs

None defined for the demo. Real production NFRs (latency, throughput, availability) would come from the customer engagement.

The demo is single-instance, single-broker, single-DB-per-service. No HA, no caching, no rate limiting. **Call this out explicitly so demo viewers don't expect production-grade ops.**

## 8. Forbidden patterns

- **Shared database** — every service owns its DB. Cross-service reads go via gateway.
- **Synchronous service-to-service HTTP** (except via gateway). Use events for async fan-out.
- **Business logic in controllers.** Controllers must be thin: validate, delegate, return DTO.
- **JPA entities in API responses.** Always map to DTO at the boundary.
- **Hibernate `@Filter` for tenant isolation.** Multi-tenancy is schema-level only for the demo (`tenant_id` columns); row-level isolation via Hibernate `@Filter` is deliberately out of scope (debug surface).
- **`double` / `float` for money or qty.** Always `BigDecimal`.
- **Modifying Flyway migrations after they're committed.** Always add a new V-migration.
- **Reusing iDempiere code.** Clean-room only.

## 9. Configuration

- All services read from `application.yml`. Environment-specific overrides via `SPRING_PROFILES_ACTIVE` (e.g., `docker`).
- Secrets and signing keys are checked into the repo for the demo (HS256 JWT key, hardcoded admin password). **Production would externalize via Vault, AWS Secrets Manager, etc.**

## 10. Build and run

```bash
# Maven multi-module build
mvn -B -DskipTests clean package

# Frontend
cd frontend && npm install && npm run build

# Run everything
docker compose up

# Smoke test
curl -X POST http://localhost:8080/auth/login -d '{"username":"admin","password":"admin"}' -H "Content-Type: application/json"
# → returns JWT

# Tail logs
docker compose logs -f orders-service inventory-service notifications-service
```
