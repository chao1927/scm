import { Result, Spin } from 'antd'
import { lazy, Suspense, useEffect, useState } from 'react'
import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { completeMfaLogin, login, logout, queryCurrentUser, queryMenus, queryPermissionSnapshot, verifyMfaChallenge } from './api/auth'
import { hasPermission } from './auth/menuAccess'
import { clearTokens, permissionCodesFromSnapshot, storeTokens } from './auth/session'
import { systemsById } from './config/systemCatalog'
import ApplicationLayout from './layout/ApplicationLayout'

const LoginPage = lazy(() => import('./pages/LoginPage'))
const ResourceDetailPage = lazy(() => import('./pages/ResourceDetailPage'))
const ResourceListPage = lazy(() => import('./pages/ResourceListPage'))
const SystemWorkbenchPage = lazy(() => import('./pages/SystemWorkbenchPage'))
const WorkbenchPage = lazy(() => import('./pages/WorkbenchPage'))

export default function App() {
  const [session, setSession] = useState(() => sessionStorage.getItem('access_token')
    ? { status: 'loading', user: null, permissions: [], menus: [], error: '' }
    : { status: 'anonymous', user: null, permissions: [], menus: [], error: '' })

  const loadSession = async () => {
    const [userResult, snapshot, menus] = await Promise.all([queryCurrentUser(), queryPermissionSnapshot(), queryMenus()])
    setSession({ status: 'authenticated', user: userResult.data, permissions: permissionCodesFromSnapshot(snapshot), menus, error: '' })
  }

  useEffect(() => {
    if (session.status !== 'loading') return
    loadSession().catch(() => {
      clearTokens()
      setSession({ status: 'anonymous', user: null, permissions: [], menus: [], error: '登录已失效，请重新登录' })
    })
  }, [session.status])

  const handleLogin = async ({ username, password }) => {
    setSession((current) => ({ ...current, status: 'authenticating', error: '' }))
    try {
      const deviceDigest = sessionStorage.getItem('iam_device_digest') || crypto.randomUUID()
      sessionStorage.setItem('iam_device_digest', deviceDigest)
      const result = await login(username, password, deviceDigest)
      if (result?.data?.status === 'MFA_REQUIRED') {
        setSession({
          status: 'mfa-required', user: null, permissions: [], menus: [], error: '',
          mfa: { challengeNo: result.data.challengeNo, sessionId: result.data.sessionId, deviceDigest },
        })
        return
      }
      storeTokens(result)
      await loadSession()
    } catch (error) {
      clearTokens()
      setSession({ status: 'anonymous', user: null, permissions: [], menus: [], error: error?.message || '登录失败' })
    }
  }

  const handleMfa = async ({ method, code }) => {
    setSession((current) => ({ ...current, status: 'mfa-verifying', error: '' }))
    try {
      const { challengeNo, sessionId, deviceDigest } = session.mfa
      await verifyMfaChallenge(challengeNo, sessionId, deviceDigest, method, code)
      storeTokens(await completeMfaLogin(challengeNo, sessionId, deviceDigest))
      await loadSession()
    } catch (error) {
      clearTokens()
      setSession((current) => ({ ...current, status: 'mfa-required', error: error?.message || 'MFA 验证失败' }))
    }
  }

  const handleLogout = async () => {
    const refreshToken = sessionStorage.getItem('refresh_token')
    try {
      if (refreshToken) await logout(refreshToken)
    } finally {
      clearTokens()
      setSession({ status: 'anonymous', user: null, permissions: [], menus: [], error: '' })
    }
  }

  if (['anonymous', 'authenticating', 'mfa-required', 'mfa-verifying'].includes(session.status)) {
    return <Suspense fallback={<div className="auth-loading"><Spin /></div>}><LoginPage loading={['authenticating', 'mfa-verifying'].includes(session.status)} error={session.error} mfa={session.status.startsWith('mfa-') ? session.mfa : null} onLogin={handleLogin} onMfa={handleMfa} /></Suspense>
  }
  if (session.status === 'loading') {
    return <div className="auth-loading"><Spin size="large" description="正在加载 IAM 权限" /></div>
  }

  return (
    <Suspense fallback={<div className="auth-loading"><Spin size="large" description="页面加载中" /></div>}>
      <Routes>
        <Route element={<ApplicationLayout session={session} onLogout={handleLogout} />}>
          <Route index element={<Navigate to="/workbench" replace />} />
          <Route path="workbench" element={<PlatformWorkbenchRoute />} />
          <Route path=":systemId" element={<SystemAccessGuard session={session}><SystemIndexRedirect /></SystemAccessGuard>} />
          <Route path=":systemId/workbench" element={<SystemAccessGuard session={session}><SystemWorkbenchPage /></SystemAccessGuard>} />
          <Route path=":systemId/:pageId" element={<SystemAccessGuard session={session}><ResourceListPage /></SystemAccessGuard>} />
          <Route path=":systemId/:pageId/:recordId" element={<SystemAccessGuard session={session}><ResourceDetailPage /></SystemAccessGuard>} />
          <Route path="*" element={<Navigate to="/workbench" replace />} />
        </Route>
      </Routes>
    </Suspense>
  )
}

function PlatformWorkbenchRoute() {
  const navigate = useNavigate()
  return <WorkbenchPage onOpenAsn={() => navigate('/supplier/asns')} />
}

function SystemIndexRedirect() {
  const { systemId } = useParams()
  return <Navigate to={`/${systemId}/workbench`} replace />
}

function SystemAccessGuard({ session, children }) {
  const { systemId } = useParams()
  const system = systemsById.get(systemId)
  if (!system) {
    return <Navigate to="/workbench" replace />
  }
  if (!hasPermission(session.permissions, system.permission)) {
    return <Result status="403" title="无权访问该子系统" subTitle={`当前用户缺少 ${system.permission} 或对应细分权限。`} />
  }
  return children
}
