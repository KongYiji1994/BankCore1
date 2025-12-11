export interface Account {
  accountId: string;
  customerId: string;
  currency: string;
  totalBalance: number;
  availableBalance: number;
  frozenBalance: number;
  status: string;
}

export type PaymentStatus =
  | 'PENDING'
  | 'INITIATED'
  | 'IN_RISK_REVIEW'
  | 'RISK_REJECTED'
  | 'RISK_APPROVED'
  | 'CLEARING'
  | 'POSTED'
  | 'FAILED';

export interface PaymentInstruction {
  requestId: string;
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
  total: number;
  succeeded: number;
  rejected: number;
  failed: number;
  failedIds: string[];
}

export interface CashPool {
  poolId: string;
  headerAccount: string;
  memberAccounts: string[];
  targetBalance: number;
  strategy: string;
  poolType: string;
  interestRate: number;
  lastInterestDate?: string;
}

export interface CashPoolInterestEntry {
  id?: number;
  poolId: string;
  headerAccount: string;
  interestAmount: number;
  rate: number;
  accrualDate: string;
  description?: string;
}
