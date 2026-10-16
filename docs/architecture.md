# Architecture decision record

## Decision path

1. A tenant submits a transaction with an idempotency key.
2. A constant-time API-key check binds the caller to one tenant and an explicit set of endpoint scopes.
3. The service canonicalizes and fingerprints the request.
4. Atomic Redis sliding windows count account, device, and IP attempts across every service instance.
5. The active tenant rule-set version combines request and velocity signals into a bounded score, outcome, and reason codes.
6. The candidate model evaluates the same evidence in shadow mode but cannot change the customer-facing outcome.
7. Decision evidence, shadow evidence, its outbox event, and any required review case commit in one PostgreSQL transaction.
8. Analysts claim review cases through expiring leases; only the lease owner can resolve them.
9. Resolution audit evidence and a follow-up outbox event commit atomically.
10. A horizontally scalable relay leases unpublished events with `SKIP LOCKED` and publishes them to Kafka.
11. Delayed real-world outcomes are ingested as immutable labels and emitted for offline validation and training.

## Why this shape

- **Synchronous decision, asynchronous distribution:** checkout receives an immediate answer while analytics and review systems remain decoupled.
- **Explainability first:** every score contribution has a stable reason code; this is intentionally more auditable than an opaque model placeholder.
- **PostgreSQL as decision ledger:** constraints enforce tenant-scoped idempotency and retain the exact evidence returned to the caller.
- **At-least-once events:** the outbox eliminates the database/Kafka dual-write gap. Consumers must deduplicate by event or decision ID.
- **Safe horizontal scaling:** `FOR UPDATE SKIP LOCKED` partitions relay work without a separate coordinator.
- **Exact velocity windows:** a Lua script atomically evicts expired observations, records the request, refreshes TTL, and counts the remaining set.
- **Deliberate degradation:** Redis outages use an explicit `FAIL_REVIEW` or `FAIL_OPEN` policy instead of an accidental availability/security trade-off.
- **Failure isolation:** repeated Redis connection failures open a local circuit before the dependency can consume the checkout latency budget. After a configurable recovery window, exactly one half-open probe tests recovery; concurrent requests continue through the selected degradation policy.
- **Immutable rule governance:** changes create a new draft version. Activation retires the previous version under a database lock and a partial unique index guarantees one active version per tenant.
- **Reproducible decisions:** every ledger row records the rule-set version that generated its outcome; tenants without overrides inherit the default active rule set.
- **Safe human decisions:** pessimistic row locking serializes claims and resolutions, leases recover abandoned work, and immutable audit events retain the actor for every transition.
- **Safe model rollout:** candidate scores are versioned and persisted beside the production outcome. Disagreement metrics provide evidence for offline validation without exposing customers to an unproven model.
- **Point-in-time feedback:** labels reference immutable decisions instead of mutating them. Tenant idempotency keys and source references prevent duplicate chargebacks or analyst outcomes from corrupting training data.
- **Identity before tenancy:** callers cannot select an arbitrary tenant with a header. Stored credentials contain only SHA-256 digests, comparisons are constant-time, and each credential is bound to one tenant and explicit scopes.
- **Trace asynchronous work:** HTTP spans continue through observed domain operations and Kafka producer headers. Correlation IDs remain a separate, sanitized operator-facing identifier while W3C trace context supplies causal linkage.
- **SLOs over vanity metrics:** alerts measure customer availability, decision latency, delivery backlog, analyst pressure, and model disagreement. Metric labels remain bounded to prevent tenant or customer cardinality explosions.
- **Performance as a contract:** k6 thresholds turn latency, error rate, and dropped work into executable release evidence. A separate smoke profile continuously protects the API's idempotency contract without making normal CI depend on noisy shared-runner benchmarks.
- **Two Kafka network planes:** containers receive broker metadata for `kafka:29092` while host tools receive `localhost:9092`, avoiding the common advertised-listener failure where a client connects successfully and then receives an unreachable broker address.
- **AWS failure-domain separation:** the reference deployment spreads Fargate tasks and stateful services across availability zones, keeps compute and data stores private, terminates TLS at the ALB, and rolls back unhealthy task revisions automatically.
- **Infrastructure ownership boundaries:** the service stack consumes an existing VPC and Kafka endpoints. Network topology and MSK authentication remain platform-level concerns instead of being recreated or weakened inside each application repository.
- **Database invariants are executable:** PostgreSQL 16 integration tests run every Flyway migration and prove tenant idempotency, single-active-rule enforcement, and review evidence constraints against the actual database engine.
- **Traceable release artifacts:** CI labels the container with the source revision and publishes the executable JAR with its SHA-256 checksum, linking deployable bytes back to a tested commit.

## Evolution plan

The final increment packages the complete recruiter-facing launch release.
