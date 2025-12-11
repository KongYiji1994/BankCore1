import { paymentClient } from './client';
import type { PaymentBatchResult, PaymentInstruction } from '../types';

export interface PaymentRequest {
  requestId?: string;
  instructionId: string;
  payerAccount: string;
  payeeAccount: string;
  currency: string;
  amount: number;
  purpose: string;
  channel?: string;
  batchId?: string;
  priority?: number;
}

export const submitPayment = async (payload: PaymentRequest): Promise<PaymentInstruction> => {
  const requestPayload = { ...payload, requestId: payload.requestId || `REQ-${Date.now()}` };
  const { data } = await paymentClient.post<PaymentInstruction>('/payments', requestPayload);
  return data;
};

export const listPayments = async (): Promise<PaymentInstruction[]> => {
  const { data } = await paymentClient.get<PaymentInstruction[]>('/payments');
  return data;
};

export const processPayment = async (instructionId: string): Promise<PaymentInstruction> => {
  const { data } = await paymentClient.post<PaymentInstruction>(`/payments/${instructionId}/process`);
  return data;
};

export const approveRisk = async (instructionId: string): Promise<PaymentInstruction> => {
  const { data } = await paymentClient.post<PaymentInstruction>(`/payments/${instructionId}/risk-approve`);
  return data;
};

export const postPayment = async (instructionId: string): Promise<PaymentInstruction> => {
  const { data } = await paymentClient.post<PaymentInstruction>(`/payments/${instructionId}/post`);
  return data;
};

export const failPayment = async (instructionId: string): Promise<PaymentInstruction> => {
  const { data } = await paymentClient.post<PaymentInstruction>(`/payments/${instructionId}/fail`);
  return data;
};

export const processBatch = async (instructionIds: string[]): Promise<PaymentBatchResult> => {
  const { data } = await paymentClient.post<PaymentBatchResult>('/payments/batch/process', instructionIds);
  return data;
};
