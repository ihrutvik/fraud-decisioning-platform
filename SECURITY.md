# Security policy

## Reporting a vulnerability

Do not disclose a suspected vulnerability in a public issue. Use GitHub's private vulnerability reporting for this repository and include the affected endpoint or component, reproduction steps, impact, and any suggested mitigation. No production credentials or customer data should be included.

This portfolio project does not operate a public production service or offer a paid bug bounty. Reports will still be assessed against the same priorities used for a production backend: authentication bypass, tenant isolation, replay or idempotency failures, credential exposure, injection, and unsafe dependency behavior.

## Supported version

Only the latest commit on `main` is supported. Dependency updates are proposed weekly for Maven, GitHub Actions, Docker, and Terraform. Every release candidate must pass unit tests, PostgreSQL migration/invariant tests, Terraform validation, Compose validation, k6 parsing, and the production container build.

## Security boundaries

- API keys are stored and compared as SHA-256 digests; raw keys belong in Secrets Manager.
- A credential is bound to one tenant and a least-privilege scope set.
- PostgreSQL, Redis, and application tasks remain on private subnets in the AWS reference architecture.
- Application logs and metrics must never include API keys, request bodies, account IDs, device IDs, or IP addresses.
- Kafka delivery is at least once; consumers must deduplicate using event IDs.
- The example MSK endpoints do not imply IAM authentication support. Do not use MSK IAM until the required SASL client has been implemented and tested.
