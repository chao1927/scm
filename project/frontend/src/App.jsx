import {
  ApiOutlined,
  AppstoreOutlined,
  AuditOutlined,
  BankOutlined,
  CarOutlined,
  DatabaseOutlined,
  DeploymentUnitOutlined,
  SafetyCertificateOutlined,
  ShopOutlined,
} from '@ant-design/icons'
import { Layout, Menu, Space, Tag, Typography } from 'antd'
import { lazy, Suspense, useMemo, useState } from 'react'
import { decorateMenuItems, parsePermissionCodes, shouldEnforceMenuAccess } from './auth/menuAccess'

const { Header, Sider, Content } = Layout
const AsnPage = lazy(() => import('./pages/AsnPage'))
const FulfillmentSettlementPage = lazy(() => import('./pages/FulfillmentSettlementPage'))
const IntegrationPage = lazy(() => import('./pages/IntegrationPage'))
const MdmIamPage = lazy(() => import('./pages/MdmIamPage'))
const PurchasePage = lazy(() => import('./pages/PurchasePage'))
const WarehouseInventoryPage = lazy(() => import('./pages/WarehouseInventoryPage'))
const WorkbenchPage = lazy(() => import('./pages/WorkbenchPage'))

export default function App() {
  const [selectedKey, setSelectedKey] = useState('workbench')
  const accessToken = globalThis.sessionStorage?.getItem('access_token') || ''
  const permissionCodes = useMemo(() => parsePermissionCodes(globalThis.sessionStorage?.getItem('permission_codes')), [])
  const enforceMenuAccess = shouldEnforceMenuAccess(accessToken, permissionCodes)
  const menuItems = useMemo(() => decorateMenuItems([
    { key: 'workbench', icon: <AppstoreOutlined />, label: '运营工作台' },
    { key: 'supplier', icon: <ShopOutlined />, label: '供应商协同', children: [
      { key: 'supplier-asn', label: 'ASN 发货通知' },
      { key: 'supplier-order', label: '采购订单确认', disabled: true },
      { key: 'supplier-quality', label: '质量整改', disabled: true },
    ] },
    { key: 'purchase', icon: <AuditOutlined />, label: '采购中心' },
    { key: 'wms', icon: <BankOutlined />, label: '仓储作业' },
    { key: 'inventory', icon: <DatabaseOutlined />, label: '中央库存' },
    { key: 'oms', icon: <DeploymentUnitOutlined />, label: '订单履约' },
    { key: 'tms', icon: <CarOutlined />, label: '物流运输' },
    { key: 'bms', icon: <BankOutlined />, label: '费用结算' },
    { key: 'mdm', icon: <DatabaseOutlined />, label: '主数据' },
    { key: 'integration', icon: <ApiOutlined />, label: '集成中心' },
    { key: 'permission', icon: <SafetyCertificateOutlined />, label: '权限管理' },
  ], permissionCodes, enforceMenuAccess), [enforceMenuAccess, permissionCodes])

  const pageTitle = selectedKey === 'supplier-asn'
    ? '供应商系统'
    : selectedKey === 'purchase' ? '采购中心'
    : ['wms', 'inventory'].includes(selectedKey) ? '仓储与库存'
    : ['oms', 'tms', 'bms'].includes(selectedKey) ? '履约、物流与结算'
    : ['mdm', 'permission'].includes(selectedKey) ? '主数据与权限'
    : selectedKey === 'integration' ? '集成中心' : '供应链平台'

  return (
    <Layout className="app-shell">
      <Sider width={224} theme="light" className="app-sider">
        <div className="brand"><ShopOutlined /> 供应链系统</div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          defaultOpenKeys={['supplier']}
          items={menuItems}
          onClick={({ key }) => setSelectedKey(key)}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Typography.Text strong>{pageTitle}</Typography.Text>
          <Space><Tag color="blue">开发环境</Tag><Typography.Text type="secondary">端到端供应链系统</Typography.Text></Space>
        </Header>
        <Content className="app-content">
          <Suspense fallback={<div className="page-loading">页面加载中</div>}>
            {selectedKey === 'supplier-asn' && <AsnPage />}
            {selectedKey === 'purchase' && <PurchasePage />}
            {['wms', 'inventory'].includes(selectedKey) && <WarehouseInventoryPage />}
            {['oms', 'tms', 'bms'].includes(selectedKey) && <FulfillmentSettlementPage />}
            {['mdm', 'permission'].includes(selectedKey) && <MdmIamPage />}
            {selectedKey === 'integration' && <IntegrationPage />}
            {!['supplier-asn', 'purchase', 'wms', 'inventory', 'oms', 'tms', 'bms', 'mdm', 'permission', 'integration'].includes(selectedKey) && <WorkbenchPage onOpenAsn={() => setSelectedKey('supplier-asn')} />}
          </Suspense>
        </Content>
      </Layout>
    </Layout>
  )
}
