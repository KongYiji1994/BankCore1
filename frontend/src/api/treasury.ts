import { treasuryClient } from './client';
import type { CashPool } from '../types';

export interface CashPoolDefinition {
  poolId: string;
  headerAccount: string;
  memberAccounts: string[];
  targetBalance: number;
  strategy: string;
}

export const registerPool = async (definition: CashPoolDefinition): Promise<CashPool> => {
  const { data } = await treasuryClient.post<CashPool>('/pools', definition);
  return data;
};

export const listPools = async (): Promise<CashPool[]> => {
  const { data } = await treasuryClient.get<CashPool[]>('/pools');
  return data;
};

export const sweepPool = async (poolId: string, headerBalance: number): Promise<CashPool> => {
  const { data } = await treasuryClient.post<CashPool>(`/pools/${poolId}/sweep`, null, {
    params: { headerBalance },
  });
  return data;
};
