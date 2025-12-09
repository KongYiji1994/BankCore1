export interface Account {
  accountId: string;
  customerId: string;
  currency: string;
  balance: number;
  availableBalance: number;
}

export type PaymentStatus =
  | 'INITIATED'
  | 'PENDING_RISK_APPROVAL'
  | 'RISK_REJECTED'
  | 'RISK_APPROVED'
  | 'PENDING_CLEARING'
  | 'POSTED'
  | 'FAILED';

export interface PaymentInstruction {
  instructionId: string;
  payerAccount: string;
  payeeAccount: string;
  currency: string;
  amount: number;
  purpose: string;
  channel?: string;
  batchId?: string;
  priority?: number;
  riskScore?: number;
  status: PaymentStatus;
  createdAt?: string;
}

export interface PaymentBatchResult {
  successes: number;
  riskRejected: number;
  failures: number;
  processedIds: string[];
}

export interface CashPool {
  poolId: string;
  headerAccount: string;
  memberAccounts: string[];
  targetBalance: number;
  strategy: string;
}
