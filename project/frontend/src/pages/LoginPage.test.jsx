import { renderToStaticMarkup } from 'react-dom/server'
import { describe, expect, it } from 'vitest'
import LoginPage from './LoginPage'

describe('LoginPage accessibility', () => {
  it('uses a page heading and exposes authentication errors as a focusable alert', () => {
    const html = renderToStaticMarkup(
      <LoginPage loading={false} error="用户名或密码错误" onLogin={() => {}} />,
    )

    expect(html).toContain('<h1')
    expect(html).toContain('role="alert"')
    expect(html).toContain('tabindex="-1"')
    expect(html).toContain('aria-describedby="login-error"')
  })
})
