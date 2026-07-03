const modules = [
  { id: "workbench", title: "权限工作台", desc: "集中查看在线用户、授权变更、审批待办、登录风险和审计异常。" },
  {
    id: "apps",
    title: "应用管理",
    desc: "登记接入 SSO 和权限体系的采购、OMS、WMS、库存、BMS 等子系统。",
    no: "APP20260628001",
    filters: [["app_code", "应用编码", "text"], ["app_name", "应用名称", "text"], ["app_type", "应用类型", "select", ["全部", "Web", "OpenAPI", "移动端", "后台服务"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["app_code", "应用编码"], ["app_name", "应用名称"], ["app_type", "应用类型"], ["base_url", "首页地址"], ["status", "状态"], ["sort_no", "排序"], ["updated_at", "更新时间"]],
    form: [["app_code", "应用编码", "text"], ["app_name", "应用名称", "text"], ["app_type", "应用类型", "select", ["Web", "OpenAPI", "移动端", "后台服务"]], ["base_url", "首页地址", "text"], ["status", "状态", "select", ["启用", "停用"]], ["sort_no", "排序", "number"], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [["OMS", "OMS系统", "Web", "/oms", "启用", "10", "2026-06-28 09:20"], ["WMS", "WMS系统", "Web", "/wms", "启用", "20", "2026-06-28 09:22"], ["INV", "中央库存系统", "OpenAPI", "/inventory", "启用", "30", "2026-06-27 18:10"], ["BMS", "BMS系统", "Web", "/bms", "停用", "40", "2026-06-20 10:00"]],
  },
  {
    id: "sso-clients",
    title: "SSO客户端",
    desc: "配置子系统 OAuth/OIDC 客户端、回调地址、授权方式和 Token 有效期。",
    no: "SSO20260628001",
    filters: [["client_code", "客户端编码", "text"], ["app_name", "所属应用", "text"], ["grant_types", "授权类型", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["client_code", "客户端编码"], ["app_name", "所属应用"], ["redirect_uris", "回调地址"], ["grant_types", "授权类型"], ["token_ttl", "Token有效期"], ["refresh_ttl", "Refresh有效期"], ["status", "状态"]],
    form: [["client_code", "客户端编码", "text"], ["app_name", "所属应用", "text"], ["redirect_uris", "回调地址", "textarea"], ["grant_types", "授权类型", "text"], ["token_ttl", "Token有效期", "number"], ["refresh_ttl", "Refresh有效期", "number"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用", "重置密钥"],
    rows: [["oms-web", "OMS系统", "https://oms.local/callback", "授权码,刷新Token", "7200s", "604800s", "启用"], ["wms-web", "WMS系统", "https://wms.local/callback", "授权码,刷新Token", "7200s", "604800s", "启用"], ["inv-api", "中央库存系统", "https://inv.local/callback", "客户端凭证", "3600s", "-", "启用"]],
  },
  {
    id: "users",
    title: "用户管理",
    desc: "维护用户账号、状态、组织、绑定对象、MFA，并支持分配角色和重置密码。",
    no: "USER20260628001",
    filters: [["username", "账号", "text"], ["real_name", "姓名", "text"], ["user_type", "用户类型", "select", ["全部", "内部员工", "供应商用户", "客户用户", "货主用户", "系统账号"]], ["status", "状态", "select", ["全部", "待激活", "已启用", "已锁定", "已停用", "已注销"]]],
    columns: [["username", "账号"], ["real_name", "姓名"], ["mobile", "手机号"], ["email", "邮箱"], ["user_type", "用户类型"], ["org_name", "组织"], ["roles", "角色"], ["status", "状态"], ["last_login_at", "最近登录"]],
    form: [["username", "账号", "text"], ["real_name", "姓名", "text"], ["mobile", "手机号", "text"], ["email", "邮箱", "text"], ["user_type", "用户类型", "select", ["内部员工", "供应商用户", "客户用户", "货主用户", "系统账号"]], ["org_name", "组织", "text"], ["mfa_enabled", "MFA", "select", ["是", "否"]], ["status", "状态", "select", ["待激活", "已启用", "已锁定", "已停用", "已注销"]]],
    actions: ["查看", "修改", "启用", "停用", "重置密码", "分配角色"],
    rows: [["admin", "系统管理员", "13800000001", "admin@example.com", "内部员工", "总部/信息部", "系统管理员", "已启用", "2026-06-28 09:00"], ["buyer01", "采购员A", "13800000002", "buyer@example.com", "内部员工", "总部/采购部", "采购员", "已启用", "2026-06-28 08:50"], ["supplier01", "供应商沈澜", "13800000003", "shenlan@supplier.com", "供应商用户", "外部/供应商", "供应商业务", "已启用", "2026-06-27 16:20"], ["locked01", "锁定用户", "13800000004", "lock@example.com", "内部员工", "总部/仓储部", "仓库主管", "已锁定", "2026-06-25 10:00"], ["disabled01", "离职用户", "13800000005", "off@example.com", "内部员工", "总部/运营部", "-", "已停用", "2026-06-10 12:00"]],
  },
  {
    id: "orgs",
    title: "组织管理",
    desc: "维护公司、部门、岗位和人员归属，支撑权限范围和审批流。",
    no: "ORG20260628001",
    filters: [["org_code", "组织编码", "text"], ["org_name", "组织名称", "text"], ["org_type", "组织类型", "select", ["全部", "公司", "部门", "岗位", "成本中心"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["org_code", "组织编码"], ["org_name", "组织名称"], ["parent_org", "上级组织"], ["org_type", "组织类型"], ["manager", "负责人"], ["member_count", "人数"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["org_code", "组织编码", "text"], ["org_name", "组织名称", "text"], ["parent_org", "上级组织", "text"], ["org_type", "组织类型", "select", ["公司", "部门", "岗位", "成本中心"]], ["manager", "负责人", "text"], ["member_count", "人数", "number"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用", "同步HR"],
    rows: [["ORG-HQ", "总部", "-", "公司", "总经理", "128", "启用", "2026-06-28 09:20"], ["ORG-PUR", "采购部", "总部", "部门", "采购经理", "18", "启用", "2026-06-26 10:00"], ["ORG-WH", "仓储部", "总部", "部门", "仓库主管", "46", "启用", "2026-06-25 11:00"]],
  },
  {
    id: "roles",
    title: "角色管理",
    desc: "维护系统角色、业务角色、外部角色，并给角色分配功能和数据范围。",
    no: "ROLE20260628001",
    filters: [["role_code", "角色编码", "text"], ["role_name", "角色名称", "text"], ["role_type", "角色类型", "select", ["全部", "系统角色", "业务角色", "外部角色"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["role_code", "角色编码"], ["role_name", "角色名称"], ["role_type", "角色类型"], ["app_scope", "适用应用"], ["data_scope_type", "数据范围"], ["user_count", "用户数"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["role_code", "角色编码", "text"], ["role_name", "角色名称", "text"], ["role_type", "角色类型", "select", ["系统角色", "业务角色", "外部角色"]], ["app_scope", "适用应用", "text"], ["data_scope_type", "数据范围", "select", ["全部", "本组织", "本组织及下级", "本人", "自定义"]], ["description", "说明", "textarea"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用", "分配权限", "查看用户"],
    rows: [["SYS_ADMIN", "系统管理员", "系统角色", "ALL", "全部", "2", "启用", "2026-06-28 09:00"], ["PUR_BUYER", "采购员", "业务角色", "PUR", "本组织", "12", "启用", "2026-06-27 15:10"], ["WMS_PICKER", "拣货员", "业务角色", "WMS", "自定义", "26", "启用", "2026-06-26 11:00"], ["SUPPLIER_USER", "供应商业务", "外部角色", "SRM", "本人", "42", "启用", "2026-06-20 12:00"]],
  },
  {
    id: "menus",
    title: "菜单页面",
    desc: "维护应用菜单树、页面路由、组件路径、排序和可见性。",
    no: "MENU20260628001",
    filters: [["app_name", "应用", "select", ["全部", "权限系统", "采购系统", "OMS系统", "WMS系统", "中央库存系统"]], ["menu_name", "菜单名称", "text"], ["menu_type", "菜单类型", "select", ["全部", "目录", "页面", "外链"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["app_name", "应用"], ["parent_menu", "父菜单"], ["menu_code", "菜单编码"], ["menu_name", "菜单名称"], ["menu_type", "类型"], ["route_path", "路由"], ["sort_no", "排序"], ["visible", "显示"], ["status", "状态"]],
    form: [["app_name", "应用", "text"], ["parent_menu", "父菜单", "text"], ["menu_code", "菜单编码", "text"], ["menu_name", "菜单名称", "text"], ["menu_type", "类型", "select", ["目录", "页面", "外链"]], ["route_path", "路由", "text"], ["component_path", "组件路径", "text"], ["sort_no", "排序", "number"], ["visible", "显示", "select", ["是", "否"]], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "新增菜单", "调整排序", "停用"],
    rows: [["权限系统", "系统管理", "iam_users", "用户管理", "页面", "/iam/users", "10", "是", "启用"], ["权限系统", "系统管理", "iam_roles", "角色管理", "页面", "/iam/roles", "20", "是", "启用"], ["WMS系统", "出库作业", "wms_picking", "拣货任务", "页面", "/wms/picking-tasks", "40", "是", "启用"]],
  },
  {
    id: "permissions",
    title: "权限点",
    desc: "维护菜单、按钮、API、字段权限，CRUD 和审批等操作统一成权限点。",
    no: "PERM20260628001",
    filters: [["permission_code", "权限编码", "text"], ["app_name", "应用", "select", ["全部", "权限系统", "采购系统", "OMS系统", "WMS系统"]], ["permission_type", "权限类型", "select", ["全部", "菜单", "按钮", "API", "字段"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["app_name", "应用"], ["menu_name", "页面"], ["permission_code", "权限编码"], ["permission_name", "权限名称"], ["permission_type", "权限类型"], ["action_type", "动作"], ["api_method", "方法"], ["api_path", "API路径"], ["status", "状态"]],
    form: [["app_name", "应用", "text"], ["menu_name", "页面", "text"], ["permission_code", "权限编码", "text"], ["permission_name", "权限名称", "text"], ["permission_type", "权限类型", "select", ["菜单", "按钮", "API", "字段"]], ["resource_type", "资源类型", "select", ["页面", "按钮", "接口", "字段"]], ["action_type", "动作", "select", ["CREATE", "READ", "UPDATE", "DELETE", "EXPORT", "IMPORT", "APPROVE", "ASSIGN", "REVOKE"]], ["api_method", "方法", "select", ["GET", "POST", "PUT", "PATCH", "DELETE"]], ["api_path", "API路径", "text"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "绑定菜单", "绑定API", "停用"],
    rows: [["权限系统", "用户管理", "iam:user:read", "查询用户", "API", "READ", "GET", "/iam/users", "启用"], ["权限系统", "用户管理", "iam:user:create", "新增用户", "按钮", "CREATE", "POST", "/iam/users", "启用"], ["权限系统", "角色授权", "iam:role:assign_permission", "分配权限", "按钮", "ASSIGN", "POST", "/iam/roles/:id/permissions", "启用"]],
  },
  {
    id: "role-auth",
    title: "角色授权",
    desc: "按应用菜单树给角色勾选菜单、按钮、API 权限，并清理权限缓存。",
    no: "RPA20260628001",
    filters: [["role_code", "角色编码", "text"], ["app_name", "应用", "select", ["全部", "权限系统", "采购系统", "OMS系统", "WMS系统"]], ["grant_status", "授权状态", "select", ["全部", "已授权", "已取消"]], ["permission_code", "权限编码", "text"]],
    columns: [["role_code", "角色编码"], ["role_name", "角色名称"], ["app_name", "应用"], ["menu_name", "页面"], ["permission_code", "权限编码"], ["permission_name", "权限名称"], ["grant_status", "授权状态"], ["granted_by", "授权人"], ["granted_at", "授权时间"]],
    form: [["role_code", "角色编码", "text"], ["role_name", "角色名称", "text"], ["app_name", "应用", "text"], ["menu_name", "页面", "text"], ["permission_code", "权限编码", "text"], ["permission_name", "权限名称", "text"], ["grant_status", "授权状态", "select", ["已授权", "已取消"]], ["granted_by", "授权人", "text"]],
    actions: ["查看", "修改", "勾选授权", "取消授权", "保存"],
    rows: [["SYS_ADMIN", "系统管理员", "权限系统", "用户管理", "iam:user:create", "新增用户", "已授权", "admin", "2026-06-28 09:10"], ["PUR_BUYER", "采购员", "采购系统", "采购订单", "purchase:order:update", "编辑采购订单", "已授权", "admin", "2026-06-27 15:20"], ["WMS_PICKER", "拣货员", "WMS系统", "拣货任务", "wms:picking_task:confirm", "确认数量", "已授权", "admin", "2026-06-26 10:30"]],
  },
  {
    id: "user-roles",
    title: "用户角色",
    desc: "给用户分配或取消角色，支持手工授权、组织继承、岗位继承和默认角色。",
    no: "UR20260628001",
    filters: [["username", "用户", "text"], ["role_code", "角色", "text"], ["grant_type", "授权类型", "select", ["全部", "手工", "组织继承", "岗位继承", "系统默认"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["username", "用户"], ["real_name", "姓名"], ["role_code", "角色编码"], ["role_name", "角色名称"], ["grant_type", "授权类型"], ["effective_from", "生效时间"], ["effective_to", "失效时间"], ["status", "状态"], ["granted_by", "授权人"]],
    form: [["username", "用户", "text"], ["real_name", "姓名", "text"], ["role_code", "角色编码", "text"], ["role_name", "角色名称", "text"], ["grant_type", "授权类型", "select", ["手工", "组织继承", "岗位继承", "系统默认"]], ["effective_from", "生效时间", "datetime-local"], ["effective_to", "失效时间", "datetime-local"], ["status", "状态", "select", ["启用", "停用"]], ["granted_by", "授权人", "text"]],
    actions: ["查看", "修改", "分配角色", "取消角色"],
    rows: [["admin", "系统管理员", "SYS_ADMIN", "系统管理员", "手工", "2026-01-01 00:00", "-", "启用", "system"], ["buyer01", "采购员A", "PUR_BUYER", "采购员", "组织继承", "2026-01-01 00:00", "-", "启用", "admin"], ["supplier01", "供应商沈澜", "SUPPLIER_USER", "供应商业务", "手工", "2026-06-01 00:00", "-", "启用", "admin"]],
  },
  {
    id: "data-scopes",
    title: "数据权限",
    desc: "控制用户或角色能看哪些组织、仓库、货主、供应商、客户数据。",
    no: "DS20260628001",
    filters: [["subject_name", "授权对象", "text"], ["subject_type", "对象类型", "select", ["全部", "用户", "角色"]], ["resource_type", "资源类型", "select", ["全部", "组织", "仓库", "货主", "供应商", "客户"]], ["scope_type", "数据范围", "select", ["全部", "全部", "本组织", "本组织及下级", "本人", "自定义"]]],
    columns: [["subject_type", "对象类型"], ["subject_name", "授权对象"], ["scope_type", "数据范围"], ["resource_type", "资源类型"], ["resource_list", "资源列表"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["subject_type", "对象类型", "select", ["用户", "角色"]], ["subject_name", "授权对象", "text"], ["scope_type", "数据范围", "select", ["全部", "本组织", "本组织及下级", "本人", "自定义"]], ["resource_type", "资源类型", "select", ["组织", "仓库", "货主", "供应商", "客户"]], ["resource_list", "资源列表", "textarea"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [["角色", "采购员", "本组织", "供应商", "SUP-001,SUP-002", "启用", "2026-06-28 09:20"], ["角色", "拣货员", "自定义", "仓库", "WH-EAST-01", "启用", "2026-06-27 10:10"], ["用户", "supplier01", "本人", "供应商", "SUP-001", "启用", "2026-06-26 14:00"]],
  },
  {
    id: "approval-templates",
    title: "审批模板",
    desc: "配置采购、库存调整、主数据变更等业务审批模板、节点、条件和委托。",
    no: "APT20260628001",
    filters: [["template_code", "模板编码", "text"], ["biz_type", "业务类型", "select", ["全部", "采购订单", "库存调整", "主数据变更", "退供申请"]], ["status", "状态", "select", ["全部", "启用", "停用"]], ["owner_org", "适用组织", "text"]],
    columns: [["template_code", "模板编码"], ["template_name", "模板名称"], ["biz_type", "业务类型"], ["node_count", "节点数"], ["condition_summary", "条件摘要"], ["owner_org", "适用组织"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["template_code", "模板编码", "text"], ["template_name", "模板名称", "text"], ["biz_type", "业务类型", "select", ["采购订单", "库存调整", "主数据变更", "退供申请"]], ["node_count", "节点数", "number"], ["condition_summary", "条件摘要", "textarea"], ["owner_org", "适用组织", "text"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用", "发布"],
    rows: [["PO_APPROVAL", "采购订单审批", "采购订单", "2", "金额>=50000", "采购部", "启用", "2026-06-28 09:10"], ["INV_ADJ_APPROVAL", "库存调整审批", "库存调整", "3", "所有调整均审批", "仓储部", "启用", "2026-06-27 13:20"], ["MDM_CHANGE_APPROVAL", "主数据关键变更审批", "主数据变更", "2", "关键字段变更", "信息部", "启用", "2026-06-26 11:00"]],
  },
  {
    id: "todos",
    title: "待办中心",
    desc: "展示待审批、已审批、抄送任务，支持通过、驳回、转交和加签。",
    no: "TODO20260628001",
    filters: [["approval_no", "审批单号", "text"], ["biz_type", "业务类型", "text"], ["approval_status", "状态", "select", ["全部", "待提交", "审批中", "已通过", "已驳回", "已撤回", "已取消"]], ["approver", "审批人", "text"]],
    columns: [["approval_no", "审批单号"], ["biz_type", "业务类型"], ["biz_no", "业务单号"], ["summary", "摘要"], ["approver", "审批人"], ["approval_status", "状态"], ["submitted_at", "提交时间"], ["processed_at", "处理时间"]],
    form: [["approval_no", "审批单号", "text"], ["biz_type", "业务类型", "text"], ["biz_no", "业务单号", "text"], ["summary", "摘要", "textarea"], ["approver", "审批人", "text"], ["approval_status", "状态", "select", ["待提交", "审批中", "已通过", "已驳回", "已撤回", "已取消"]]],
    actions: ["查看", "修改", "通过", "驳回", "转交", "加签"],
    rows: [["APV20260628001", "采购订单", "PO20260628001", "采购金额 66850 元", "采购经理", "审批中", "2026-06-28 10:00", "-"], ["APV20260627004", "库存调整", "ADJ20260628001", "盘亏调整 -8", "仓库主管", "审批中", "2026-06-27 15:00", "-"], ["APV20260626005", "主数据变更", "SKU-EL-1008", "库存单位变更", "信息经理", "已驳回", "2026-06-26 11:00", "2026-06-26 15:20"]],
  },
  {
    id: "sessions",
    title: "会话管理",
    desc: "查看在线用户、客户端、Token 状态、登录 IP、过期时间并可强制下线。",
    no: "SES20260628001",
    filters: [["username", "用户", "text"], ["client_code", "客户端", "text"], ["token_status", "Token状态", "select", ["全部", "有效", "已刷新", "已撤销", "已过期"]], ["login_ip", "登录IP", "text"]],
    columns: [["username", "用户"], ["client_code", "客户端"], ["login_ip", "登录IP"], ["token_status", "Token状态"], ["login_at", "登录时间"], ["expires_at", "过期时间"], ["user_agent", "客户端信息"]],
    form: [["username", "用户", "text"], ["client_code", "客户端", "text"], ["login_ip", "登录IP", "text"], ["token_status", "Token状态", "select", ["有效", "已刷新", "已撤销", "已过期"]], ["login_at", "登录时间", "datetime-local"], ["expires_at", "过期时间", "datetime-local"], ["user_agent", "客户端信息", "textarea"]],
    actions: ["查看", "修改", "强制下线"],
    rows: [["admin", "iam-web", "10.10.3.11", "有效", "2026-06-28 09:00", "2026-06-28 11:00", "Chrome 126"], ["buyer01", "pur-web", "10.10.3.21", "有效", "2026-06-28 08:50", "2026-06-28 10:50", "Chrome 126"], ["supplier01", "srm-web", "172.16.1.20", "已过期", "2026-06-27 16:20", "2026-06-27 18:20", "Safari"]],
  },
  {
    id: "login-logs",
    title: "登录日志",
    desc: "记录登录成功和失败，追踪账号锁定、密码错误、Token 过期和 MFA 失败。",
    no: "LLOG20260628001",
    filters: [["username", "账号", "text"], ["login_result", "登录结果", "select", ["全部", "成功", "失败"]], ["failure_reason", "失败原因", "select", ["全部", "密码错误", "用户停用", "用户锁定", "Token过期", "MFA失败"]], ["login_ip", "IP", "text"]],
    columns: [["username", "账号"], ["real_name", "用户"], ["client_code", "客户端"], ["login_result", "登录结果"], ["failure_reason", "失败原因"], ["login_ip", "IP"], ["login_at", "登录时间"]],
    form: [["username", "账号", "text"], ["real_name", "用户", "text"], ["client_code", "客户端", "text"], ["login_result", "登录结果", "select", ["成功", "失败"]], ["failure_reason", "失败原因", "select", ["密码错误", "用户停用", "用户锁定", "Token过期", "MFA失败", "-"]], ["login_ip", "IP", "text"], ["login_at", "登录时间", "datetime-local"]],
    actions: ["查看", "修改", "导出"],
    rows: [["admin", "系统管理员", "iam-web", "成功", "-", "10.10.3.11", "2026-06-28 09:00"], ["locked01", "锁定用户", "wms-web", "失败", "密码错误", "10.10.3.45", "2026-06-28 08:10"], ["supplier01", "供应商沈澜", "srm-web", "失败", "MFA失败", "172.16.1.20", "2026-06-27 16:18"]],
  },
  {
    id: "operation-logs",
    title: "操作日志",
    desc: "查询用户写操作、授权变更、敏感导出和审批操作，日志只追加。",
    no: "OPLOG20260628001",
    filters: [["operator_name", "操作人", "text"], ["app_code", "应用", "select", ["全部", "IAM", "PUR", "OMS", "WMS", "INV"]], ["operation_type", "操作类型", "select", ["全部", "CREATE", "UPDATE", "DELETE", "ENABLE", "DISABLE", "ASSIGN", "REVOKE", "APPROVE", "EXPORT", "LOGIN"]], ["operation_result", "结果", "select", ["全部", "成功", "失败"]]],
    columns: [["operator_name", "操作人"], ["app_code", "应用"], ["module_code", "模块"], ["operation_type", "操作类型"], ["permission_code", "权限点"], ["biz_no", "业务对象"], ["operation_result", "结果"], ["ip", "IP"], ["created_at", "操作时间"]],
    form: [["operator_name", "操作人", "text"], ["app_code", "应用", "text"], ["module_code", "模块", "text"], ["operation_type", "操作类型", "text"], ["permission_code", "权限点", "text"], ["biz_no", "业务对象", "text"], ["operation_result", "结果", "select", ["成功", "失败"]], ["ip", "IP", "text"], ["operation_desc", "说明", "textarea"]],
    actions: ["查看", "修改", "导出"],
    rows: [["系统管理员", "IAM", "用户管理", "CREATE", "iam:user:create", "USER20260628001", "成功", "10.10.3.11", "2026-06-28 09:20"], ["系统管理员", "IAM", "角色授权", "ASSIGN", "iam:role:assign_permission", "SYS_ADMIN", "成功", "10.10.3.11", "2026-06-28 09:30"], ["采购经理", "PUR", "采购订单", "APPROVE", "purchase:order:approve", "PO20260628001", "成功", "10.10.3.18", "2026-06-28 10:00"]],
  },
  {
    id: "security",
    title: "安全策略",
    desc: "配置密码策略、锁定策略、会话策略、MFA 和 IP 白名单。",
    no: "SEC20260628001",
    filters: [["policy_code", "策略编码", "text"], ["policy_type", "策略类型", "select", ["全部", "密码", "锁定", "会话", "MFA", "IP白名单"]], ["status", "状态", "select", ["全部", "启用", "停用"]], ["app_scope", "适用应用", "text"]],
    columns: [["policy_code", "策略编码"], ["policy_name", "策略名称"], ["policy_type", "策略类型"], ["policy_value", "策略值"], ["app_scope", "适用应用"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["policy_code", "策略编码", "text"], ["policy_name", "策略名称", "text"], ["policy_type", "策略类型", "select", ["密码", "锁定", "会话", "MFA", "IP白名单"]], ["policy_value", "策略值", "textarea"], ["app_scope", "适用应用", "text"], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [["PWD_COMPLEXITY", "密码复杂度", "密码", "长度>=8,大小写+数字", "ALL", "启用", "2026-06-20 10:00"], ["LOGIN_LOCK", "登录失败锁定", "锁定", "5次失败锁定30分钟", "ALL", "启用", "2026-06-20 10:00"], ["TOKEN_TTL", "Token有效期", "会话", "7200秒", "ALL", "启用", "2026-06-20 10:00"]],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护权限系统页面枚举，包括用户状态、角色类型、权限类型、动作类型等。",
    no: "ENUM20260628001",
    filters: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["enum_type", "枚举类型"], ["enum_value", "枚举值"], ["label", "显示名称"], ["is_default", "默认"], ["sort_no", "排序"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["is_default", "默认", "select", ["是", "否"]], ["sort_no", "排序", "number"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "排序", "停用"],
    rows: [["USER_STATUS", "ENABLED", "已启用", "否", "20", "启用", "2026-06-20 10:00"], ["PERMISSION_TYPE", "API", "API", "否", "30", "启用", "2026-06-20 10:00"], ["ACTION_TYPE", "APPROVE", "审批", "否", "70", "启用", "2026-06-18 09:00"]],
  },
];

const state = { filters: {}, page: {}, pageSize: 5 };
const app = document.querySelector("#app");
const recordsByModule = Object.fromEntries(modules.filter((m) => m.columns).map((m) => [m.id, makeRecords(m)]));

function makeRecords(mod) {
  return mod.rows.map((row, index) => {
    const record = { id: `${mod.id}-${index + 1}` };
    mod.columns.forEach(([key], i) => (record[key] = row[i] || ""));
    return record;
  });
}

function esc(value) {
  return String(value ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}

function route() {
  const raw = window.location.hash.replace(/^#\/?/, "");
  const [moduleId = "workbench", view = "list", recordId = ""] = raw.split("/");
  return { moduleId, view, recordId };
}

function moduleById(id) {
  return modules.find((m) => m.id === id) || modules[0];
}

function go(path) {
  window.location.hash = path;
}

function render() {
  const r = route();
  const mod = moduleById(r.moduleId);
  if (mod.id === "workbench") return shell(mod, workbench());
  if (r.view === "detail") return shell(mod, detail(mod, r.recordId));
  if (r.view === "new") return shell(mod, form(mod, "new"));
  if (r.view === "edit") return shell(mod, form(mod, "edit", r.recordId));
  return shell(mod, list(mod));
}

function shell(active, content) {
  app.innerHTML = `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-title">权限系统原型</div>
          <div class="brand-subtitle">Identity & Access Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules.map((m) => `<button class="nav-item ${m.id === active.id ? "active" : ""}" data-route="/${m.id}"><span class="nav-dot"></span><span>${esc(m.title)}</span></button>`).join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">权限系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索用户、角色、权限、IP" />
            <span class="user-chip">系统管理员</span>
          </div>
        </header>
        <section class="content">${content}</section>
      </main>
    </div>
    <div id="toast" class="toast hidden"></div>
  `;
}

function head(mod, suffix = "") {
  return `
    <div class="page-head">
      <div>
        <h1 class="page-title">${esc(mod.title)}${suffix}</h1>
        <p class="page-desc">${esc(mod.desc)}</p>
      </div>
      <div class="button-row">
        <button class="btn" data-action="refresh">刷新</button>
        <button class="btn primary" data-route="/${mod.id}/new">新增</button>
      </div>
    </div>
  `;
}

function workbench() {
  const stats = [["在线用户", "42", "外部用户 8"], ["权限变更", "16", "今日授权 11 次"], ["审批待办", "9", "采购审批 4 单"], ["登录失败", "7", "MFA失败 2 次"], ["敏感操作", "5", "导出 3 次"]];
  return `
    ${head(modules[0])}
    <div class="stats-grid">${stats.map(([a, b, c]) => `<div class="stat-card"><div class="stat-title">${a}</div><div class="stat-value">${b}</div><div class="stat-note">${c}</div></div>`).join("")}</div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">身份授权闭环</h2>
        <div class="flow-steps">
          ${["应用接入 SSO", "创建用户并绑定组织", "维护角色和权限点", "角色授权", "用户分配角色", "配置数据权限", "Token 校验和缓存", "审计日志追溯"].map((x, i) => `<div class="flow-step"><span>${i + 1}. ${x}</span><span>进入</span></div>`).join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>审批</td><td>PO20260628001</td><td>${tag("审批中", "amber")}</td></tr>
            <tr><td>登录风险</td><td>locked01</td><td>${tag("已锁定", "red")}</td></tr>
            <tr><td>授权</td><td>SYS_ADMIN</td><td>${tag("已授权", "green")}</td></tr>
            <tr><td>会话</td><td>supplier01</td><td>${tag("已过期", "red")}</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function list(mod) {
  const rows = filtered(mod);
  const totalPages = Math.max(1, Math.ceil(rows.length / state.pageSize));
  const page = Math.min(state.page[mod.id] || 1, totalPages);
  state.page[mod.id] = page;
  const shown = rows.slice((page - 1) * state.pageSize, page * state.pageSize);
  return `
    ${head(mod)}
    <div class="filter-panel">
      <div class="filter-grid">
        ${mod.filters.map((f) => filterField(mod, f)).join("")}
        <div class="filter-actions">
          <button class="btn primary" data-action="search" data-module="${mod.id}">查询</button>
          <button class="btn" data-action="reset" data-module="${mod.id}">重置</button>
        </div>
      </div>
    </div>
    <div class="table-panel">
      <div class="table-toolbar"><div class="table-title">${esc(mod.title)}列表</div><div class="button-row"><button class="btn" data-action="export">导出</button><button class="btn" data-action="batch">批量操作</button></div></div>
      ${table(mod, shown)}
      <div class="pagination">
        <span>共 ${rows.length} 条，每页 ${state.pageSize} 条，当前第 ${page}/${totalPages} 页</span>
        <div class="pager-buttons">
          <button class="btn" data-action="prev-page" data-module="${mod.id}" ${page <= 1 ? "disabled" : ""}>上一页</button>
          <button class="btn" data-action="next-page" data-module="${mod.id}" ${page >= totalPages ? "disabled" : ""}>下一页</button>
        </div>
      </div>
    </div>
  `;
}

function filtered(mod) {
  const f = state.filters[mod.id] || {};
  return (recordsByModule[mod.id] || []).filter((r) => Object.entries(f).every(([k, v]) => !v || v === "全部" || String(r[k] || "").includes(v)));
}

function filterField(mod, [key, label, type, options]) {
  const val = state.filters[mod.id]?.[key] || "";
  if (type === "select") return `<div class="field"><label>${esc(label)}</label><select data-filter="${key}" data-module="${mod.id}">${options.map((o) => `<option ${o === val ? "selected" : ""}>${esc(o)}</option>`).join("")}</select></div>`;
  return `<div class="field"><label>${esc(label)}</label><input type="${type}" value="${esc(val)}" placeholder="请输入${esc(label)}" data-filter="${key}" data-module="${mod.id}" /></div>`;
}

function table(mod, rows) {
  if (!rows.length) return `<div class="empty">暂无数据，请调整查询条件。</div>`;
  return `
    <table>
      <thead><tr>${mod.columns.map(([, label]) => `<th>${esc(label)}</th>`).join("")}<th style="width: 220px;">操作</th></tr></thead>
      <tbody>${rows.map((r) => `<tr>${mod.columns.map(([k]) => `<td title="${esc(r[k])}">${cell(r[k])}</td>`).join("")}<td><div class="table-actions">${rowActions(mod, r)}</div></td></tr>`).join("")}</tbody>
    </table>
  `;
}

function rowActions(mod, record) {
  return mod.actions.map((a) => {
    if (a === "查看") return `<button class="btn text" data-route="/${mod.id}/detail/${record.id}">查看</button>`;
    if (a === "修改") return `<button class="btn text" data-route="/${mod.id}/edit/${record.id}">修改</button>`;
    const danger = ["停用", "取消授权", "取消角色", "强制下线", "驳回", "撤销", "禁用"].includes(a) ? "danger" : "";
    return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(a)}" data-no="${esc(primaryNo(mod, record))}">${esc(a)}</button>`;
  }).join("");
}

function cell(value) {
  const raw = String(value || "");
  if (["待激活", "待处理", "审批中", "待提交", "请求中", "有效", "是"].includes(raw)) return tag(raw, "amber");
  if (["启用", "已启用", "成功", "已授权", "已通过", "通过"].includes(raw)) return tag(raw, "green");
  if (["停用", "已停用", "已锁定", "已注销", "失败", "已过期", "已撤销", "否"].includes(raw)) return tag(raw, "red");
  if (["已刷新", "处理中", "已驳回", "已取消"].includes(raw)) return tag(raw, "purple");
  return esc(raw);
}

function tag(value, color = "") {
  return `<span class="tag ${color}">${esc(value)}</span>`;
}

function primaryNo(mod, record) {
  const key = mod.columns.find(([k]) => k.endsWith("_code") || k.endsWith("_no") || k === "username")?.[0] || mod.columns[0][0];
  return record[key] || mod.no || record.id;
}

function findRecord(mod, id) {
  return (recordsByModule[mod.id] || []).find((r) => r.id === id) || (recordsByModule[mod.id] || [])[0] || {};
}

function detail(mod, id) {
  const r = findRecord(mod, id);
  return `
    ${head(mod, "详情")}
    <div class="detail-layout">
      <div>
        <div class="detail-card">
          <h2 class="detail-title">基本信息</h2>
          <div class="detail-grid">${mod.columns.map(([k, label]) => `<div><div class="kv-label">${esc(label)}</div><div class="kv-value">${cell(r[k])}</div></div>`).join("")}</div>
        </div>
        <div class="detail-card">
          <h2 class="detail-title">权限明细</h2>
          <table>
            <thead><tr><th>应用</th><th>页面</th><th>权限编码</th><th>权限名称</th><th>动作</th><th>授权状态</th></tr></thead>
            <tbody>
              <tr><td>权限系统</td><td>用户管理</td><td>iam:user:read</td><td>查询用户</td><td>READ</td><td>${tag("已授权", "green")}</td></tr>
              <tr><td>权限系统</td><td>角色管理</td><td>iam:role:update</td><td>编辑角色</td><td>UPDATE</td><td>${tag("已授权", "green")}</td></tr>
              <tr><td>WMS系统</td><td>拣货任务</td><td>wms:picking_task:confirm</td><td>确认数量</td><td>APPROVE</td><td>${tag("已授权", "green")}</td></tr>
            </tbody>
          </table>
        </div>
      </div>
      <aside>
        <div class="detail-card">
          <h2 class="detail-title">操作</h2>
          <div class="button-row">
            <button class="btn" data-route="/${mod.id}">返回</button>
            <button class="btn primary" data-route="/${mod.id}/edit/${r.id}">修改</button>
            <button class="btn" data-action="export">导出</button>
          </div>
        </div>
        <div class="detail-card">
          <h2 class="detail-title">安全流转</h2>
          <div class="timeline">${["创建/接入", "分配角色或权限", "配置数据范围", "生成权限版本", "刷新子系统缓存", "写入审计日志"].map((s, i) => `<div class="timeline-item"><div class="timeline-main">${s}</div><div class="timeline-sub">2026-06-${22 + i} ${9 + i}:00，记录权限操作日志</div></div>`).join("")}</div>
        </div>
      </aside>
    </div>
  `;
}

function form(mod, mode, id = "") {
  const r = mode === "edit" ? findRecord(mod, id) : {};
  const title = mode === "edit" ? "修改" : "新增";
  return `
    ${head(mod, title)}
    <div class="form-panel">
      <div class="form-section">
        <h2 class="section-title">${esc(mod.title)}${title}信息</h2>
        <div class="form-grid">${mod.form.map((f) => formField(mod, r, f, mode)).join("")}</div>
      </div>
      <div class="form-section">
        <h2 class="section-title">业务备注</h2>
        <div class="field"><label>备注</label><textarea placeholder="记录授权原因、审批意见、停用原因、安全策略说明或审计备注">${mode === "edit" ? "根据当前身份权限情况调整后保存。" : ""}</textarea></div>
      </div>
      <div class="form-actions">
        <button class="btn primary" data-action="save-form" data-module="${mod.id}">保存</button>
        <button class="btn" data-route="/${mod.id}">取消</button>
      </div>
    </div>
  `;
}

function formField(mod, record, [key, label, type, options], mode) {
  const defaultValue = mode === "new" && (key.endsWith("_no") || key.endsWith("_code") || key === "username") ? mod.no : "";
  const value = record[key] || defaultValue;
  if (type === "select") return `<div class="field"><label>${esc(label)}</label><select>${options.map((o) => `<option ${o === value ? "selected" : ""}>${esc(o)}</option>`).join("")}</select></div>`;
  if (type === "textarea") return `<div class="field"><label>${esc(label)}</label><textarea placeholder="请输入${esc(label)}">${esc(value)}</textarea></div>`;
  return `<div class="field"><label>${esc(label)}</label><input type="${type}" value="${esc(value)}" placeholder="请输入${esc(label)}" /></div>`;
}

function toast(message) {
  const t = document.querySelector("#toast");
  t.textContent = message;
  t.classList.remove("hidden");
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => t.classList.add("hidden"), 1800);
}

document.addEventListener("click", (e) => {
  const routeEl = e.target.closest("[data-route]");
  if (routeEl) return go(routeEl.dataset.route);
  const el = e.target.closest("[data-action]");
  if (!el) return;
  const mod = moduleById(el.dataset.module || route().moduleId);
  const action = el.dataset.action;
  if (action === "search") { state.page[mod.id] = 1; render(); toast("已按当前条件查询"); }
  if (action === "reset") { state.filters[mod.id] = {}; state.page[mod.id] = 1; render(); toast("查询条件已重置"); }
  if (action === "prev-page") { state.page[mod.id] = Math.max(1, (state.page[mod.id] || 1) - 1); render(); }
  if (action === "next-page") { state.page[mod.id] = (state.page[mod.id] || 1) + 1; render(); }
  if (action === "row-op") toast(`${el.dataset.no} 已执行：${el.dataset.op}`);
  if (action === "save-form") { toast("已保存原型数据，返回列表"); setTimeout(() => go(`/${mod.id}`), 350); }
  if (["refresh", "export", "batch"].includes(action)) toast(`已触发：${el.textContent.trim()}`);
});

document.addEventListener("input", (e) => {
  const input = e.target.closest("[data-filter]");
  if (!input) return;
  state.filters[input.dataset.module] = state.filters[input.dataset.module] || {};
  state.filters[input.dataset.module][input.dataset.filter] = input.value;
});

document.addEventListener("change", (e) => {
  const input = e.target.closest("[data-filter]");
  if (!input) return;
  state.filters[input.dataset.module] = state.filters[input.dataset.module] || {};
  state.filters[input.dataset.module][input.dataset.filter] = input.value;
});

window.addEventListener("hashchange", render);
if (!window.location.hash) window.location.hash = "/workbench";
else render();
