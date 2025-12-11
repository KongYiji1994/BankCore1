import { accountClient } from './client';
import type { Account } from '../types';

export const listAccounts = async (): Promise<Account[]> => {
  const { data } = await accountClient.get<Account[]>('/accounts');
  return data;
};

export const createAccount = async (
  customerId: string,
  currency: string,
  openingBalance: number
): Promise<Account> => {
  const { data } = await accountClient.post<Account>('/accounts', null, {
    params: { customerId, currency, openingBalance },
  });
  return data;
};

export const creditAccount = async (accountId: string, amount: number): Promise<Account> => {
  const { data } = await accountClient.post<Account>(`/accounts/${accountId}/credit`, null, {
    params: { amount },
  });
  return data;
};

export const debitAccount = async (accountId: string, amount: number): Promise<Account> => {
  const { data } = await accountClient.post<Account>(`/accounts/${accountId}/debit`, null, {
    params: { amount },
  });
  return data;
};

export const settleAccount = async (accountId: string, amount: number): Promise<Account> => {
  const { data } = await accountClient.post<Account>(`/accounts/${accountId}/settle`, null, {
    params: { amount },
  });
  return data;
};

export const unfreezeAccount = async (accountId: string, amount: number): Promise<Account> => {
  const { data } = await accountClient.post<Account>(`/accounts/${accountId}/unfreeze`, null, {
    params: { amount },
  });
  return data;
};
