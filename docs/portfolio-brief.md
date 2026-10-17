# Five-minute engineering brief

## The problem

A checkout needs a fraud answer quickly, but that answer must also be explainable, retry-safe, tenant-isolated, reviewable by analysts, and available to downstream analytics. Redis, Kafka, or an unproven candidate model must not silently corrupt the customer-facing result.

## The core design

The synchronous path authenticates the tenant, fingerprints the request, records account/device/IP velocity in Redis, evaluates the active versioned rules, evaluates a candidate model in shadow mode, and atomically commits the decision evidence plus its outbox event in PostgreSQL. Review decisions create an analyst case in the same transaction.

A separate relay leases unpublished outbox rows with `FOR UPDATE SKIP LOCKED` and publishes them to Kafka. Delivery is at least once, so event IDs are stable and consumers are expected to deduplicate. Verified outcomes arrive later as immutable labels for validation and training pipelines.

## Three decisions worth discussing

1. **PostgreSQL outbox instead of a database/Kafka dual write.** Losing an event is worse than delivering one twice; the design makes duplication explicit and recoverable.
2. **Explainable versioned rules plus shadow evaluation.** Customer outcomes remain deterministic while a candidate can accumulate real comparison evidence before promotion.
3. **Redis degradation as a product decision.** A short timeout and circuit breaker protect latency, while `FAIL_REVIEW` or `FAIL_OPEN` makes the risk/availability trade-off configurable rather than accidental.

## Failure behavior

- Identical retries return the original decision; changed payloads return `409`.
- Redis failures follow the configured degradation policy and open a local circuit after repeated failures.
- Kafka outages leave outbox rows unpublished for later retry.
- Abandoned review claims expire and can be safely reassigned.
- Duplicate feedback, review resolution, and shadow evaluation are constrained in PostgreSQL.

## Evidence in the repository

- Unit tests for risk, rules, review, feedback, security, tracing, metrics, and circuit behavior.
- PostgreSQL 16 tests for migrations, idempotency, active-rule uniqueness, and review constraints.
- k6 latency/error budgets and idempotency correctness checks.
- Prometheus/Grafana operations assets and OpenTelemetry tracing.
- Terraform for private multi-AZ AWS infrastructure with rollback and autoscaling.

## Honest limitations

This is a portfolio reference, not a hosted regulated service. It does not claim PCI DSS compliance, verified production throughput, or Amazon MSK IAM support. A production rollout would add managed workforce identity, signed model artifacts, four-eyes rule approval, consumer implementations, restore drills, and organization-specific network and key policies.
