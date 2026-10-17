# Contributing

## Local workflow

1. Use Java 17 and a Docker-compatible container runtime.
2. Run `mvn --batch-mode verify` before submitting a change.
3. Run `docker compose config --quiet` for Compose changes.
4. Run `terraform -chdir=deploy/aws init -backend=false` and `terraform -chdir=deploy/aws validate` for infrastructure changes.
5. Keep API examples, architecture decisions, and operational documentation aligned with behavior.

## Design rules

- Preserve tenant isolation in repository queries, uniqueness constraints, metrics, and logs.
- Treat idempotency as a payload-aware contract; key reuse with different data must fail.
- Persist state and outgoing events atomically through the outbox.
- Keep metric labels bounded and exclude customer identifiers.
- Add a Flyway migration for every schema change; never edit an applied migration.
- Add failure-path tests for dependency outages, duplicate delivery, concurrency, and invalid transitions.
- Do not introduce claims of production scale, compliance, or benchmark performance without reproducible evidence.

Report vulnerabilities through the process in [SECURITY.md](SECURITY.md), not through a public issue.
