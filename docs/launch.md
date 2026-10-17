# Launch kit

## GitHub repository description

Production-minded Java 17 fraud decisioning platform with explainable rules, Redis velocity checks, Kafka outbox delivery, manual review, shadow models, verified feedback, OpenTelemetry, k6, and AWS Terraform.

## Suggested repository topics

`java` · `spring-boot` · `fraud-detection` · `distributed-systems` · `kafka` · `postgresql` · `redis` · `opentelemetry` · `terraform` · `aws`

## LinkedIn launch post

I’ve completed a new backend portfolio project: a Real-Time Fraud Decisioning Platform built with Java 17 and Spring Boot.

The interesting part was not the scoring formula. It was designing the production behavior around it:

- payload-aware idempotency for retried checkouts;
- explainable, versioned rules and immutable decision evidence;
- atomic Redis velocity windows with explicit outage handling;
- transactional Kafka outbox delivery with concurrent worker leasing;
- analyst review leases and immutable resolution history;
- shadow-model evaluation that cannot change production outcomes;
- verified feedback ingestion for later model validation;
- OpenTelemetry traces, Prometheus/Grafana SLOs, and k6 performance gates;
- PostgreSQL 16 integration tests and an AWS Fargate Terraform reference stack.

I also documented the trade-offs and limitations rather than presenting a demo as a finished regulated product. The repository includes a threat model, resilience runbook, release checklist, architecture decisions, executable API examples, and a five-minute engineering brief.

Repository: https://github.com/ihrutvik/fraud-decisioning-platform

#Java #SpringBoot #DistributedSystems #BackendEngineering #Kafka #PostgreSQL #Redis #AWS #OpenTelemetry

## Resume-ready project bullet

Built a production-minded Java 17 fraud decisioning platform with explainable versioned rules, distributed Redis velocity checks, payload-aware idempotency, transactional Kafka outbox delivery, analyst review workflows, shadow-model evaluation, verified feedback, OpenTelemetry/SLO dashboards, k6 performance gates, PostgreSQL integration tests, and AWS Terraform.
