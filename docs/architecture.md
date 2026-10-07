# Architecture decision record

## Decision path

1. A tenant submits a transaction with an idempotency key.
2. The service canonicalizes and fingerprints the request.
3. Atomic Redis sliding windows count account, device, and IP attempts across every service instance.
4. The active tenant rule-set version combines request and velocity signals into a bounded score, outcome, and reason codes.
5. Decision evidence, its outbox event, and any required review case commit in one PostgreSQL transaction.
6. Analysts claim review cases through expiring leases; only the lease owner can resolve them.
7. Resolution audit evidence and a follow-up outbox event commit atomically.
8. A horizontally scalable relay leases unpublished events with `SKIP LOCKED` and publishes them to Kafka.

## Why this shape

- **Synchronous decision, asynchronous distribution:** checkout receives an immediate answer while analytics and review systems remain decoupled.
- **Explainability first:** every score contribution has a stable reason code; this is intentionally more auditable than an opaque model placeholder.
- **PostgreSQL as decision ledger:** constraints enforce tenant-scoped idempotency and retain the exact evidence returned to the caller.
- **At-least-once events:** the outbox eliminates the database/Kafka dual-write gap. Consumers must deduplicate by event or decision ID.
- **Safe horizontal scaling:** `FOR UPDATE SKIP LOCKED` partitions relay work without a separate coordinator.
- **Exact velocity windows:** a Lua script atomically evicts expired observations, records the request, refreshes TTL, and counts the remaining set.
- **Deliberate degradation:** Redis outages use an explicit `FAIL_REVIEW` or `FAIL_OPEN` policy instead of an accidental availability/security trade-off.
- **Immutable rule governance:** changes create a new draft version. Activation retires the previous version under a database lock and a partial unique index guarantees one active version per tenant.
- **Reproducible decisions:** every ledger row records the rule-set version that generated its outcome; tenants without overrides inherit the default active rule set.
- **Safe human decisions:** pessimistic row locking serializes claims and resolutions, leases recover abandoned work, and immutable audit events retain the actor for every transition.

## Evolution plan

Subsequent increments will add model shadowing, feedback ingestion, security, tracing, load tests, and AWS/EKS deployment assets.
