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
  message,
} from 'antd';
import { createAccount, creditAccount, debitAccount, listAccounts, settleAccount, unfreezeAccount } from '../api/account';
import type { Account } from '../types';

interface ActionModalState {
  open: boolean;
  type: 'credit' | 'debit' | 'settle' | 'unfreeze';
  accountId?: string;
  amount?: number;
}

const currencyOptions = [
  { label: 'CNY', value: 'CNY' },
  { label: 'USD', value: 'USD' },
  { label: 'EUR', value: 'EUR' },
];

export const AccountsPage = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionModal, setActionModal] = useState<ActionModalState>({ open: false, type: 'credit' });

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listAccounts();
      setAccounts(data);
    } catch (err) {
      console.error(err);
      message.error('加载账户列表失败，请确认 account-service 已启动。');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const onCreate = async (values: { customerId: string; currency: string; openingBalance: number }) => {
    try {
      await createAccount(values.customerId, values.currency, values.openingBalance || 0);
      message.success('账户创建成功');
      refresh();
    } catch (err) {
      console.error(err);
      message.error('创建账户失败');
    }
  };

  const onConfirmAction = async () => {
    if (!actionModal.accountId || !actionModal.amount || actionModal.amount <= 0) {
      message.warning('请输入大于 0 的金额');
      return;
    }
    try {
      if (actionModal.type === 'credit') {
        await creditAccount(actionModal.accountId, actionModal.amount);
        message.success('入账成功');
      } else if (actionModal.type === 'settle') {
        await settleAccount(actionModal.accountId, actionModal.amount);
        message.success('清算扣减成功');
      } else if (actionModal.type === 'unfreeze') {
        await unfreezeAccount(actionModal.accountId, actionModal.amount);
        message.success('解冻成功');
      } else {
        await debitAccount(actionModal.accountId, actionModal.amount);
        message.success('冻结资金成功');
      }
      refresh();
    } catch (err) {
      console.error(err);
      message.error('交易失败，请检查余额或服务状态');
    } finally {
      setActionModal({ open: false, type: 'credit', amount: 0 });
    }
  };

  const columns = [
    { title: '账户', dataIndex: 'accountId' },
    { title: '客户号', dataIndex: 'customerId' },
    { title: '币种', dataIndex: 'currency' },
    { title: '状态', dataIndex: 'status' },
    {
      title: '总余额 / 可用 / 冻结',
      render: (_: unknown, record: Account) =>
        `${record.totalBalance} / ${record.availableBalance} / ${record.frozenBalance}`,
    },
    {
      title: '操作',
      render: (_: unknown, record: Account) => (
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          <Button size="small" onClick={() => setActionModal({ open: true, type: 'credit', accountId: record.accountId })}>
            入账
          </Button>
          <Button size="small" onClick={() => setActionModal({ open: true, type: 'debit', accountId: record.accountId })}>
            付款冻结
          </Button>
          <Button
            danger
            size="small"
            onClick={() => setActionModal({ open: true, type: 'settle', accountId: record.accountId })}
          >
            清算扣减
          </Button>
          <Button
            size="small"
            onClick={() => setActionModal({ open: true, type: 'unfreeze', accountId: record.accountId })}
          >
            取消/解冻
          </Button>
        </div>
      ),
    },
  ];

  return (
    <Row gutter={16}>
      <Col xs={24} md={10}>
        <Card title="新建企业结算户" bordered>
          <Form layout="vertical" onFinish={onCreate} initialValues={{ currency: 'CNY', openingBalance: 0 }}>
            <Form.Item name="customerId" label="客户号" rules={[{ required: true, message: '请输入客户号' }]}>
              <Input placeholder="如：CUST10001" allowClear />
            </Form.Item>
            <Form.Item name="currency" label="币种" rules={[{ required: true }]}>
              <Select options={currencyOptions} />
            </Form.Item>
            <Form.Item name="openingBalance" label="初始余额">
              <InputNumber min={0} style={{ width: '100%' }} precision={2} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlFor="submit" htmlType="submit" block>
                创建账户
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Col>
      <Col xs={24} md={14}>
        <Card title="账户列表" bordered>
          <Table
            columns={columns}
            dataSource={accounts}
            rowKey="accountId"
            loading={loading}
            pagination={{ pageSize: 5 }}
            size="small"
          />
        </Card>
      </Col>

      <Modal
        title={{
          credit: '账户入账',
          debit: '付款冻结',
          settle: '清算扣减',
          unfreeze: '取消/解冻',
        }[actionModal.type]}
        open={actionModal.open}
        onOk={onConfirmAction}
        onCancel={() => setActionModal({ open: false, type: 'credit' })}
        okText="确认"
        cancelText="取消"
        destroyOnClose
      >
        <InputNumber
          autoFocus
          min={0}
          precision={2}
          style={{ width: '100%' }}
          placeholder="金额"
          value={actionModal.amount}
          onChange={(val) => setActionModal((prev) => ({ ...prev, amount: Number(val) }))}
        />
      </Modal>
    </Row>
  );
};

export default AccountsPage;
