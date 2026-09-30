import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
  stages: [
    { duration: '10s', target: 20 }, // Ramp up to 20 users
    { duration: '30s', target: 50 }, // Spike to 50 concurrent users
    { duration: '10s', target: 0 },  // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.01'],   // Less than 1% failure rate for API ingress
  },
};

const BASE_URL = 'http://localhost:8083'; // Order Service API

export default function () {
  // 1. Fetch available products (mocking the catalog API call from frontend)
  // We'll hardcode a known product UUID for the load test or assume the UI already has it.
  const productId = '11111111-1111-1111-1111-111111111111'; // Needs to exist in DB
  const customerId = uuidv4();

  // 2. Place an order
  const payload = JSON.stringify({
    customerId: customerId,
    items: [
      {
        productId: productId,
        quantity: 1,
        price: 99.99
      }
    ]
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(`${BASE_URL}/api/orders`, payload, params);

  check(res, {
    'Order created successfully (200/201)': (r) => r.status === 200 || r.status === 201,
    'Order is in PENDING state initially': (r) => {
      if (r.status !== 200 && r.status !== 201) return false;
      const body = JSON.parse(r.body);
      return body.status === 'PENDING';
    }
  });

  // Optional: Poll to see if saga completes (Eventually Consistent)
  // Real clients might use WebSockets, but we'll do short polling here to measure Saga completion time.
  /*
  let sagaCompleted = false;
  let attempts = 0;
  if (res.status === 200 || res.status === 201) {
    const orderId = JSON.parse(res.body).id;
    while (!sagaCompleted && attempts < 10) {
      sleep(1);
      const checkRes = http.get(`${BASE_URL}/api/orders/${orderId}`);
      if (checkRes.status === 200) {
        const status = JSON.parse(checkRes.body).status;
        if (status === 'CONFIRMED' || status === 'CANCELLED') {
          sagaCompleted = true;
        }
      }
      attempts++;
    }
  }
  check(sagaCompleted, { 'Saga completed within 10 seconds': (s) => s === true });
  */
  
  sleep(1);
}
