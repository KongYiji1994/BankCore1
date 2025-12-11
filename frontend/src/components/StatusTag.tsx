import { Tag } from 'antd';
import type { PaymentStatus } from '../types';

const statusColor: Record<PaymentStatus, string> = {
  PENDING: 'default',
  INITIATED: 'default',
  IN_RISK_REVIEW: 'processing',
  RISK_REJECTED: 'error',
  RISK_APPROVED: 'cyan',
  CLEARING: 'gold',
  POSTED: 'green',
  FAILED: 'volcano',
};

export const StatusTag = ({ status }: { status: PaymentStatus }) => (
  <Tag color={statusColor[status]}>{status}</Tag>
);

export default StatusTag;
