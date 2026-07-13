# FE-REQ-008 前端路由懒加载与拆包

## 1. 背景

前端业务页面已覆盖核心子系统，但 Vite 构建持续提示单个 JS chunk 超过 500KB。当前 `App.jsx` 静态导入所有页面，导致 Ant Design 与业务页面代码集中进入首屏包。

## 2. 目标

- 使用 `React.lazy` 和 `Suspense` 对页面组件做按需加载。
- 保持现有菜单和页面行为不变。
- 消除或显著降低 Vite 大 chunk 警告。

## 3. 范围

- `App.jsx` 页面组件懒加载。
- 增加后台页面加载态样式。
- 复用既有权限菜单逻辑。

## 4. 不在本轮范围

- 引入 React Router。
- 手工配置 Rollup vendor chunks。
- 页面级骨架屏深度定制。

## 5. 验收标准

- `npm run test`、`npm run build`、后端 `mvn test -q`、`git diff --check` 通过。
- 构建产物不再出现单个 chunk 超过 500KB 警告，或警告范围显著收敛并记录原因。
