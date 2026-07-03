const modules = [
  { id: "workbench", title: "库存工作台", desc: "集中查看缺货、预占失败、冻结、对账差异和调整待审。" },
  {
    id: "balances",
    title: "库存余额",
    desc: "按 SKU、仓库、货主、批次、库存状态查询中央库存账面余额。",
    no: "BAL20260628001",
    filters: [["warehouse", "仓库", "text"], ["owner", "货主", "text"], ["sku_code", "SKU", "text"], ["stock_status", "库存状态", "select", ["全部", "可用", "冻结", "不合格", "在途", "待退供"]]],
    columns: [["warehouse", "仓库"], ["owner", "货主"], ["sku_code", "SKU"], ["batch_no", "批次"], ["stock_status", "库存状态"], ["on_hand_qty", "实物"], ["available_qty", "可用"], ["reserved_qty", "预占"], ["frozen_qty", "冻结"], ["in_transit_qty", "在途"]],
    form: [["warehouse", "仓库", "text"], ["owner", "货主", "text"], ["sku_code", "SKU", "text"], ["batch_no", "批次", "text"], ["stock_status", "库存状态", "select", ["可用", "冻结", "不合格", "在途", "待退供"]], ["on_hand_qty", "实物数量", "number"], ["available_qty", "可用数量", "number"], ["reserved_qty", "预占数量", "number"], ["frozen_qty", "冻结数量", "number"], ["in_transit_qty", "在途数量", "number"]],
    actions: ["查看", "修改", "导出", "冻结", "调整"],
    rows: [["华东一仓", "自营", "SKU-PK-1182", "B20260601", "可用", "1200", "920", "180", "100", "0"], ["华南中心仓", "自营", "SKU-EL-1008", "E20260612", "可用", "680", "260", "320", "100", "0"], ["华东一仓", "自营", "SKU-PK-2031", "P20260608", "冻结", "300", "0", "0", "300", "0"], ["华北仓", "渠道A", "SKU-WH-3320", "-", "在途", "0", "0", "0", "0", "500"], ["华东一仓", "自营", "SKU-QC-7780", "Q20260603", "不合格", "42", "0", "0", "42", "0"], ["华南中心仓", "自营", "SKU-SR-9012", "S20260604", "待退供", "25", "0", "0", "25", "0"]],
  },
  {
    id: "available",
    title: "可用库存",
    desc: "统一 ATP 口径，给 OMS、采购、调拨等系统查看可承诺库存。",
    no: "ATP20260628001",
    filters: [["warehouse", "仓库", "text"], ["sku_code", "SKU", "text"], ["owner", "货主", "text"], ["availability_level", "可用等级", "select", ["全部", "充足", "紧张", "缺货"]]],
    columns: [["warehouse", "仓库"], ["owner", "货主"], ["sku_code", "SKU"], ["available_qty", "可用数量"], ["reserved_qty", "预占"], ["frozen_qty", "冻结"], ["expected_inbound_qty", "预计入库"], ["in_transit_qty", "在途"], ["availability_level", "可用等级"]],
    form: [["warehouse", "仓库", "text"], ["owner", "货主", "text"], ["sku_code", "SKU", "text"], ["available_qty", "可用数量", "number"], ["reserved_qty", "预占数量", "number"], ["frozen_qty", "冻结数量", "number"], ["expected_inbound_qty", "预计入库", "number"], ["in_transit_qty", "在途数量", "number"], ["availability_level", "可用等级", "select", ["充足", "紧张", "缺货"]]],
    actions: ["查看", "修改", "导出", "刷新ATP"],
    rows: [["华东一仓", "自营", "SKU-PK-1182", "920", "180", "100", "600", "0", "充足"], ["华南中心仓", "自营", "SKU-EL-1008", "260", "320", "100", "400", "0", "紧张"], ["华东一仓", "自营", "SKU-PK-2031", "0", "0", "300", "0", "0", "缺货"], ["华北仓", "渠道A", "SKU-WH-3320", "0", "0", "0", "0", "500", "缺货"]],
  },
  {
    id: "reservations",
    title: "预占管理",
    desc: "查看销售、调拨、退供等库存预占，支持释放、关闭和重试。",
    no: "RSV20260628001",
    filters: [["reservation_no", "预占单号", "text"], ["source_order_no", "来源单号", "text"], ["reservation_type", "预占类型", "select", ["全部", "销售", "调拨", "退供"]], ["reservation_status", "状态", "select", ["全部", "已创建", "已预占", "部分消耗", "已消耗", "已释放", "已关闭", "失败"]]],
    columns: [["reservation_no", "预占单号"], ["source_system", "来源系统"], ["source_order_no", "来源单号"], ["reservation_type", "类型"], ["sku_code", "SKU"], ["requested_qty", "请求数量"], ["reserved_qty", "预占数量"], ["reservation_status", "状态"], ["expire_at", "过期时间"]],
    form: [["reservation_no", "预占单号", "text"], ["source_system", "来源系统", "select", ["OMS", "调拨", "采购", "BMS"]], ["source_order_no", "来源单号", "text"], ["reservation_type", "预占类型", "select", ["销售", "调拨", "退供"]], ["sku_code", "SKU", "text"], ["requested_qty", "请求数量", "number"], ["reserved_qty", "预占数量", "number"], ["reservation_status", "状态", "select", ["已创建", "已预占", "部分消耗", "已消耗", "已释放", "已关闭", "失败"]], ["expire_at", "过期时间", "datetime-local"]],
    actions: ["查看", "修改", "释放", "关闭", "重试"],
    rows: [["RSV20260628001", "OMS", "SO20260628008", "销售", "SKU-PK-1182", "120", "120", "已预占", "2026-06-28 23:59"], ["RSV20260627006", "OMS", "SO20260627011", "销售", "SKU-EL-1008", "300", "300", "部分消耗", "2026-06-29 12:00"], ["RSV20260626003", "调拨", "TR20260626002", "调拨", "SKU-WH-3320", "500", "500", "已预占", "2026-06-30 18:00"], ["RSV20260625009", "采购", "SRR20260625001", "退供", "SKU-SR-9012", "25", "0", "失败", "2026-06-26 18:00"]],
  },
  {
    id: "freezes",
    title: "冻结解冻",
    desc: "冻结质检、盘点、风控或异常库存，并支持审批后解冻。",
    no: "FRZ20260628001",
    filters: [["freeze_no", "冻结单号", "text"], ["freeze_reason", "冻结原因", "select", ["全部", "质检", "盘点", "风控", "异常", "人工"]], ["freeze_status", "冻结状态", "select", ["全部", "草稿", "待审批", "已冻结", "部分解冻", "已解冻", "已取消"]], ["approval_status", "审批状态", "select", ["全部", "草稿", "待审批", "已批准", "已驳回"]]],
    columns: [["freeze_no", "冻结单号"], ["warehouse", "仓库"], ["sku_code", "SKU"], ["freeze_qty", "冻结数量"], ["freeze_reason", "原因"], ["freeze_status", "冻结状态"], ["approval_status", "审批状态"], ["created_by", "创建人"], ["created_at", "创建时间"]],
    form: [["freeze_no", "冻结单号", "text"], ["warehouse", "仓库", "text"], ["sku_code", "SKU", "text"], ["freeze_qty", "冻结数量", "number"], ["freeze_reason", "原因", "select", ["质检", "盘点", "风控", "异常", "人工"]], ["freeze_status", "冻结状态", "select", ["草稿", "待审批", "已冻结", "部分解冻", "已解冻", "已取消"]], ["approval_status", "审批状态", "select", ["草稿", "待审批", "已批准", "已驳回"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "提交", "审批", "解冻", "取消"],
    rows: [["FRZ20260628001", "华东一仓", "SKU-PK-2031", "300", "质检", "已冻结", "已批准", "库存运营", "2026-06-28 09:20"], ["FRZ20260627004", "华南中心仓", "SKU-EL-1008", "100", "风控", "待审批", "待审批", "库存运营", "2026-06-27 16:40"], ["FRZ20260625002", "华东一仓", "SKU-QC-7780", "42", "异常", "部分解冻", "已批准", "质量人员", "2026-06-25 11:00"]],
  },
  {
    id: "adjustments",
    title: "库存调整",
    desc: "处理盘盈、盘亏、差异修正和红冲，关键动作审批留痕。",
    no: "ADJ20260628001",
    filters: [["adjustment_no", "调整单号", "text"], ["adjustment_type", "调整类型", "select", ["全部", "盘盈", "盘亏", "差异修正", "红冲"]], ["adjustment_status", "调整状态", "select", ["全部", "草稿", "待审批", "已批准", "已执行", "已驳回", "已取消"]], ["approval_status", "审批状态", "select", ["全部", "草稿", "待审批", "已批准", "已驳回"]]],
    columns: [["adjustment_no", "调整单号"], ["warehouse", "仓库"], ["sku_code", "SKU"], ["adjustment_type", "类型"], ["adjust_qty", "调整数量"], ["adjustment_reason", "原因"], ["adjustment_status", "调整状态"], ["approval_status", "审批状态"], ["executed_at", "执行时间"]],
    form: [["adjustment_no", "调整单号", "text"], ["warehouse", "仓库", "text"], ["sku_code", "SKU", "text"], ["adjustment_type", "类型", "select", ["盘盈", "盘亏", "差异修正", "红冲"]], ["adjust_qty", "调整数量", "number"], ["adjustment_reason", "原因", "textarea"], ["adjustment_status", "调整状态", "select", ["草稿", "待审批", "已批准", "已执行", "已驳回", "已取消"]], ["approval_status", "审批状态", "select", ["草稿", "待审批", "已批准", "已驳回"]]],
    actions: ["查看", "修改", "提交", "审批", "执行", "取消"],
    rows: [["ADJ20260628001", "华东一仓", "SKU-PK-1182", "盘亏", "-8", "盘点差异", "待审批", "待审批", "-"], ["ADJ20260626005", "华南中心仓", "SKU-EL-1008", "差异修正", "12", "WMS 对账差异修正", "已执行", "已批准", "2026-06-26 15:18"], ["ADJ20260624003", "华北仓", "SKU-WH-3320", "盘盈", "20", "月度盘点", "已批准", "已批准", "-"]],
  },
  {
    id: "ledgers",
    title: "库存流水",
    desc: "只追加库存流水，追溯库存变化前后数量、来源单据和事件。",
    no: "LED20260628001",
    filters: [["ledger_no", "流水号", "text"], ["ledger_type", "流水类型", "select", ["全部", "入库", "出库", "预占", "释放", "冻结", "解冻", "调整", "红冲"]], ["source_order_no", "来源单号", "text"], ["source_system", "来源系统", "select", ["全部", "OMS", "WMS", "PURCHASE", "TRANSFER", "MDM", "BMS"]]],
    columns: [["ledger_no", "流水号"], ["ledger_type", "类型"], ["change_direction", "方向"], ["sku_code", "SKU"], ["qty_delta", "变化数量"], ["before_available_qty", "前可用"], ["after_available_qty", "后可用"], ["source_system", "来源系统"], ["source_order_no", "来源单号"], ["created_at", "创建时间"]],
    form: [["ledger_no", "流水号", "text"], ["ledger_type", "类型", "select", ["入库", "出库", "预占", "释放", "冻结", "解冻", "调整", "红冲"]], ["change_direction", "方向", "select", ["增加", "减少", "占用", "释放"]], ["sku_code", "SKU", "text"], ["qty_delta", "变化数量", "number"], ["before_available_qty", "前可用", "number"], ["after_available_qty", "后可用", "number"], ["source_system", "来源系统", "text"], ["source_order_no", "来源单号", "text"]],
    actions: ["查看", "修改", "导出", "红冲"],
    rows: [["LED20260628001", "预占", "占用", "SKU-PK-1182", "120", "1040", "920", "OMS", "SO20260628008", "2026-06-28 10:05"], ["LED20260628002", "冻结", "减少", "SKU-EL-1008", "100", "360", "260", "库存", "FRZ20260627004", "2026-06-28 10:20"], ["LED20260627008", "入库", "增加", "SKU-PK-1182", "600", "440", "1040", "WMS", "IN20260627006", "2026-06-27 16:40"], ["LED20260626004", "调整", "增加", "SKU-EL-1008", "12", "248", "260", "库存", "ADJ20260626005", "2026-06-26 15:18"]],
  },
  {
    id: "snapshots",
    title: "库存快照",
    desc: "生成日结、月结或手工库存快照，支持重算和导出。",
    no: "SNP20260628001",
    filters: [["snapshot_no", "快照号", "text"], ["snapshot_date", "快照日期", "date"], ["warehouse", "仓库", "text"], ["snapshot_status", "状态", "select", ["全部", "生成中", "已生成", "失败", "已关闭"]]],
    columns: [["snapshot_no", "快照号"], ["snapshot_date", "快照日期"], ["warehouse", "仓库"], ["snapshot_type", "类型"], ["sku_count", "SKU 数"], ["total_on_hand_qty", "实物总量"], ["snapshot_status", "状态"], ["created_at", "创建时间"]],
    form: [["snapshot_no", "快照号", "text"], ["snapshot_date", "快照日期", "date"], ["warehouse", "仓库", "text"], ["snapshot_type", "类型", "select", ["日结", "月结", "手工"]], ["sku_count", "SKU 数", "number"], ["total_on_hand_qty", "实物总量", "number"], ["snapshot_status", "状态", "select", ["生成中", "已生成", "失败", "已关闭"]]],
    actions: ["查看", "修改", "生成", "重算", "导出"],
    rows: [["SNP20260628001", "2026-06-28", "华东一仓", "日结", "1280", "560,200", "已生成", "2026-06-28 02:00"], ["SNP20260628002", "2026-06-28", "华南中心仓", "日结", "860", "320,180", "已生成", "2026-06-28 02:10"], ["SNP2026060101", "2026-06-01", "全部仓", "月结", "2100", "880,500", "已关闭", "2026-06-01 03:00"]],
  },
  {
    id: "reconciliations",
    title: "库存对账",
    desc: "对比中央库存和 WMS 库存，确认差异并生成调整。",
    no: "REC20260628001",
    filters: [["reconciliation_no", "对账单号", "text"], ["warehouse", "仓库", "text"], ["recon_date", "对账日期", "date"], ["recon_status", "状态", "select", ["全部", "草稿", "对账中", "有差异", "已确认", "已关闭"]]],
    columns: [["reconciliation_no", "对账单号"], ["warehouse", "仓库"], ["recon_date", "对账日期"], ["central_qty", "中央数"], ["wms_qty", "WMS 数"], ["diff_count", "差异行"], ["recon_status", "状态"], ["created_by", "创建人"], ["created_at", "创建时间"]],
    form: [["reconciliation_no", "对账单号", "text"], ["warehouse", "仓库", "text"], ["recon_date", "对账日期", "date"], ["central_qty", "中央数", "number"], ["wms_qty", "WMS 数", "number"], ["diff_count", "差异行", "number"], ["recon_status", "状态", "select", ["草稿", "对账中", "有差异", "已确认", "已关闭"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "生成", "确认", "生成调整"],
    rows: [["REC20260628001", "华东一仓", "2026-06-28", "560200", "560192", "2", "有差异", "仓储运营", "2026-06-28 08:00"], ["REC20260627002", "华南中心仓", "2026-06-27", "320180", "320180", "0", "已确认", "仓储运营", "2026-06-27 08:00"], ["REC20260626003", "华北仓", "2026-06-26", "180050", "180070", "1", "已确认", "仓储运营", "2026-06-26 08:00"]],
  },
  {
    id: "event-logs",
    title: "事件日志",
    desc: "查看库存消费事件、幂等处理结果、失败原因和重放记录。",
    no: "EVT20260628001",
    filters: [["event_id", "事件 ID", "text"], ["event_name", "事件名称", "text"], ["source_system", "来源系统", "select", ["全部", "OMS", "WMS", "PURCHASE", "TRANSFER", "MDM", "BMS"]], ["process_status", "处理状态", "select", ["全部", "待处理", "成功", "失败", "已忽略"]]],
    columns: [["event_id", "事件 ID"], ["event_name", "事件名称"], ["source_system", "来源系统"], ["source_order_no", "来源单号"], ["process_status", "处理状态"], ["fail_reason", "失败原因"], ["received_at", "接收时间"], ["processed_at", "处理时间"]],
    form: [["event_id", "事件 ID", "text"], ["event_name", "事件名称", "text"], ["source_system", "来源系统", "select", ["OMS", "WMS", "PURCHASE", "TRANSFER", "MDM", "BMS"]], ["source_order_no", "来源单号", "text"], ["process_status", "处理状态", "select", ["待处理", "成功", "失败", "已忽略"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "重放", "忽略"],
    rows: [["evt-20260628-001", "StockReservationRequested", "OMS", "SO20260628008", "成功", "-", "2026-06-28 10:05", "2026-06-28 10:05"], ["evt-20260628-002", "OutboundShipped", "WMS", "OUT20260628003", "待处理", "-", "2026-06-28 10:30", "-"], ["evt-20260627-009", "InboundPutawayCompleted", "WMS", "IN20260627006", "失败", "库存维度缺失", "2026-06-27 16:38", "2026-06-27 16:39"]],
  },
  {
    id: "settings",
    title: "参数配置",
    desc: "维护可用口径、预占超时、库存维度和对账策略。",
    no: "CFG20260628001",
    filters: [["param_code", "参数编码", "text"], ["param_name", "参数名称", "text"], ["param_group", "参数分组", "select", ["全部", "可用口径", "预占策略", "维度规则", "对账策略"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["param_code", "参数编码"], ["param_name", "参数名称"], ["param_group", "参数分组"], ["param_value", "参数值"], ["status", "状态"], ["updated_by", "更新人"], ["updated_at", "更新时间"]],
    form: [["param_code", "参数编码", "text"], ["param_name", "参数名称", "text"], ["param_group", "参数分组", "select", ["可用口径", "预占策略", "维度规则", "对账策略"]], ["param_value", "参数值", "text"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [["ATP_FORMULA", "可用库存公式", "可用口径", "实物-冻结-预占-不可售", "启用", "系统管理员", "2026-06-20 10:00"], ["RESERVE_TIMEOUT", "预占超时分钟", "预占策略", "1440", "启用", "系统管理员", "2026-06-18 09:30"], ["RECON_DIFF_AUTO_ADJ", "对账差异自动调整", "对账策略", "false", "停用", "系统管理员", "2026-06-12 11:20"]],
  },
  {
    id: "logs",
    title: "操作日志",
    desc: "查询库存关键操作，支持审计和问题追踪。",
    no: "LOG20260628001",
    filters: [["operator", "操作人", "text"], ["object_type", "对象类型", "select", ["全部", "余额", "预占", "冻结", "调整", "对账", "快照", "参数"]], ["action_type", "动作", "text"], ["result", "结果", "select", ["全部", "成功", "失败"]]],
    columns: [["operator", "操作人"], ["object_type", "对象类型"], ["object_no", "对象单号"], ["action_type", "动作"], ["result", "结果"], ["fail_reason", "失败原因"], ["created_at", "操作时间"]],
    form: [["operator", "操作人", "text"], ["object_type", "对象类型", "text"], ["object_no", "对象单号", "text"], ["action_type", "动作", "text"], ["result", "结果", "select", ["成功", "失败"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "导出"],
    rows: [["库存运营", "冻结", "FRZ20260628001", "提交审批", "成功", "-", "2026-06-28 09:20"], ["系统", "预占", "RSV20260628001", "预占", "成功", "-", "2026-06-28 10:05"], ["仓储运营", "对账", "REC20260628001", "生成对账", "成功", "-", "2026-06-28 08:00"]],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护库存系统枚举项中文标签、颜色、排序和启停状态。",
    no: "ENUM20260628001",
    filters: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["enum_type", "枚举类型"], ["enum_value", "枚举值"], ["label", "显示名称"], ["sort_no", "排序"], ["status", "状态"], ["updated_by", "更新人"], ["updated_at", "更新时间"]],
    form: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["sort_no", "排序", "number"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "排序", "停用"],
    rows: [["INV_STOCK_STATUS", "AVAILABLE", "可用", "10", "启用", "系统管理员", "2026-06-20 10:20"], ["LEDGER_TYPE", "RESERVE", "预占", "30", "启用", "系统管理员", "2026-06-20 10:18"], ["EVENT_PROCESS_STATUS", "FAILED", "失败", "30", "启用", "系统管理员", "2026-06-18 09:15"]],
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
          <div class="brand-title">中央库存系统原型</div>
          <div class="brand-subtitle">Central Inventory Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules.map((m) => `<button class="nav-item ${m.id === active.id ? "active" : ""}" data-route="/${m.id}"><span class="nav-dot"></span><span>${esc(m.title)}</span></button>`).join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">中央库存系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索 SKU、仓库、单号" />
            <span class="user-chip">库存运营</span>
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
  const stats = [["缺货 SKU", "18", "华东一仓 7 个"], ["预占失败", "5", "多为可用不足"], ["冻结库存", "442", "质检冻结 300"], ["对账差异", "3", "待生成调整"], ["调整待审", "4", "盘点差异 2 单"]];
  return `
    ${head(modules[0])}
    <div class="stats-grid">${stats.map(([a, b, c]) => `<div class="stat-card"><div class="stat-title">${a}</div><div class="stat-value">${b}</div><div class="stat-note">${c}</div></div>`).join("")}</div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">库存账本闭环</h2>
        <div class="flow-steps">
          ${["库存余额初始化", "可用库存计算", "库存预占", "出库扣减/入库增加", "冻结解冻", "库存调整", "流水追溯", "快照与对账"].map((x, i) => `<div class="flow-step"><span>${i + 1}. ${x}</span><span>进入</span></div>`).join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>预占</td><td>RSV20260625009</td><td>${tag("失败", "red")}</td></tr>
            <tr><td>冻结</td><td>FRZ20260627004</td><td>${tag("待审批", "amber")}</td></tr>
            <tr><td>对账</td><td>REC20260628001</td><td>${tag("有差异", "purple")}</td></tr>
            <tr><td>调整</td><td>ADJ20260628001</td><td>${tag("待审批", "amber")}</td></tr>
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
    const danger = ["释放", "关闭", "取消", "停用", "忽略", "红冲"].includes(a) ? "danger" : "";
    return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(a)}" data-no="${esc(primaryNo(mod, record))}">${esc(a)}</button>`;
  }).join("");
}

function cell(value) {
  const raw = String(value || "");
  if (["待审批", "已创建", "草稿", "生成中", "对账中", "待处理", "紧张"].includes(raw)) return tag(raw, "amber");
  if (["可用", "已预占", "已消耗", "已解冻", "已批准", "已执行", "已生成", "已确认", "成功", "启用", "充足"].includes(raw)) return tag(raw, "green");
  if (["冻结", "不合格", "待退供", "失败", "已驳回", "已取消", "停用", "缺货"].includes(raw)) return tag(raw, "red");
  if (["在途", "部分消耗", "部分解冻", "有差异", "红冲"].includes(raw)) return tag(raw, "purple");
  return esc(raw);
}

function tag(value, color = "") {
  return `<span class="tag ${color}">${esc(value)}</span>`;
}

function primaryNo(mod, record) {
  const key = mod.columns.find(([k]) => k.endsWith("_no") || k === "event_id")?.[0] || mod.columns[0][0];
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
          <h2 class="detail-title">库存明细</h2>
          <table>
            <thead><tr><th>仓库</th><th>SKU</th><th>批次</th><th>实物</th><th>可用</th><th>预占</th><th>冻结</th></tr></thead>
            <tbody>
              <tr><td>华东一仓</td><td>SKU-PK-1182</td><td>B20260601</td><td>1200</td><td>920</td><td>180</td><td>100</td></tr>
              <tr><td>华南中心仓</td><td>SKU-EL-1008</td><td>E20260612</td><td>680</td><td>260</td><td>320</td><td>100</td></tr>
              <tr><td>华北仓</td><td>SKU-WH-3320</td><td>-</td><td>0</td><td>0</td><td>0</td><td>0</td></tr>
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
          <h2 class="detail-title">库存流转</h2>
          <div class="timeline">${["接收事件/命令", "校验幂等与库存维度", "加锁计算可用", "更新余额并追加流水", "发布库存结果事件"].map((s, i) => `<div class="timeline-item"><div class="timeline-main">${s}</div><div class="timeline-sub">2026-06-${24 + i} ${9 + i}:00，记录库存操作日志</div></div>`).join("")}</div>
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
        <div class="field"><label>备注</label><textarea placeholder="记录库存原因、审批意见、对账说明或异常说明">${mode === "edit" ? "根据当前库存业务情况调整后保存。" : ""}</textarea></div>
      </div>
      <div class="form-actions">
        <button class="btn primary" data-action="save-form" data-module="${mod.id}">保存</button>
        <button class="btn" data-route="/${mod.id}">取消</button>
      </div>
    </div>
  `;
}

function formField(mod, record, [key, label, type, options], mode) {
  const defaultValue = mode === "new" && (key.endsWith("_no") || key === "event_id") ? mod.no : "";
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
