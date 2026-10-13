# Performance and capacity testing

The repository keeps correctness and throughput tests separate. `idempotency-smoke.js` proves the write API preserves exactly-once client semantics under retries. `decision-load.js` generates bounded-cardinality production-like traffic and enforces an explicit latency/error budget.

## Run the correctness smoke test

```bash
mvn -DskipTests package
docker compose up --build -d postgres kafka redis jaeger app
docker compose --profile load run --rm k6 run /scripts/idempotency-smoke.js
docker compose --profile load down --volumes
```

The smoke test creates one decision, repeats the identical request, and then reuses the same key with a different amount. It requires the replay to return the original decision ID and the conflict to return `409 IDEMPOTENCY_CONFLICT`.

## Run the throughput profile

```bash
docker compose up --build -d postgres kafka redis jaeger app
docker compose --profile load run --rm k6
```

The default profile ramps from 10 to 100 decision requests per second. Its service-level gates are:

- fewer than 0.5% failed HTTP requests;
- p95 latency below 200 ms and p99 below 500 ms;
- more than 99% successful response checks;
- fewer than 10 dropped iterations, which reveals inadequate virtual-user capacity.

These are regression gates for the local reference topology, not universal production promises. Capture CPU, database connections, Redis latency, outbox backlog, and Kafka publish latency alongside each run before choosing production instance sizes.

## CI strategy

The normal CI workflow parses both k6 programs on every change. The manually triggered `performance-smoke` workflow boots the complete decision path and executes the idempotency test. Full load is intentionally opt-in so pull-request checks stay deterministic and do not mistake shared-runner contention for an application regression.
