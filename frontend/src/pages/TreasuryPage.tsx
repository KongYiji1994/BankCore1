import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Modal,
  Row,
  Select,
  Table,
  Tag,
  message,
} from 'antd';
import { CashPoolDefinition, listPools, registerPool, sweepPool } from '../api/treasury';
import type { CashPool } from '../types';

interface SweepModalState {
  open: boolean;
  poolId?: string;
  headerBalance?: number;
}

const strategies = [
  { label: 'TARGET_BALANCE', value: 'TARGET_BALANCE' },
  { label: 'ZERO_BALANCE', value: 'ZERO_BALANCE' },
  { label: 'NOTIONAL_POOL', value: 'NOTIONAL_POOL' },
];

export const TreasuryPage = () => {
  const [pools, setPools] = useState<CashPool[]>([]);
  const [loading, setLoading] = useState(false);
  const [sweepModal, setSweepModal] = useState<SweepModalState>({ open: false });

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listPools();
      setPools(data);
    } catch (err) {
      console.error(err);
      message.error('加载现金池失败，请确认 treasury-service 已启动。');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const onCreate = async (definition: CashPoolDefinition) => {
    try {
      await registerPool({ ...definition, memberAccounts: definition.memberAccounts.filter(Boolean) });
      message.success('现金池已注册');
      refresh();
    } catch (err) {
      console.error(err);
      message.error('注册失败');
    }
  };

  const onSweep = async () => {
    if (!sweepModal.poolId || sweepModal.headerBalance === undefined) {
      message.warning('请输入 header 账户余额');
      return;
    }
    try {
      await sweepPool(sweepModal.poolId, sweepModal.headerBalance);
      message.success('手工归集/下拨完成');
      refresh();
    } catch (err) {
      console.error(err);
      message.error('归集失败');
    } finally {
      setSweepModal({ open: false });
    }
  };

  const columns = [
    { title: 'Pool ID', dataIndex: 'poolId' },
    { title: 'Header 账户', dataIndex: 'headerAccount' },
    {
      title: '成员账户',
      render: (_: unknown, record: CashPool) => (
        <span>{record.memberAccounts && record.memberAccounts.length ? record.memberAccounts.join(', ') : 'N/A'}</span>
      ),
    },
    { title: '目标余额', dataIndex: 'targetBalance' },
    {
      title: '策略',
      dataIndex: 'strategy',
      render: (value: string) => <Tag color="blue">{value}</Tag>,
    },
    {
      title: '操作',
      render: (_: unknown, record: CashPool) => (
        <Button onClick={() => setSweepModal({ open: true, poolId: record.poolId })} size="small" type="primary">
          手动归集
        </Button>
      ),
    },
  ];

  return (
    <Row gutter={16}>
      <Col xs={24} md={10}>
        <Card title="配置现金池" bordered>
          <Form layout="vertical" onFinish={onCreate} initialValues={{ strategy: 'TARGET_BALANCE' }}>
            <Form.Item name="poolId" label="Pool ID" rules={[{ required: true, message: '请输入唯一标识' }]}>
              <Input placeholder="POOL-001" />
            </Form.Item>
            <Form.Item name="headerAccount" label="Header 账户" rules={[{ required: true, message: '请输入 header 账户' }]}>
              <Input placeholder="如：6222000001" />
            </Form.Item>
            <Form.Item name="memberAccounts" label="成员账户" rules={[{ required: true, message: '请输入成员账户' }]}>
              <Select mode="tags" tokenSeparators={[',']} placeholder="输入成员账户，回车确认" />
            </Form.Item>
            <Form.Item name="targetBalance" label="Header 目标余额" rules={[{ required: true }]}>
              <InputNumber min={0} precision={2} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="strategy" label="归集策略" rules={[{ required: true }]}>
              <Select options={strategies} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block>
                创建策略
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Col>
      <Col xs={24} md={14}>
        <Card title="现金池列表" bordered>
          <Table
            columns={columns}
            dataSource={pools}
            rowKey="poolId"
            loading={loading}
            size="small"
            pagination={{ pageSize: 5 }}
          />
        </Card>
      </Col>

      <Modal
        title="手动归集/下拨"
        open={sweepModal.open}
        onOk={onSweep}
        onCancel={() => setSweepModal({ open: false })}
        okText="执行"
        cancelText="取消"
        destroyOnClose
      >
        <InputNumber
          autoFocus
          min={0}
          precision={2}
          style={{ width: '100%' }}
          placeholder="Header 当前余额"
          value={sweepModal.headerBalance}
          onChange={(val) => setSweepModal((prev) => ({ ...prev, headerBalance: Number(val) }))}
        />
      </Modal>
    </Row>
  );
};

export default TreasuryPage;
