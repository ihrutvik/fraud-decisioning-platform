# Redis velocity resilience

Velocity signals improve fraud detection, but Redis must not be allowed to exhaust the synchronous checkout latency budget during an outage. The service therefore combines a short Redis client timeout, an explicit degradation policy, and a local circuit breaker.

## State model

- `CLOSED`: every request may call Redis; consecutive connection failures are counted.
- `OPEN`: Redis calls are skipped and the configured `FAIL_REVIEW` or `FAIL_OPEN` snapshot is returned immediately.
- `HALF_OPEN`: after the recovery delay, one request probes Redis. All other concurrent requests continue using the degradation policy.
- A successful probe closes the circuit. A failed probe starts a new open interval.

## Configuration

| Environment variable | Default | Purpose |
|---|---:|---|
| `VELOCITY_FAILURE_POLICY` | `FAIL_REVIEW` | Safe outcome when velocity is unavailable |
| `VELOCITY_CIRCUIT_FAILURE_THRESHOLD` | `3` | Consecutive failed operations before opening |
| `VELOCITY_CIRCUIT_OPEN_SECONDS` | `30` | Delay before the single recovery probe |

Each application instance owns its circuit. This deliberately avoids making circuit coordination depend on Redis itself. Instances naturally stagger probes, while the 500 ms Redis timeout bounds an individual failed call.

## Metrics and operations

- `fraud_velocity_circuit_rejections_total` counts decisions protected from a known-unhealthy Redis dependency.
- `fraud_velocity_circuit_transitions_total{state=...}` records transitions to open, half-open, and closed states.

Alert on sustained rejection growth, not isolated transitions. A short open interval is normal during a Redis failover; sustained open/half-open cycling indicates continued dependency failure or insufficient Redis capacity.
