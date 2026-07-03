const modules = [
  { id: "workbench", title: "主数据工作台", desc: "集中查看待审核、发布失败、导入失败、数据质量问题和近期变更。" },
  {
    id: "types",
    title: "主数据类型",
    desc: "配置供应链系统中有哪些主数据类型，以及审核、版本、发布能力。",
    no: "TYPE20260628001",
    filters: [["type_code", "类型编码", "text"], ["type_name", "类型名称", "text"], ["domain", "所属领域", "select", ["全部", "商品", "伙伴", "仓储", "物流", "组织", "财务"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["type_code", "类型编码"], ["type_name", "类型名称"], ["domain", "所属领域"], ["parent_type_code", "上级类型"], ["approval_required", "需审核"], ["version_enabled", "启用版本"], ["publish_enabled", "可发布"], ["status", "状态"]],
    form: [["type_code", "类型编码", "text"], ["type_name", "类型名称", "text"], ["domain", "所属领域", "select", ["商品", "伙伴", "仓储", "物流", "组织", "财务"]], ["parent_type_code", "上级类型", "text"], ["approval_required", "需审核", "select", ["是", "否"]], ["version_enabled", "启用版本", "select", ["是", "否"]], ["publish_enabled", "可发布", "select", ["是", "否"]], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [["SPU", "SPU", "商品", "-", "是", "是", "是", "启用"], ["SKU", "SKU", "商品", "SPU", "是", "是", "是", "启用"], ["SUPPLIER", "供应商", "伙伴", "-", "是", "是", "是", "启用"], ["WAREHOUSE", "仓库", "仓储", "-", "是", "是", "是", "启用"], ["CARRIER", "物流商", "物流", "-", "是", "是", "是", "启用"], ["TAX_RATE", "税率", "财务", "-", "是", "是", "是", "停用"]],
  },
  {
    id: "fields",
    title: "字段模板",
    desc: "配置不同主数据类型的字段、必填、唯一、枚举、引用和关键字段规则。",
    no: "FLD20260628001",
    filters: [["type_code", "主数据类型", "select", ["全部", "SPU", "SKU", "SUPPLIER", "WAREHOUSE", "CARRIER", "OWNER"]], ["field_code", "字段编码", "text"], ["field_type", "字段类型", "select", ["全部", "字符串", "数字", "日期", "枚举", "布尔", "引用", "JSON"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["type_code", "主数据类型"], ["field_code", "字段编码"], ["field_name", "字段名称"], ["field_type", "字段类型"], ["required", "必填"], ["unique_flag", "唯一"], ["critical_flag", "关键字段"], ["visible_in_list", "列表展示"], ["status", "状态"]],
    form: [["type_code", "主数据类型", "text"], ["field_code", "字段编码", "text"], ["field_name", "字段名称", "text"], ["field_type", "字段类型", "select", ["字符串", "数字", "日期", "枚举", "布尔", "引用", "JSON"]], ["required", "必填", "select", ["是", "否"]], ["unique_flag", "唯一", "select", ["是", "否"]], ["critical_flag", "关键字段", "select", ["是", "否"]], ["visible_in_list", "列表展示", "select", ["是", "否"]], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "新增字段", "排序", "停用"],
    rows: [["SKU", "sku_code", "SKU编码", "字符串", "是", "是", "是", "是", "启用"], ["SKU", "stock_unit", "库存单位", "枚举", "是", "否", "是", "是", "启用"], ["SUPPLIER", "tax_no", "税号", "字符串", "是", "是", "是", "是", "启用"], ["WAREHOUSE", "temperature_zone", "温区", "枚举", "否", "否", "否", "是", "启用"]],
  },
  {
    id: "code-rules",
    title: "编码规则",
    desc: "配置自动编码的前缀、日期格式、流水号长度和重置周期。",
    no: "RULE20260628001",
    filters: [["rule_code", "规则编码", "text"], ["rule_name", "规则名称", "text"], ["apply_type", "适用类型", "select", ["全部", "SPU", "SKU", "SUPPLIER", "WAREHOUSE", "CARRIER"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["rule_code", "规则编码"], ["rule_name", "规则名称"], ["apply_type", "适用类型"], ["prefix", "前缀"], ["date_pattern", "日期格式"], ["sequence_length", "流水长度"], ["reset_cycle", "重置周期"], ["status", "状态"]],
    form: [["rule_code", "规则编码", "text"], ["rule_name", "规则名称", "text"], ["apply_type", "适用类型", "text"], ["prefix", "前缀", "text"], ["date_pattern", "日期格式", "text"], ["sequence_length", "流水长度", "number"], ["reset_cycle", "重置周期", "select", ["不重置", "每日", "每月", "每年"]], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "启用", "停用", "预览"],
    rows: [["SKU_RULE", "SKU编码规则", "SKU", "SKU", "yyyyMMdd", "4", "每日", "启用"], ["SUP_RULE", "供应商编码规则", "SUP", "SUP", "yyyy", "5", "每年", "启用"], ["WH_RULE", "仓库编码规则", "WAREHOUSE", "WH", "yyyy", "3", "不重置", "启用"]],
  },
  {
    id: "products",
    title: "商品主数据",
    desc: "管理 SPU/SKU、类目、品牌、属性、条码、包装、单位和版本。",
    no: "SKU20260628001",
    filters: [["data_code", "商品编码", "text"], ["data_name", "商品名称", "text"], ["category", "类目", "text"], ["data_status", "状态", "select", ["全部", "草稿", "待审核", "审核驳回", "已启用", "变更待审核", "已冻结", "已停用", "已淘汰"]]],
    columns: [["data_code", "编码"], ["data_name", "名称"], ["product_type", "类型"], ["category", "类目"], ["brand", "品牌"], ["data_status", "状态"], ["current_version", "版本"], ["updated_at", "更新时间"]],
    form: [["data_code", "编码", "text"], ["data_name", "名称", "text"], ["product_type", "类型", "select", ["SPU", "SKU"]], ["category", "类目", "text"], ["brand", "品牌", "text"], ["barcode", "条码", "text"], ["stock_unit", "库存单位", "text"], ["data_status", "状态", "select", ["草稿", "待审核", "审核驳回", "已启用", "变更待审核", "已冻结", "已停用", "已淘汰"]], ["current_version", "版本", "number"]],
    actions: ["查看", "修改", "提交审核", "启用", "停用"],
    rows: [["SKU-PK-1182", "环保包装盒 A 型", "SKU", "包装材料", "蓝海", "已启用", "6", "2026-06-28 09:20"], ["SKU-EL-1008", "控制板 X1", "SKU", "电子元件", "芯合", "变更待审核", "3", "2026-06-27 16:12"], ["SPU-WH-3320", "仓储标签纸", "SPU", "仓储耗材", "丰达", "已启用", "4", "2026-06-20 11:00"]],
  },
  {
    id: "partners",
    title: "合作伙伴主数据",
    desc: "管理供应商、客户、货主、物流商等业务主体资料和状态。",
    no: "PART20260628001",
    filters: [["data_code", "伙伴编码", "text"], ["data_name", "伙伴名称", "text"], ["partner_type", "伙伴类型", "select", ["全部", "供应商", "客户", "货主", "物流商"]], ["data_status", "状态", "select", ["全部", "草稿", "待审核", "已启用", "已冻结", "已停用", "已淘汰"]]],
    columns: [["data_code", "编码"], ["data_name", "名称"], ["partner_type", "类型"], ["settlement_object", "结算对象"], ["score", "评分"], ["qualification_status", "资质状态"], ["data_status", "状态"], ["updated_at", "更新时间"]],
    form: [["data_code", "编码", "text"], ["data_name", "名称", "text"], ["partner_type", "类型", "select", ["供应商", "客户", "货主", "物流商"]], ["settlement_object", "结算对象", "text"], ["score", "评分", "number"], ["qualification_status", "资质状态", "select", ["待审核", "有效", "即将到期", "已过期"]], ["data_status", "状态", "select", ["草稿", "待审核", "已启用", "已冻结", "已停用", "已淘汰"]]],
    actions: ["查看", "修改", "审核", "启用", "停用"],
    rows: [["SUP-001", "苏州蓝海材料", "供应商", "苏州蓝海材料", "92", "有效", "已启用", "2026-06-28 09:10"], ["CUS-001", "华东KA客户", "客户", "华东KA客户", "-", "有效", "已启用", "2026-06-27 10:30"], ["OWN-001", "自营货主", "货主", "自营公司", "-", "有效", "已启用", "2026-06-20 09:00"], ["CAR-001", "顺丰速运", "物流商", "顺丰速运", "-", "即将到期", "已启用", "2026-06-18 16:20"]],
  },
  {
    id: "warehouses",
    title: "仓储主数据",
    desc: "管理仓库、库区、库位、容量、温区和货主仓关系。",
    no: "WH20260628001",
    filters: [["warehouse_code", "仓库编码", "text"], ["warehouse_name", "仓库名称", "text"], ["location_type", "位置类型", "select", ["全部", "仓库", "库区", "库位", "货主仓关系"]], ["status", "状态", "select", ["全部", "启用", "停用", "冻结"]]],
    columns: [["warehouse_code", "仓库编码"], ["warehouse_name", "仓库名称"], ["zone_location", "库区/库位"], ["location_type", "类型"], ["temperature_zone", "温区"], ["capacity", "容量"], ["owner_scope", "货主范围"], ["status", "状态"]],
    form: [["warehouse_code", "仓库编码", "text"], ["warehouse_name", "仓库名称", "text"], ["zone_location", "库区/库位", "text"], ["location_type", "类型", "select", ["仓库", "库区", "库位", "货主仓关系"]], ["temperature_zone", "温区", "select", ["常温", "冷藏", "冷冻", "恒温"]], ["capacity", "容量", "number"], ["owner_scope", "货主范围", "text"], ["status", "状态", "select", ["启用", "停用", "冻结"]]],
    actions: ["查看", "修改", "提交审核", "启用", "停用"],
    rows: [["WH-EAST-01", "华东一仓", "A区/A-01-02-03", "库位", "常温", "1200", "自营", "启用"], ["WH-SOUTH-01", "华南中心仓", "B区/B-02-01-05", "库位", "常温", "800", "自营", "启用"], ["WH-NORTH-01", "华北仓", "冷链区/C-01", "库区", "冷藏", "500", "渠道A", "冻结"]],
  },
  {
    id: "logistics",
    title: "物流主数据",
    desc: "管理物流商、物流产品、服务区域、面单和轨迹能力。",
    no: "LOGI20260628001",
    filters: [["carrier_code", "物流商编码", "text"], ["carrier_name", "物流商名称", "text"], ["service_type", "服务类型", "select", ["全部", "快递", "快运", "冷链", "同城"]], ["status", "状态", "select", ["全部", "启用", "停用", "待联调"]]],
    columns: [["carrier_code", "物流商编码"], ["carrier_name", "物流商"], ["logistics_product", "物流产品"], ["service_type", "服务类型"], ["service_area", "服务区域"], ["label_template", "面单模板"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["carrier_code", "物流商编码", "text"], ["carrier_name", "物流商", "text"], ["logistics_product", "物流产品", "text"], ["service_type", "服务类型", "select", ["快递", "快运", "冷链", "同城"]], ["service_area", "服务区域", "text"], ["label_template", "面单模板", "text"], ["status", "状态", "select", ["启用", "停用", "待联调"]]],
    actions: ["查看", "修改", "提交审核", "启用", "停用"],
    rows: [["CAR-SF", "顺丰速运", "标快", "快递", "全国", "SF_STD", "启用", "2026-06-28 10:00"], ["CAR-DB", "德邦", "大件快运", "快运", "全国", "DB_BIG", "启用", "2026-06-27 15:20"], ["CAR-CC", "冷链达", "冷链次日", "冷链", "华东", "CC_COLD", "待联调", "2026-06-24 11:10"]],
  },
  {
    id: "approvals",
    title: "主数据审核",
    desc: "处理建档和关键字段变更审核，通过后生成版本并启用。",
    no: "APR20260628001",
    filters: [["type_code", "主数据类型", "text"], ["data_code", "主数据编码", "text"], ["approval_status", "审批状态", "select", ["全部", "未提交", "审批中", "通过", "驳回", "撤回"]], ["applicant", "申请人", "text"]],
    columns: [["approval_no", "审核单号"], ["type_code", "类型"], ["data_code", "编码"], ["data_name", "名称"], ["change_summary", "变更摘要"], ["applicant", "申请人"], ["approval_status", "审批状态"], ["submitted_at", "提交时间"]],
    form: [["approval_no", "审核单号", "text"], ["type_code", "类型", "text"], ["data_code", "编码", "text"], ["data_name", "名称", "text"], ["change_summary", "变更摘要", "textarea"], ["applicant", "申请人", "text"], ["approval_status", "审批状态", "select", ["未提交", "审批中", "通过", "驳回", "撤回"]]],
    actions: ["查看", "修改", "审核通过", "驳回"],
    rows: [["APR20260628001", "SKU", "SKU-EL-1008", "控制板 X1", "变更库存单位和包装规格", "商品运营", "审批中", "2026-06-28 09:40"], ["APR20260627002", "SUPPLIER", "SUP-001", "苏州蓝海材料", "更新税号和结算资料", "供应商管理", "通过", "2026-06-27 14:20"], ["APR20260626003", "WAREHOUSE", "WH-NORTH-01", "华北仓", "新增冷链库区", "仓储运营", "驳回", "2026-06-26 11:00"]],
  },
  {
    id: "publishes",
    title: "主数据发布",
    desc: "查看发布到子系统的版本、事件、目标系统、状态和重试结果。",
    no: "PUB20260628001",
    filters: [["type_code", "主数据类型", "text"], ["data_code", "主数据编码", "text"], ["target_system", "目标系统", "select", ["全部", "PUR", "SRM", "OMS", "WMS", "INV", "BMS"]], ["publish_status", "发布状态", "select", ["全部", "待发布", "发布中", "成功", "失败"]]],
    columns: [["publish_no", "发布记录"], ["type_code", "类型"], ["data_code", "编码"], ["version_no", "版本"], ["target_system", "目标系统"], ["event_name", "事件名"], ["publish_status", "发布状态"], ["retry_count", "重试"], ["failure_reason", "失败原因"]],
    form: [["publish_no", "发布记录", "text"], ["type_code", "类型", "text"], ["data_code", "编码", "text"], ["version_no", "版本", "number"], ["target_system", "目标系统", "select", ["PUR", "SRM", "OMS", "WMS", "INV", "BMS"]], ["event_name", "事件名", "text"], ["publish_status", "发布状态", "select", ["待发布", "发布中", "成功", "失败"]], ["failure_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "重试"],
    rows: [["PUB20260628001", "SKU", "SKU-PK-1182", "6", "OMS", "SkuEnabled", "成功", "0", "-"], ["PUB20260628002", "WAREHOUSE", "WH-EAST-01", "4", "WMS", "WarehouseEnabled", "成功", "0", "-"], ["PUB20260627008", "CARRIER", "CAR-CC", "1", "OMS", "CarrierEnabled", "失败", "2", "目标系统超时"]],
  },
  {
    id: "imports",
    title: "导入导出",
    desc: "批量维护主数据，支持下载模板、导入、导出和查看错误文件。",
    no: "IMP20260628001",
    filters: [["task_no", "任务号", "text"], ["type_code", "主数据类型", "text"], ["import_mode", "导入模式", "select", ["全部", "新增", "更新", "覆盖", "新增或更新"]], ["task_status", "状态", "select", ["全部", "待处理", "处理中", "成功", "部分成功", "失败"]]],
    columns: [["task_no", "任务号"], ["type_code", "主数据类型"], ["file_name", "文件名"], ["import_mode", "导入模式"], ["total_count", "总数"], ["success_count", "成功"], ["failed_count", "失败"], ["task_status", "状态"], ["created_by", "创建人"]],
    form: [["task_no", "任务号", "text"], ["type_code", "主数据类型", "text"], ["file_name", "文件名", "text"], ["import_mode", "导入模式", "select", ["新增", "更新", "覆盖", "新增或更新"]], ["total_count", "总数", "number"], ["success_count", "成功", "number"], ["failed_count", "失败", "number"], ["task_status", "状态", "select", ["待处理", "处理中", "成功", "部分成功", "失败"]]],
    actions: ["查看", "修改", "下载模板", "导入", "导出", "查看错误"],
    rows: [["IMP20260628001", "SKU", "sku_import_0628.xlsx", "新增或更新", "500", "492", "8", "部分成功", "主数据专员"], ["IMP20260627002", "SUPPLIER", "supplier_update.xlsx", "更新", "80", "80", "0", "成功", "供应商管理"], ["IMP20260626003", "WAREHOUSE", "location_import.xlsx", "新增", "1200", "0", "1200", "失败", "仓储运营"]],
  },
  {
    id: "change-logs",
    title: "变更日志",
    desc: "追溯主数据字段级变更，查看旧值、新值、关键字段和操作人。",
    no: "CHG20260628001",
    filters: [["type_code", "主数据类型", "text"], ["data_code", "主数据编码", "text"], ["field_code", "字段编码", "text"], ["critical_flag", "关键字段", "select", ["全部", "是", "否"]]],
    columns: [["change_no", "变更记录"], ["type_code", "类型"], ["data_code", "编码"], ["field_code", "字段"], ["old_value", "旧值"], ["new_value", "新值"], ["critical_flag", "关键字段"], ["operator", "操作人"], ["operated_at", "操作时间"]],
    form: [["change_no", "变更记录", "text"], ["type_code", "类型", "text"], ["data_code", "编码", "text"], ["field_code", "字段", "text"], ["old_value", "旧值", "textarea"], ["new_value", "新值", "textarea"], ["critical_flag", "关键字段", "select", ["是", "否"]], ["operator", "操作人", "text"]],
    actions: ["查看", "修改", "导出"],
    rows: [["CHG20260628001", "SKU", "SKU-EL-1008", "stock_unit", "个", "套", "是", "商品运营", "2026-06-28 09:35"], ["CHG20260627004", "SUPPLIER", "SUP-001", "tax_no", "9132***001", "9132***889", "是", "供应商管理", "2026-06-27 14:10"], ["CHG20260625007", "WAREHOUSE", "WH-EAST-01", "capacity", "1000", "1200", "否", "仓储运营", "2026-06-25 11:05"]],
  },
  {
    id: "logs",
    title: "操作日志",
    desc: "查询主数据新增、编辑、审核、启停、导入、导出和发布重试操作。",
    no: "LOG20260628001",
    filters: [["operator", "操作人", "text"], ["object_type", "对象类型", "select", ["全部", "类型", "字段", "编码规则", "商品", "伙伴", "仓储", "物流", "审核", "发布", "导入"]], ["action_type", "动作", "text"], ["result", "结果", "select", ["全部", "成功", "失败"]]],
    columns: [["operator", "操作人"], ["object_type", "对象类型"], ["object_no", "对象编码"], ["action_type", "动作"], ["result", "结果"], ["fail_reason", "失败原因"], ["created_at", "操作时间"]],
    form: [["operator", "操作人", "text"], ["object_type", "对象类型", "text"], ["object_no", "对象编码", "text"], ["action_type", "动作", "text"], ["result", "结果", "select", ["成功", "失败"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "导出"],
    rows: [["商品运营", "商品", "SKU-EL-1008", "提交审核", "成功", "-", "2026-06-28 09:40"], ["主数据专员", "导入", "IMP20260628001", "导入", "成功", "-", "2026-06-28 08:20"], ["系统", "发布", "PUB20260627008", "发布重试", "失败", "目标系统超时", "2026-06-27 18:20"]],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护字段枚举类型、枚举值、显示名、排序、状态和引用范围。",
    no: "ENUM20260628001",
    filters: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["enum_type", "枚举类型"], ["enum_value", "枚举值"], ["label", "显示名称"], ["sort_no", "排序"], ["status", "状态"], ["updated_by", "更新人"], ["updated_at", "更新时间"]],
    form: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["sort_no", "排序", "number"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "排序", "停用"],
    rows: [["MASTER_DATA_STATUS", "ENABLED", "已启用", "40", "启用", "系统管理员", "2026-06-20 10:18"], ["FIELD_TYPE", "ENUM", "枚举", "70", "启用", "系统管理员", "2026-06-20 10:20"], ["PUBLISH_STATUS", "FAILED", "失败", "40", "启用", "系统管理员", "2026-06-18 09:15"]],
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
          <div class="brand-title">主数据系统原型</div>
          <div class="brand-subtitle">Master Data Management Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules.map((m) => `<button class="nav-item ${m.id === active.id ? "active" : ""}" data-route="/${m.id}"><span class="nav-dot"></span><span>${esc(m.title)}</span></button>`).join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">主数据系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索编码、名称、类型、版本" />
            <span class="user-chip">主数据专员</span>
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
  const stats = [["待审核", "15", "关键字段 6 单"], ["发布失败", "3", "目标系统超时"], ["导入失败", "1", "库位导入失败"], ["数据质量", "8", "疑似重复 4 条"], ["本周变更", "126", "商品 72 条"]];
  return `
    ${head(modules[0])}
    <div class="stats-grid">${stats.map(([a, b, c]) => `<div class="stat-card"><div class="stat-title">${a}</div><div class="stat-value">${b}</div><div class="stat-note">${c}</div></div>`).join("")}</div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">主数据治理闭环</h2>
        <div class="flow-steps">
          ${["配置主数据类型", "配置字段模板", "新增或导入记录", "校验唯一/必填/引用", "提交审核", "生成版本", "发布到子系统", "追溯变更日志"].map((x, i) => `<div class="flow-step"><span>${i + 1}. ${x}</span><span>进入</span></div>`).join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>审核</td><td>SKU-EL-1008</td><td>${tag("审批中", "amber")}</td></tr>
            <tr><td>发布</td><td>CAR-CC</td><td>${tag("失败", "red")}</td></tr>
            <tr><td>导入</td><td>IMP20260628001</td><td>${tag("部分成功", "purple")}</td></tr>
            <tr><td>数据质量</td><td>SUP-001</td><td>${tag("待处理", "amber")}</td></tr>
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
    const danger = ["停用", "驳回", "查看错误"].includes(a) ? "danger" : "";
    return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(a)}" data-no="${esc(primaryNo(mod, record))}">${esc(a)}</button>`;
  }).join("");
}

function cell(value) {
  const raw = String(value || "");
  if (["待审核", "审批中", "未提交", "待发布", "发布中", "待处理", "处理中", "即将到期", "待联调", "部分成功", "草稿", "变更待审核"].includes(raw)) return tag(raw, "amber");
  if (["启用", "已启用", "通过", "成功", "有效", "是"].includes(raw)) return tag(raw, "green");
  if (["停用", "已停用", "已冻结", "已淘汰", "驳回", "失败", "已过期", "否"].includes(raw)) return tag(raw, "red");
  if (["冻结", "审核驳回", "发布失败"].includes(raw)) return tag(raw, "purple");
  return esc(raw);
}

function tag(value, color = "") {
  return `<span class="tag ${color}">${esc(value)}</span>`;
}

function primaryNo(mod, record) {
  const key = mod.columns.find(([k]) => k.endsWith("_code") || k.endsWith("_no") || k === "data_code")?.[0] || mod.columns[0][0];
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
          <h2 class="detail-title">版本与发布</h2>
          <table>
            <thead><tr><th>版本</th><th>变更类型</th><th>变更摘要</th><th>目标系统</th><th>发布状态</th><th>生效时间</th></tr></thead>
            <tbody>
              <tr><td>v6</td><td>修改</td><td>更新包装规格</td><td>OMS/WMS/INV</td><td>${tag("成功", "green")}</td><td>2026-06-28 10:00</td></tr>
              <tr><td>v5</td><td>启用</td><td>资料审核通过</td><td>OMS/WMS/INV</td><td>${tag("成功", "green")}</td><td>2026-06-20 09:00</td></tr>
              <tr><td>v4</td><td>修改</td><td>关键字段变更</td><td>OMS</td><td>${tag("失败", "red")}</td><td>2026-06-18 14:00</td></tr>
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
          <h2 class="detail-title">流转记录</h2>
          <div class="timeline">${["创建草稿", "字段模板校验", "提交审核", "生成版本", "发布子系统", "子系统确认"].map((s, i) => `<div class="timeline-item"><div class="timeline-main">${s}</div><div class="timeline-sub">2026-06-${22 + i} ${9 + i}:00，记录主数据操作日志</div></div>`).join("")}</div>
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
        <div class="field"><label>备注</label><textarea placeholder="记录建档说明、变更原因、审批意见、发布失败原因或数据质量备注">${mode === "edit" ? "根据当前主数据治理情况调整后保存。" : ""}</textarea></div>
      </div>
      <div class="form-actions">
        <button class="btn primary" data-action="save-form" data-module="${mod.id}">保存</button>
        <button class="btn" data-route="/${mod.id}">取消</button>
      </div>
    </div>
  `;
}

function formField(mod, record, [key, label, type, options], mode) {
  const defaultValue = mode === "new" && (key.endsWith("_no") || key.endsWith("_code") || key === "data_code") ? mod.no : "";
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
