import { Tag } from 'antd';
import type { PaymentStatus } from '../types';

const statusColor: Record<PaymentStatus, string> = {
  INITIATED: 'default',
  PENDING_RISK_APPROVAL: 'processing',
  RISK_REJECTED: 'error',
  RISK_APPROVED: 'cyan',
  PENDING_CLEARING: 'gold',
  POSTED: 'green',
  FAILED: 'volcano',
};

export const StatusTag = ({ status }: { status: PaymentStatus }) => (
  <Tag color={statusColor[status]}>{status}</Tag>
);

export default StatusTag;
