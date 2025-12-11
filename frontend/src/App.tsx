import { Layout, Menu, Typography } from 'antd';
import {
  BankOutlined,
  DashboardOutlined,
  DeploymentUnitOutlined,
  SwapOutlined,
} from '@ant-design/icons';
import { Link, Route, Routes, useLocation } from 'react-router-dom';
import AccountsPage from './pages/AccountsPage';
import PaymentsPage from './pages/PaymentsPage';
import TreasuryPage from './pages/TreasuryPage';
import Dashboard from './pages/Dashboard';
import './styles.css';

const { Header, Content, Sider } = Layout;

const menuItems = [
  { key: '/', label: <Link to="/">Dashboard</Link>, icon: <DashboardOutlined /> },
  { key: '/accounts', label: <Link to="/accounts">账户管理</Link>, icon: <BankOutlined /> },
  { key: '/payments', label: <Link to="/payments">支付指令</Link>, icon: <SwapOutlined /> },
  { key: '/treasury', label: <Link to="/treasury">现金池</Link>, icon: <DeploymentUnitOutlined /> },
];

const App = () => {
  const location = useLocation();
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div className="logo">BankCore Portal</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname === '/' ? '/' : `/${location.pathname.split('/')[1]}`]}
          items={menuItems}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', paddingLeft: 16 }}>
          <Typography.Title level={4} style={{ margin: 0 }}>
            企业现金管理前端工作台
          </Typography.Title>
        </Header>
        <Content style={{ margin: '16px' }}>
          <div className="site-layout-content">
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/accounts" element={<AccountsPage />} />
              <Route path="/payments" element={<PaymentsPage />} />
              <Route path="/treasury" element={<TreasuryPage />} />
            </Routes>
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

export default App;
