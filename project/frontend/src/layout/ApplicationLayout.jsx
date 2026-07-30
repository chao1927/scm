import {
  AppstoreOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  QuestionCircleOutlined,
} from '@ant-design/icons'
import { Avatar, Button, Drawer, Dropdown, Layout, Menu, Tooltip, Typography } from 'antd'
import { useMemo, useState } from 'react'
import { Outlet, useLocation, useNavigate, useParams } from 'react-router-dom'
import { hasPermission } from '../auth/menuAccess'
import { menuOverridesFromResources } from '../auth/session'
import { findSystem, systemCatalog } from '../config/systemCatalog'

const { Header, Sider, Content } = Layout

function selectedPageFromPath(pathname, systemId) {
  const parts = pathname.split('/').filter(Boolean)
  return parts[0] === systemId ? parts[1] || 'workbench' : 'workbench'
}

export default function ApplicationLayout({ session, onLogout }) {
  const navigate = useNavigate()
  const location = useLocation()
  const params = useParams()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const isPlatformPage = location.pathname === '/workbench'
  const activeSystem = findSystem(params.systemId || 'supplier')
  const activePageId = isPlatformPage ? 'workbench' : selectedPageFromPath(location.pathname, activeSystem.id)
  const activePage = activeSystem.pages.find((item) => item.id === activePageId) || activeSystem.pages[0]
  const menuOverrides = useMemo(() => menuOverridesFromResources(session.menus), [session.menus])
  const hasDynamicMenu = menuOverrides.size > 0
  const menuConfiguredForSystem = (system) => !hasDynamicMenu
    || menuOverrides.has(system.id)
    || [...menuOverrides.entries()].some(([key, value]) => value.parentCode === system.id || key.startsWith(`${system.id}-`) || key.startsWith(`${system.id}.`))

  const moduleItems = useMemo(() => (isPlatformPage
    ? [
        { key: 'workbench', label: '平台工作台', sortNo: 0 },
      ]
    : activeSystem.pages
      .map((item, index) => {
        const candidates = [
          `${activeSystem.id}-${item.id}`,
          `${activeSystem.id}.${item.id}`,
          item.legacy,
          item.id,
        ].filter(Boolean)
        const override = candidates.map((key) => menuOverrides.get(key)).find(Boolean)
        return {
          key: item.id,
          label: override?.label || item.label,
          sortNo: override?.sortNo ?? index,
          configured: item.id === 'workbench' || !hasDynamicMenu || Boolean(override),
        }
      })
      .filter((item) => item.configured)
      .sort((left, right) => left.sortNo - right.sortNo)), [activeSystem, isPlatformPage, menuOverrides, session.permissions])

  const openSystem = (system) => {
    if (!hasPermission(session.permissions, system.permission) || !menuConfiguredForSystem(system)) return
    navigate(`/${system.id}/workbench`)
    setMobileMenuOpen(false)
  }

  const openModule = ({ key }) => {
    navigate(isPlatformPage ? `/${key}` : `/${activeSystem.id}/${key}`)
    setMobileMenuOpen(false)
  }

  const moduleNavigation = (
    <>
      <div className="module-context">
        <span className={`module-context-icon ${isPlatformPage ? 'platform-tone' : `system-tone-${activeSystem.id}`}`}>{isPlatformPage ? <AppstoreOutlined /> : activeSystem.shortName.slice(0, 1)}</span>
        <div>
          <Typography.Text strong>{isPlatformPage ? '供应链管理平台' : activeSystem.domain}</Typography.Text>
          <Typography.Text type="secondary">{isPlatformPage ? 'SCM PLATFORM' : activeSystem.code}</Typography.Text>
        </div>
      </div>
      <Menu mode="inline" selectedKeys={[activePageId]} items={moduleItems.map(({ key, label, disabled }) => ({ key, label, disabled }))} onClick={openModule} className="module-menu" />
      <div className="scope-summary">
        <Typography.Text strong>当前数据范围</Typography.Text>
        <Typography.Text type="secondary">按 IAM 组织、仓库、货主和本人权限过滤</Typography.Text>
      </div>
    </>
  )

  const userItems = [
    { key: 'help', icon: <QuestionCircleOutlined />, label: '使用说明' },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
  ]

  return (
    <Layout className="scm-shell">
      <Sider width={76} theme="dark" className="system-rail">
        <button className="scm-logo" aria-label="打开平台工作台" onClick={() => navigate('/workbench')}>SC</button>
        <div className="system-rail-list">
          {systemCatalog.map((system) => {
            const allowed = hasPermission(session.permissions, system.permission) && menuConfiguredForSystem(system)
            return (
              <Tooltip key={system.id} title={allowed ? system.name : `无${system.name}访问权限`} placement="right">
                <button
                  className={`system-rail-item ${activeSystem.id === system.id && !isPlatformPage ? 'active' : ''}`}
                  disabled={!allowed}
                  aria-label={system.name}
                  aria-current={activeSystem.id === system.id && !isPlatformPage ? 'page' : undefined}
                  onClick={() => openSystem(system)}
                >
                  <span className={`system-glyph system-tone-${system.id}`}>{system.shortName.slice(0, 1)}</span>
                  <span>{system.shortName}</span>
                </button>
              </Tooltip>
            )
          })}
        </div>
      </Sider>

      <Layout className="system-workspace">
        <Sider width={226} theme="light" className="module-sider">
          {moduleNavigation}
        </Sider>
        <Layout>
          <Header className="scm-header">
            <Button className="mobile-menu-button" icon={<MenuFoldOutlined />} onClick={() => setMobileMenuOpen(true)} aria-label="打开功能菜单" />
            <div className="scm-breadcrumb">
              {isPlatformPage
                ? <span>供应链管理平台</span>
                : <button onClick={() => openSystem(activeSystem)}>{activeSystem.name}</button>}
              <span>/</span>
              <strong>{isPlatformPage ? '平台工作台' : activePage.label}</strong>
            </div>
            <div className="header-actions">
              <Dropdown
                menu={{ items: userItems, onClick: ({ key }) => key === 'logout' && onLogout() }}
                trigger={['click']}
              >
                <button className="user-trigger">
                  <Avatar size={30}>{session.user?.username?.slice(0, 1)?.toUpperCase() || 'U'}</Avatar>
                  <span>{session.user?.username}</span>
                </button>
              </Dropdown>
            </div>
          </Header>
          <Content className="scm-content">
            <Outlet context={{ session }} />
          </Content>
        </Layout>
      </Layout>

      <Drawer title={isPlatformPage ? '供应链管理平台' : activeSystem.name} placement="left" size={300} open={mobileMenuOpen} onClose={() => setMobileMenuOpen(false)}>
        {moduleNavigation}
      </Drawer>

      <nav className="mobile-system-nav" aria-label="移动端子系统">
        {systemCatalog.map((system) => (
          <button key={system.id} disabled={!hasPermission(session.permissions, system.permission) || !menuConfiguredForSystem(system)} className={activeSystem.id === system.id && !isPlatformPage ? 'active' : ''} onClick={() => openSystem(system)}>
            <span className={`system-glyph system-tone-${system.id}`}>{system.shortName.slice(0, 1)}</span>
            <span>{system.shortName}</span>
          </button>
        ))}
      </nav>
    </Layout>
  )
}
