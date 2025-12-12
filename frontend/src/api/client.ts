import axios from 'axios';

const createClient = (baseUrl: string | undefined, fallback: string) =>
  axios.create({
    baseURL: baseUrl || fallback,
    headers: { 'Content-Type': 'application/json' },
    timeout: 10000,
  });

export const accountClient = createClient(
  import.meta.env.VITE_ACCOUNT_BASE_URL,
  'http://localhost:8081'
);

export const paymentClient = createClient(
  import.meta.env.VITE_PAYMENT_BASE_URL,
  'http://localhost:8083'
);

export const treasuryClient = createClient(
  import.meta.env.VITE_TREASURY_BASE_URL,
  'http://localhost:8084'
);
