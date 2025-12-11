import { useEffect, useMemo, useState } from 'react';
import { Card, Col, List, Row, Statistic, Tag, Typography, message } from 'antd';
import { listAccounts } from '../api/account';
import { listPayments } from '../api/payment';
import { listPools } from '../api/treasury';
import type { Account, CashPool, PaymentInstruction } from '../types';
import StatusTag from '../components/StatusTag';

const { Title, Paragraph } = Typography;

const riskStatuses = ['PENDING_RISK_APPROVAL', 'RISK_REJECTED'] as const;
const clearingStatuses = ['PENDING_CLEARING'] as const;

export const Dashboard = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [payments, setPayments] = useState<PaymentInstruction[]>([]);
  const [pools, setPools] = useState<CashPool[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [accountData, paymentData, poolData] = await Promise.all([
          listAccounts(),
          listPayments(),
          listPools(),
        ]);
        setAccounts(accountData);
        setPayments(paymentData);
        setPools(poolData);
      } catch (err) {
        console.error(err);
        message.error('Failed to load dashboard data. Please ensure backend services are running.');
      }
    };
    fetchData();
  }, []);

  const totals = useMemo(() => {
    const totalBalance = accounts.reduce((sum, a) => sum + Number(a.totalBalance || 0), 0);
    const available = accounts.reduce((sum, a) => sum + Number(a.availableBalance || 0), 0);
    const frozen = accounts.reduce((sum, a) => sum + Number(a.frozenBalance || 0), 0);
    return { totalBalance, available, frozen };
  }, [accounts]);

  const riskQueue = payments.filter((p) => riskStatuses.includes(p.status as any));
  const clearingQueue = payments.filter((p) => clearingStatuses.includes(p.status as any));

  return (
    <div style={{ padding: 24 }}>
      <Title level={3}>实时运营看板</Title>
      <Paragraph>
        结合后台 MyBatis + MySQL 的企业现金管理服务，前端工作台提供账户余额总览、支付风控/清算队列、现金池策略监控，便于面试展示端到端能力。
      </Paragraph>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card title="账户总览" bordered>
            <Statistic title="总余额" value={totals.totalBalance} precision={2} suffix="CNY" />
            <Statistic title="可用余额" value={totals.available} precision={2} suffix="CNY" style={{ marginTop: 12 }} />
            <Statistic title="冻结资金" value={totals.frozen} precision={2} suffix="CNY" style={{ marginTop: 12 }} />
            <Statistic title="账户数量" value={accounts.length} style={{ marginTop: 12 }} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card title="风控队列 (待审核/拒绝)" bordered>
            <List
              dataSource={riskQueue}
              locale={{ emptyText: '暂无待风控指令' }}
              renderItem={(item) => (
                <List.Item>
                  <div>
                    <div>{item.instructionId}</div>
                    <div>
                      {item.payerAccount} → {item.payeeAccount} {item.amount} {item.currency}
                    </div>
                  </div>
                  <StatusTag status={item.status} />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card title="清算队列" bordered>
            <List
              dataSource={clearingQueue}
              locale={{ emptyText: '暂无待清算指令' }}
              renderItem={(item) => (
                <List.Item>
                  <div>
                    <div>{item.instructionId}</div>
                    <div>渠道: {item.channel || '直联'}, 优先级: {item.priority || 5}</div>
                  </div>
                  <Tag color="blue">金额 {item.amount} {item.currency}</Tag>
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} md={12}>
          <Card title="现金池策略" bordered>
            <List
              dataSource={pools}
              locale={{ emptyText: '暂无现金池策略' }}
              renderItem={(pool) => (
                <List.Item>
                  <div>
                    <div>
                      <strong>{pool.poolId}</strong> · Header: {pool.headerAccount}
                    </div>
                    <div>成员: {pool.memberAccounts.join(', ') || 'N/A'}</div>
                    <div>
                      策略: {pool.strategy}, 目标余额: {pool.targetBalance}
                    </div>
                  </div>
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card title="通道/批次监控" bordered>
            <List
              dataSource={payments.slice(0, 8)}
              locale={{ emptyText: '暂无支付流水' }}
              renderItem={(pay) => (
                <List.Item>
                  <div>
                    <div>{pay.instructionId}</div>
                    <div>批次: {pay.batchId || '单笔'}, 渠道: {pay.channel || 'N/A'}</div>
                    <div>
                      {pay.payerAccount} → {pay.payeeAccount}
                    </div>
                  </div>
                  <StatusTag status={pay.status} />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
