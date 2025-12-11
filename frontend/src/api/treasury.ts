import { treasuryClient } from './client';
import type { CashPool, CashPoolInterestEntry } from '../types';

export interface CashPoolDefinition {
  poolId: string;
  headerAccount: string;
  memberAccounts: string[];
  targetBalance: number;
  strategy: string;
  poolType: string;
  interestRate: number;
}

export const registerPool = async (definition: CashPoolDefinition): Promise<CashPool> => {
  const { data } = await treasuryClient.post<CashPool>('/pools', definition);
  return data;
};

export const listPools = async (): Promise<CashPool[]> => {
  const { data } = await treasuryClient.get<CashPool[]>('/pools');
  return data;
};

export const sweepPool = async (poolId: string): Promise<CashPool> => {
  const { data } = await treasuryClient.post<CashPool>(`/pools/${poolId}/sweep`);
  return data;
};

export const accruePoolInterest = async (poolId: string): Promise<CashPoolInterestEntry> => {
  const { data } = await treasuryClient.post<CashPoolInterestEntry>(`/pools/${poolId}/accrue`);
  return data;
};
