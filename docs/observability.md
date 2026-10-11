# Observability runbook

## Local trace inspection

1. Run `docker compose up --build`.
2. Submit a request from `docs/api-examples.http`, optionally supplying `X-Correlation-Id`.
3. Open Jaeger at `http://localhost:16686` and select `fraud-decisioning-platform`.
4. Search by operation (`evaluate-fraud-decision`, `ingest-fraud-feedback`, or `publish-fraud-events`) and correlate it with the `traceId`, `spanId`, and `correlationId` fields in application logs.

HTTP instrumentation creates the root span. Observed domain methods create child spans, and the Kafka template injects W3C trace context into producer records. The scheduled outbox relay creates a new producer trace when publication happens after the originating request transaction.

## Production controls

- Set `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` to the collector's HTTPS trace endpoint.
- Set `TRACING_SAMPLING_PROBABILITY` below `1.0` for high-volume production traffic; start with `0.1` and tune against incident and cost requirements.
- Do not put API keys, raw payment data, IP addresses, or customer identifiers in span tags.
- Keep `X-Correlation-Id` within the enforced safe character set and 64-character limit.
- Alert on exporter failures separately from the decision path: telemetry loss must not block fraud decisions.
