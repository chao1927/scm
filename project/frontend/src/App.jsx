import { AppstoreOutlined, DatabaseOutlined, SafetyCertificateOutlined, ShopOutlined } from '@ant-design/icons'
import { Layout, Menu, Space, Tag, Typography } from 'antd'
import AsnPage from './pages/AsnPage'

const { Header, Sider, Content } = Layout

export default function App() {
  return (
    <Layout className="app-shell">
      <Sider width={224} theme="light" className="app-sider">
        <div className="brand"><ShopOutlined /> 供应链系统</div>
        <Menu
          mode="inline"
          selectedKeys={['supplier-asn']}
          items={[
            { key: 'workbench', icon: <AppstoreOutlined />, label: '供应商工作台' },
            { key: 'supplier', icon: <ShopOutlined />, label: '供应商协同', children: [
              { key: 'supplier-asn', label: 'ASN 发货通知' },
              { key: 'supplier-order', label: '采购订单确认', disabled: true },
              { key: 'supplier-quality', label: '质量整改', disabled: true },
            ] },
            { key: 'master-data', icon: <DatabaseOutlined />, label: '主数据', disabled: true },
            { key: 'permission', icon: <SafetyCertificateOutlined />, label: '权限管理', disabled: true },
          ]}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Typography.Text strong>供应商系统</Typography.Text>
          <Space><Tag color="blue">开发环境</Tag><Typography.Text type="secondary">采购协同中心</Typography.Text></Space>
        </Header>
        <Content className="app-content"><AsnPage /></Content>
      </Layout>
    </Layout>
  )
}
