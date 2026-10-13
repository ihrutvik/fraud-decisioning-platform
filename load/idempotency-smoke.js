import http from 'k6/http';
import { check } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const tenantId = __ENV.TENANT_ID || 'merchant-demo';
const apiKey = __ENV.API_KEY || 'demo-merchant-key';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: { checks: ['rate==1'] },
};

export default function () {
  const suffix = `${Date.now()}-${__VU}`;
  const key = `smoke-${suffix}`;
  const body = {
    transactionId: `smoke-tx-${suffix}`,
    accountId: 'smoke-account',
    deviceId: 'smoke-device',
    ipAddress: '203.0.113.41',
    amount: 499.00,
    currency: 'INR',
    billingCountry: 'IN',
    ipCountry: 'IN',
    cardPresent: true,
    accountAgeDays: 720,
  };
  const params = { headers: {
    'Content-Type': 'application/json',
    'X-Tenant-Id': tenantId,
    'X-API-Key': apiKey,
    'Idempotency-Key': key,
  }};

  const first = http.post(`${baseUrl}/v1/decisions`, JSON.stringify(body), params);
  const replay = http.post(`${baseUrl}/v1/decisions`, JSON.stringify(body), params);
  const conflict = http.post(`${baseUrl}/v1/decisions`, JSON.stringify({ ...body, amount: 500.00 }), params);

  check(first, {'initial request created': (r) => r.status === 201});
  check(replay, {
    'exact replay accepted': (r) => r.status === 201,
    'replay returns same decision': (r) => r.json('decisionId') === first.json('decisionId'),
    'replay is identified': (r) => r.json('replayed') === true,
  });
  check(conflict, {
    'mismatched replay rejected': (r) => r.status === 409,
    'conflict is structured': (r) => r.json('code') === 'IDEMPOTENCY_CONFLICT',
  });
}
