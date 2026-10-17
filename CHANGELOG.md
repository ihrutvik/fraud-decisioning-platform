# Changelog

## 1.0.0 — 2026-10-17

### Decision platform

- Explainable `APPROVE`, `REVIEW`, and `DECLINE` outcomes with immutable evidence.
- Distributed Redis velocity windows and explicit outage behavior.
- Versioned tenant rules, safe activation, and reproducible decisions.
- Payload-aware idempotency and conflict detection.

### Reliable workflows

- Transactional Kafka outbox with horizontally scalable `SKIP LOCKED` leasing.
- Manual review cases with exclusive leases and immutable analyst history.
- Shadow-model evaluation with persisted comparison and disagreement metrics.
- Verified outcome feedback with idempotent, source-aware deduplication.

### Production readiness

- Tenant-bound API keys and least-privilege scopes.
- OpenTelemetry traces, Prometheus metrics and alerts, Grafana dashboards, and SLO runbooks.
- Redis circuit breaking with a single half-open recovery probe.
- k6 throughput and idempotency suites with executable performance budgets.
- PostgreSQL 16 integration tests for Flyway migrations and database invariants.
- Docker Compose local environment and Terraform AWS Fargate reference deployment.
- CI validation of Java, Terraform, Compose, k6, and the production image, with checksummed artifacts.
- Threat model, security policy, resilience guidance, and release checklist.
