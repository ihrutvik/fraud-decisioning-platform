# Threat model

## Assets and trust boundaries

The primary assets are tenant credentials, transaction evidence, fraud outcomes, analyst resolutions, verified labels, and rule/model versions. Requests cross the public load-balancer boundary, synchronous decisions cross PostgreSQL and Redis boundaries, and asynchronous events cross the Kafka boundary.

| Threat | Existing control | Residual risk / operator action |
|---|---|---|
| Credential theft | Only SHA-256 digests are stored; comparison is constant-time; AWS secrets are injected at runtime | Rotate exposed keys and review authentication failures; add managed identity before internet-facing production use |
| Cross-tenant access | Credential-bound tenant identity and scoped authorization | Keep tenant identifiers out of unbounded metric labels and validate every new repository query |
| Duplicate or changed retry | Tenant-scoped unique key plus canonical request fingerprint | Clients must retain keys for the complete retry window |
| Redis outage or latency | 500 ms timeout, fail-review/fail-open policy, local circuit breaker | Alert on sustained circuit rejection and choose the policy per risk appetite |
| Database/Kafka dual write | Transactional outbox and leased at-least-once relay | Consumers must deduplicate events; monitor unpublished age |
| Forged analyst action | Separate analyst scope, lease ownership, immutable audit history | Production should federate analyst identity through an IdP rather than shared API keys |
| Rule or model tampering | Immutable versions and explicit activation | Add four-eyes approval and signed model artifacts before regulated use |
| Sensitive telemetry | Bounded domain labels and sanitized correlation IDs | Log configuration changes require security review; never record raw transaction identity |
| Supply-chain drift | Immutable ECR tags, image scanning, weekly dependency updates, build checksums | Pin third-party actions by commit SHA in a regulated environment and sign images with organizational KMS |

## Abuse cases tested

- conflicting reuse of an idempotency key;
- cross-tenant credential/header mismatch;
- duplicate feedback and Kafka command delivery;
- review resolution without ownership;
- invalid database states and multiple active rule sets;
- Redis failure storms and failed half-open recovery probes.

The project intentionally avoids claims of PCI DSS compliance: it never requires or stores PAN/CVV data, and a real deployment would still need organizational policies, evidence, key management, network controls, and independent assessment.
