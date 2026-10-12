# Service-level objectives and alerts

| Signal | Objective | Alert |
|---|---:|---|
| API availability | 99.5% non-5xx responses over the rolling window | 5xx ratio above 0.5% for 5 minutes |
| Decision latency | p95 below 200 ms | p95 above 200 ms for 10 minutes |
| Event delivery | Outbox remains near zero | More than 100 unpublished events for 5 minutes |
| Analyst capacity | Open queue below 100 | More than 100 open cases for 15 minutes |
| Candidate stability | Disagreement below 10% | Above 10% for 30 minutes |

Prometheus evaluates the rules in `observability/alerts.yml`. Grafana is provisioned at `http://localhost:3000` with the Fraud Decisioning Platform dashboard; Prometheus is available at `http://localhost:9090`.

These thresholds are operational starting points, not universal constants. Production owners should tune them from actual traffic, fraud-loss tolerance, analyst staffing, and error-budget policy. Alerts intentionally avoid high-cardinality tenant or customer labels.
