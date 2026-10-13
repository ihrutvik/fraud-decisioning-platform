import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const tenantId = __ENV.TENANT_ID || 'merchant-demo';
const apiKey = __ENV.API_KEY || 'demo-merchant-key';

export const options = {
  scenarios: {
    decision_traffic: {
      executor: 'ramping-arrival-rate',
      startRate: 10,
      timeUnit: '1s',
      preAllocatedVUs: 30,
      maxVUs: 200,
      stages: [
        { target: 50, duration: '1m' },
        { target: 100, duration: '2m' },
        { target: 0, duration: '30s' },
      ],
      exec: 'createDecision',
    },
  },
  thresholds: {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.005'],
    http_req_duration: ['p(95)<200', 'p(99)<500'],
    dropped_iterations: ['count<10'],
  },
};

function requestBody(sequence) {
  return JSON.stringify({
    transactionId: `load-tx-${sequence}`,
    accountId: `load-account-${sequence % 10000}`,
    deviceId: `load-device-${sequence % 20000}`,
    ipAddress: `198.51.${sequence % 255}.${Math.floor(sequence / 255) % 255}`,
    amount: 1499.00,
    currency: 'INR',
    billingCountry: 'IN',
    ipCountry: 'IN',
    cardPresent: true,
    accountAgeDays: 720,
  });
}

export function createDecision() {
  const sequence = `${Date.now()}-${__VU}-${__ITER}`;
  const response = http.post(`${baseUrl}/v1/decisions`, requestBody(sequence), {
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Id': tenantId,
      'X-API-Key': apiKey,
      'Idempotency-Key': `load-${sequence}`,
    },
    tags: { operation: 'create-decision' },
  });

  check(response, {
    'decision created': (result) => result.status === 201,
    'decision is explainable': (result) => {
      const body = result.json();
      return body && body.decisionId && body.outcome && Array.isArray(body.reasonCodes);
    },
  });
}
