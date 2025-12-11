import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Table,
  Tag,
  message,
} from 'antd';
import {
  approveRisk,
  failPayment,
  listPayments,
  postPayment,
  processBatch,
  processPayment,
  submitPayment,
} from '../api/payment';
import type { PaymentInstruction } from '../types';
import { PaymentRequest } from '../api/payment';
import StatusTag from '../components/StatusTag';

const channelOptions = [
  { label: '银企直联', value: 'EBANK_DIRECT' },
  { label: '跨境通道', value: 'CROSS_BORDER' },
  { label: '网银/柜面', value: 'BRANCH' },
];

export const PaymentsPage = () => {
  const [payments, setPayments] = useState<PaymentInstruction[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listPayments();
      setPayments(data);
    } catch (err) {
      console.error(err);
      message.error('加载支付指令失败，请确认 payment-service 已启动。');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const onSubmit = async (payload: PaymentRequest) => {
    try {
      await submitPayment(payload);
      message.success('指令已录入');
      refresh();
    } catch (err) {
      console.error(err);
      message.error('录入失败');
    }
  };

  const handleAction = async (instructionId: string, action: 'process' | 'approve' | 'post' | 'fail') => {
    try {
      if (action === 'process') await processPayment(instructionId);
      if (action === 'approve') await approveRisk(instructionId);
      if (action === 'post') await postPayment(instructionId);
      if (action === 'fail') await failPayment(instructionId);
      refresh();
    } catch (err) {
      console.error(err);
      message.error('操作失败');
    }
  };

  const onBatchProcess = async () => {
    if (selectedRowKeys.length === 0) {
      message.info('请选择至少一条指令');
      return;
    }
    try {
      const { total } = await processBatch(selectedRowKeys as string[]);
      message.success(`批处理入队成功：${total} 笔`);
      setSelectedRowKeys([]);
      refresh();
    } catch (err) {
      console.error(err);
      message.error('批处理失败');
    }
  };

  const counts = useMemo(() => {
    const statusCounter: Record<string, number> = {};
    payments.forEach((p) => {
      statusCounter[p.status] = (statusCounter[p.status] || 0) + 1;
    });
    return statusCounter;
  }, [payments]);

  const columns = [
    { title: '请求号', dataIndex: 'requestId', width: 150 },
    { title: '指令号', dataIndex: 'instructionId', width: 150 },
    { title: '付款人', dataIndex: 'payerAccount' },
    { title: '收款人', dataIndex: 'payeeAccount' },
    { title: '金额', dataIndex: 'amount' },
    { title: '币种', dataIndex: 'currency', width: 70 },
    { title: '渠道', dataIndex: 'channel', render: (val: string) => val || '直联' },
    { title: '批次', dataIndex: 'batchId', render: (val: string) => val || '单笔' },
    { title: '优先级', dataIndex: 'priority', width: 80 },
    { title: '风险评分', dataIndex: 'riskScore', width: 100 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 150,
      render: (status: any) => <StatusTag status={status} />,
    },
    {
      title: '操作',
      width: 280,
      render: (_: unknown, record: PaymentInstruction) => (
        <Space size="small">
          <Button size="small" onClick={() => handleAction(record.instructionId, 'process')}>
            风控+清算
          </Button>
          <Button size="small" onClick={() => handleAction(record.instructionId, 'approve')}>
            人工放行
          </Button>
          <Button size="small" onClick={() => handleAction(record.instructionId, 'post')}>
            总账记账
          </Button>
          <Button size="small" danger onClick={() => handleAction(record.instructionId, 'fail')}>
            拒绝
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Row gutter={16}>
      <Col xs={24} lg={10}>
        <Card title="录入支付/代发指令" bordered>
          <Form
            layout="vertical"
            onFinish={onSubmit}
            initialValues={{ currency: 'CNY', priority: 5, requestId: `REQ-${Date.now()}` }}
          >
            <Form.Item name="requestId" label="请求号" rules={[{ required: true, message: '请输入或生成请求号' }]}>
              <Input placeholder="如：REQ20240901001" />
            </Form.Item>
            <Form.Item name="instructionId" label="指令号" rules={[{ required: true, message: '请输入指令号' }]}>
              <Input placeholder="如：PMT20240901001" />
            </Form.Item>
            <Form.Item name="payerAccount" label="付款账户" rules={[{ required: true }]}>
              <Input placeholder="源账户" />
            </Form.Item>
            <Form.Item name="payeeAccount" label="收款账户" rules={[{ required: true }]}>
              <Input placeholder="目标账户" />
            </Form.Item>
            <Form.Item name="currency" label="币种" rules={[{ required: true }]}>
              <Select
                options={[
                  { label: 'CNY', value: 'CNY' },
                  { label: 'USD', value: 'USD' },
                ]}
              />
            </Form.Item>
            <Form.Item name="amount" label="金额" rules={[{ required: true }]}>
              <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="purpose" label="用途/摘要" rules={[{ required: true }]}> 
              <Input placeholder="工资代发、货款等" />
            </Form.Item>
            <Form.Item name="channel" label="渠道"> 
              <Select options={channelOptions} allowClear />
            </Form.Item>
            <Form.Item name="batchId" label="批次号">
              <Input placeholder="可选：批量代发批次号" />
            </Form.Item>
            <Form.Item name="priority" label="优先级">
              <InputNumber min={1} max={9} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block>
                提交指令
              </Button>
            </Form.Item>
          </Form>
          <Alert
            showIcon
            style={{ marginTop: 12 }}
            type="info"
            message="录入后可通过“风控+清算”并行处理，或模拟人工放行/记账。"
          />
        </Card>
      </Col>
      <Col xs={24} lg={14}>
        <Card
          title="支付指令列表"
          bordered
          extra={
            <Space>
              <Tag color="purple">PENDING {counts.PENDING || 0}</Tag>
              <Tag color="orange">风控 {counts.IN_RISK_REVIEW || 0}</Tag>
              <Tag color="gold">清算 {counts.CLEARING || 0}</Tag>
              <Button size="small" onClick={onBatchProcess} disabled={selectedRowKeys.length === 0}>
                并行批处理
              </Button>
            </Space>
          }
        >
          <Table
            columns={columns}
            dataSource={payments}
            rowKey="instructionId"
            loading={loading}
            size="small"
            pagination={{ pageSize: 6 }}
            rowSelection={{
              selectedRowKeys,
              onChange: setSelectedRowKeys,
            }}
            scroll={{ x: 900 }}
          />
        </Card>
      </Col>
    </Row>
  );
};

export default PaymentsPage;
