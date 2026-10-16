# Release checklist

## Build evidence

- [ ] `mvn --batch-mode verify` passes, including PostgreSQL 16 Testcontainers tests.
- [ ] Terraform initializes and validates without cloud credentials.
- [ ] Docker Compose and both k6 programs parse successfully.
- [ ] The production image builds from the same commit as the JAR.
- [ ] The JAR and `SHA256SUMS` artifact is retained by GitHub Actions.

## Runtime readiness

- [ ] Application secrets contain only SHA-256 API-key digests and have an owner/rotation date.
- [ ] RDS, Redis, Kafka, and OTLP endpoints are reachable only from approved private networks.
- [ ] Database backups and restore procedures have been exercised.
- [ ] Alert routes and dashboards have an on-call owner.
- [ ] The idempotency smoke test and a representative load profile pass in the target environment.
- [ ] Redis degradation policy is explicitly accepted by the fraud-risk owner.
- [ ] Kafka consumers demonstrate event-ID deduplication.

## Rollout and rollback

- [ ] Deploy one immutable image digest through a canary or minimum healthy ECS deployment.
- [ ] Watch availability, p95/p99 latency, review pressure, circuit rejections, and outbox age.
- [ ] Stop or roll back on SLO burn, database saturation, sustained circuit opening, or outbox growth.
- [ ] Record the image digest, database migration version, rule version, and change owner.
