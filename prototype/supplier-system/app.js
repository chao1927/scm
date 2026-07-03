const modules = [
  { id: "workbench", title: "供应商工作台", desc: "集中查看订单待办、ASN、退供、对账、质量问题和评分预警。" },
  {
    id: "profiles",
    title: "供应商档案",
    desc: "查看主数据同步来的供应商资料快照，支持申请资料变更。",
    no: "SUP20260628001",
    filters: [["supplier_code", "供应商编码", "text"], ["supplier_name", "供应商名称", "text"], ["supplier_level", "等级", "select", ["全部", "A", "B", "C", "D", "E"]], ["cooperation_status", "合作状态", "select", ["全部", "准入", "合作中", "暂停", "冻结", "淘汰"]]],
    columns: [["supplier_code", "供应商编码"], ["supplier_name", "供应商名称"], ["supplier_type", "类型"], ["supplier_level", "等级"], ["contact_name", "联系人"], ["settlement_method", "结算方式"], ["cooperation_status", "合作状态"], ["synced_at", "同步时间"]],
    form: [["supplier_code", "供应商编码", "text"], ["supplier_name", "供应商名称", "text"], ["supplier_type", "类型", "select", ["生产商", "贸易商", "服务商", "物流商"]], ["supplier_level", "等级", "select", ["A", "B", "C", "D", "E"]], ["contact_name", "联系人", "text"], ["contact_mobile", "联系电话", "text"], ["settlement_method", "结算方式", "select", ["月结", "票到付款", "预付款", "货到付款"]], ["cooperation_status", "合作状态", "select", ["准入", "合作中", "暂停", "冻结", "淘汰"]]],
    actions: ["查看", "修改", "申请变更", "冻结"],
    rows: [["SUP-001", "苏州蓝海材料", "生产商", "A", "沈澜", "月结", "合作中", "2026-06-28 09:10"], ["SUP-002", "深圳芯合电子", "贸易商", "B", "梁川", "票到付款", "合作中", "2026-06-27 16:20"], ["SUP-003", "杭州启明包装", "生产商", "C", "何悦", "月结", "暂停", "2026-06-25 11:04"], ["SUP-004", "上海良材", "服务商", "D", "董林", "货到付款", "冻结", "2026-06-23 10:18"], ["SUP-005", "宁波丰达", "生产商", "B", "方舟", "月结", "合作中", "2026-06-20 14:36"], ["SUP-006", "广州禾盛", "贸易商", "A", "罗宁", "预付款", "准入", "2026-06-18 09:42"]],
  },
  {
    id: "bindings",
    title: "账号绑定",
    desc: "管理供应商用户与供应商主体关系，控制供应商数据隔离范围。",
    no: "BIND20260628001",
    filters: [["user_name", "用户", "text"], ["supplier_name", "供应商", "text"], ["binding_role", "绑定角色", "select", ["全部", "业务", "财务", "质量", "管理员"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["user_name", "用户"], ["mobile", "手机号"], ["email", "邮箱"], ["supplier_name", "绑定供应商"], ["binding_role", "绑定角色"], ["is_primary", "主账号"], ["status", "状态"], ["bound_at", "绑定时间"]],
    form: [["user_name", "用户", "text"], ["mobile", "手机号", "text"], ["email", "邮箱", "text"], ["supplier_name", "绑定供应商", "text"], ["binding_role", "绑定角色", "select", ["业务", "财务", "质量", "管理员"]], ["is_primary", "主账号", "select", ["是", "否"]], ["status", "状态", "select", ["启用", "停用"]]],
    actions: ["查看", "修改", "绑定", "解绑", "启用", "停用"],
    rows: [["沈澜", "13800010001", "shenlan@supplier.com", "苏州蓝海材料", "管理员", "是", "启用", "2026-06-20 10:00"], ["梁川", "13800010002", "liang@supplier.com", "深圳芯合电子", "业务", "是", "启用", "2026-06-21 11:20"], ["周琪", "13800010003", "zhouqi@supplier.com", "杭州启明包装", "财务", "否", "停用", "2026-06-22 13:30"]],
  },
  {
    id: "skus",
    title: "供应商商品",
    desc: "维护供应商可供 SKU、供应商 SKU 编码、MOQ、MPQ 和供货周期。",
    no: "SSKU20260628001",
    filters: [["sku_code", "SKU", "text"], ["supplier_name", "供应商", "text"], ["supply_status", "供货状态", "select", ["全部", "可供", "暂停", "停供"]], ["lead_time_days", "供货周期", "text"]],
    columns: [["supplier_name", "供应商"], ["sku_code", "SKU"], ["sku_name", "商品名称"], ["supplier_sku_code", "供应商 SKU"], ["moq", "MOQ"], ["mpq", "MPQ"], ["lead_time_days", "供货周期"], ["supply_status", "供货状态"]],
    form: [["supplier_name", "供应商", "text"], ["sku_code", "SKU", "text"], ["sku_name", "商品名称", "text"], ["supplier_sku_code", "供应商 SKU", "text"], ["moq", "MOQ", "number"], ["mpq", "MPQ", "number"], ["lead_time_days", "供货周期", "number"], ["supply_status", "供货状态", "select", ["可供", "暂停", "停供"]]],
    actions: ["查看", "修改", "申请变更", "停供"],
    rows: [["苏州蓝海材料", "SKU-PK-1182", "环保包装盒 A 型", "LH-PK-A01", "100", "20", "5天", "可供"], ["深圳芯合电子", "SKU-EL-1008", "控制板 X1", "XH-PCB-88", "50", "10", "12天", "可供"], ["杭州启明包装", "SKU-PK-2031", "缓冲袋 B 型", "QM-BAG-2", "200", "50", "7天", "暂停"]],
  },
  {
    id: "po-confirms",
    title: "订单协同",
    desc: "供应商确认采购订单，或反馈数量、交期、价格差异。",
    no: "SC20260628001",
    filters: [["confirm_no", "协同单号", "text"], ["purchase_order_no", "PO 单号", "text"], ["supplier_name", "供应商", "text"], ["confirm_status", "确认状态", "select", ["全部", "待确认", "已确认", "差异待处理", "已拒绝", "已关闭"]]],
    columns: [["confirm_no", "协同单号"], ["purchase_order_no", "PO 单号"], ["supplier_name", "供应商"], ["buyer", "采购员"], ["confirm_status", "确认状态"], ["confirm_deadline", "确认截止"], ["confirmed_at", "确认时间"], ["diff_type", "差异类型"]],
    form: [["confirm_no", "协同单号", "text"], ["purchase_order_no", "PO 单号", "text"], ["supplier_name", "供应商", "text"], ["buyer", "采购员", "text"], ["confirm_status", "确认状态", "select", ["待确认", "已确认", "差异待处理", "已拒绝", "已关闭"]], ["confirm_deadline", "确认截止", "datetime-local"], ["diff_type", "差异类型", "select", ["无", "数量", "交期", "价格", "其他"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "确认", "拒绝", "反馈差异"],
    rows: [["SC20260628001", "PO20260628001", "苏州蓝海材料", "许诺", "待确认", "2026-06-29 18:00", "-", "-"], ["SC20260627003", "PO20260627006", "深圳芯合电子", "马越", "已确认", "2026-06-28 12:00", "2026-06-27 16:12", "-"], ["SC20260621005", "PO20260621005", "上海良材", "许诺", "差异待处理", "2026-06-22 18:00", "-", "数量"]],
  },
  {
    id: "asns",
    title: "ASN 管理",
    desc: "供应商创建送货预告、预约到仓、发货和打印送货单。",
    no: "ASN20260628001",
    filters: [["asn_no", "ASN 单号", "text"], ["purchase_order_no", "PO 单号", "text"], ["warehouse", "目的仓", "text"], ["asn_status", "状态", "select", ["全部", "草稿", "已提交", "已预约", "已发货", "已到仓", "已收货", "已取消", "已关闭"]]],
    columns: [["asn_no", "ASN 单号"], ["purchase_order_no", "PO 单号"], ["supplier_name", "供应商"], ["warehouse", "目的仓"], ["eta", "预计到仓"], ["ship_at", "发货时间"], ["carrier_name", "承运商"], ["asn_status", "状态"]],
    form: [["asn_no", "ASN 单号", "text"], ["purchase_order_no", "PO 单号", "text"], ["supplier_name", "供应商", "text"], ["warehouse", "目的仓", "text"], ["eta", "预计到仓", "datetime-local"], ["ship_at", "发货时间", "datetime-local"], ["carrier_name", "承运商", "text"], ["tracking_no", "运单号", "text"], ["asn_status", "状态", "select", ["草稿", "已提交", "已预约", "已发货", "已到仓", "已收货", "已取消", "已关闭"]]],
    actions: ["查看", "修改", "提交", "取消", "打印", "发货"],
    rows: [["ASN20260628003", "PO20260627006", "深圳芯合电子", "华南中心仓", "2026-07-03 10:00", "2026-07-02 16:00", "顺丰", "已预约"], ["ASN20260625006", "PO20260625011", "宁波丰达", "华东一仓", "2026-06-25 09:30", "2026-06-24 18:00", "德邦", "已收货"], ["ASN20260628008", "PO20260628001", "苏州蓝海材料", "华东一仓", "2026-07-06 14:00", "-", "安能", "草稿"]],
  },
  {
    id: "returns",
    title: "退供协同",
    desc: "供应商确认退供、签收退货，或反馈退货差异。",
    no: "SRC20260628001",
    filters: [["return_confirm_no", "退供协同单号", "text"], ["supplier_return_no", "退供单号", "text"], ["supplier_name", "供应商", "text"], ["return_status", "状态", "select", ["全部", "待确认", "已确认", "已拒绝", "已签收", "差异待处理", "已关闭"]]],
    columns: [["return_confirm_no", "退供协同单号"], ["supplier_return_no", "退供单号"], ["supplier_name", "供应商"], ["confirmed_qty", "确认数量"], ["signed_qty", "签收数量"], ["return_status", "状态"], ["diff_reason", "差异原因"], ["confirmed_at", "确认时间"]],
    form: [["return_confirm_no", "退供协同单号", "text"], ["supplier_return_no", "退供单号", "text"], ["supplier_name", "供应商", "text"], ["confirmed_qty", "确认数量", "number"], ["signed_qty", "签收数量", "number"], ["return_status", "状态", "select", ["待确认", "已确认", "已拒绝", "已签收", "差异待处理", "已关闭"]], ["return_address", "退货地址", "textarea"], ["diff_reason", "差异原因", "textarea"]],
    actions: ["查看", "修改", "确认", "拒绝", "签收", "反馈差异"],
    rows: [["SRC20260628001", "SRR20260628001", "深圳芯合电子", "20", "0", "待确认", "-", "-"], ["SRC20260626002", "SRR20260626002", "杭州启明包装", "50", "50", "已签收", "-", "2026-06-26 15:18"], ["SRC20260623006", "SRR20260623006", "宁波丰达", "12", "10", "差异待处理", "外箱破损", "2026-06-24 09:30"]],
  },
  {
    id: "recons",
    title: "对账协同",
    desc: "供应商确认对账单、反馈差异并上传发票。",
    no: "RECON20260628001",
    filters: [["reconciliation_no", "对账单号", "text"], ["supplier_name", "供应商", "text"], ["confirm_status", "状态", "select", ["全部", "待确认", "已确认", "差异待处理", "已开票", "已关闭"]], ["invoice_no", "发票号", "text"]],
    columns: [["reconciliation_no", "对账单号"], ["supplier_name", "供应商"], ["bill_amount", "金额"], ["tax_amount", "税额"], ["diff_amount", "差异金额"], ["confirm_status", "状态"], ["invoice_no", "发票号"], ["confirmed_at", "确认时间"]],
    form: [["reconciliation_no", "对账单号", "text"], ["supplier_name", "供应商", "text"], ["bill_amount", "金额", "number"], ["tax_amount", "税额", "number"], ["diff_amount", "差异金额", "number"], ["confirm_status", "状态", "select", ["待确认", "已确认", "差异待处理", "已开票", "已关闭"]], ["invoice_no", "发票号", "text"]],
    actions: ["查看", "修改", "确认", "反馈差异", "上传发票"],
    rows: [["REC202606001", "苏州蓝海材料", "66,850.00", "7,690.00", "0.00", "待确认", "-", "-"], ["REC202606002", "深圳芯合电子", "128,000.00", "14,725.66", "320.00", "差异待处理", "-", "-"], ["REC202606003", "宁波丰达", "24,560.00", "2,824.48", "0.00", "已开票", "INV20260626001", "2026-06-26 10:20"]],
  },
  {
    id: "quality",
    title: "质量协同",
    desc: "查看质检异常、提交整改方案和上传质量附件。",
    no: "QI20260628001",
    filters: [["issue_no", "问题单号", "text"], ["supplier_name", "供应商", "text"], ["issue_type", "问题类型", "select", ["全部", "外观", "规格", "包装", "性能", "证照"]], ["issue_status", "状态", "select", ["全部", "待处理", "整改中", "待审核", "已关闭"]]],
    columns: [["issue_no", "问题单号"], ["supplier_name", "供应商"], ["sku_code", "SKU"], ["source_doc_no", "来源单号"], ["issue_type", "问题类型"], ["severity", "严重程度"], ["issue_status", "状态"], ["deadline", "截止时间"]],
    form: [["issue_no", "问题单号", "text"], ["supplier_name", "供应商", "text"], ["sku_code", "SKU", "text"], ["source_doc_no", "来源单号", "text"], ["issue_type", "问题类型", "select", ["外观", "规格", "包装", "性能", "证照"]], ["severity", "严重程度", "select", ["轻微", "一般", "严重", "致命"]], ["issue_status", "状态", "select", ["待处理", "整改中", "待审核", "已关闭"]], ["deadline", "截止时间", "datetime-local"]],
    actions: ["查看", "修改", "提交整改", "上传附件", "关闭"],
    rows: [["QI20260628001", "深圳芯合电子", "SKU-EL-1008", "QC20260628005", "性能", "严重", "整改中", "2026-07-03 18:00"], ["QI20260625002", "杭州启明包装", "SKU-PK-2031", "QC20260625004", "包装", "一般", "待审核", "2026-06-30 18:00"], ["QI20260622006", "上海良材", "SKU-WH-3320", "SRR20260622001", "规格", "严重", "已关闭", "2026-06-27 18:00"]],
  },
  {
    id: "scores",
    title: "供应商评分",
    desc: "查看综合评分、维度评分、预警原因和人工修正记录。",
    no: "SCORE20260628001",
    filters: [["supplier_name", "供应商", "text"], ["score_period", "评分周期", "text"], ["score_level", "等级", "select", ["全部", "A", "B", "C", "D", "E"]], ["score_status", "状态", "select", ["全部", "待计算", "已计算", "正常", "预警", "整改中", "冻结建议", "已冻结"]]],
    columns: [["supplier_name", "供应商"], ["score_period", "评分周期"], ["total_score", "综合分"], ["score_level", "等级"], ["quality_score", "质量分"], ["price_score", "价格分"], ["delivery_score", "交付分"], ["score_status", "状态"], ["warning_reason", "预警原因"]],
    form: [["supplier_name", "供应商", "text"], ["score_period", "评分周期", "text"], ["period_type", "周期类型", "select", ["月度", "季度", "半年", "年度"]], ["total_score", "综合分", "number"], ["score_level", "等级", "select", ["A", "B", "C", "D", "E"]], ["score_status", "状态", "select", ["待计算", "已计算", "正常", "预警", "整改中", "冻结建议", "已冻结"]], ["warning_reason", "预警原因", "textarea"], ["adjust_reason", "修正原因", "textarea"]],
    actions: ["查看", "修改", "导出", "发起重算", "人工修正"],
    rows: [["苏州蓝海材料", "2026-06", "92.30", "A", "96", "88", "93", "正常", "-"], ["深圳芯合电子", "2026-06", "84.20", "B", "78", "86", "88", "正常", "-"], ["杭州启明包装", "2026-06", "72.60", "C", "68", "80", "70", "预警", "质量不良率偏高"], ["上海良材", "2026-06", "58.40", "E", "52", "62", "57", "冻结建议", "连续两期低于 60 分"]],
  },
  {
    id: "score-rules",
    title: "评分规则",
    desc: "配置质量、价格、交付、响应、异常等评分维度权重和阈值。",
    no: "RULE20260628001",
    filters: [["rule_code", "规则编码", "text"], ["dimension", "维度", "select", ["全部", "质量", "价格", "交付", "响应", "异常"]], ["formula_type", "公式类型", "select", ["全部", "阈值", "比例", "扣分", "人工"]], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["rule_code", "规则编码"], ["dimension", "维度"], ["weight", "权重"], ["formula_type", "公式类型"], ["warning_threshold", "预警阈值"], ["effective_from", "生效日期"], ["status", "状态"], ["updated_at", "更新时间"]],
    form: [["rule_code", "规则编码", "text"], ["dimension", "维度", "select", ["质量", "价格", "交付", "响应", "异常"]], ["weight", "权重", "number"], ["formula_type", "公式类型", "select", ["阈值", "比例", "扣分", "人工"]], ["warning_threshold", "预警阈值", "number"], ["effective_from", "生效日期", "date"], ["status", "状态", "select", ["启用", "停用"]], ["formula_config", "公式配置", "textarea"]],
    actions: ["查看", "修改", "发布", "启用", "停用"],
    rows: [["QUALITY_RATE", "质量", "35%", "比例", "70", "2026-01-01", "启用", "2026-06-10 11:00"], ["PRICE_COMPETE", "价格", "25%", "比例", "70", "2026-01-01", "启用", "2026-06-10 11:00"], ["DELIVERY_ON_TIME", "交付", "25%", "扣分", "70", "2026-01-01", "启用", "2026-06-10 11:00"], ["RESPONSE_TIME", "响应", "10%", "阈值", "70", "2026-01-01", "启用", "2026-06-10 11:00"]],
  },
  {
    id: "rectifications",
    title: "整改管理",
    desc: "管理评分预警、质量异常或人工发起的供应商整改任务。",
    no: "RECT20260628001",
    filters: [["rectification_no", "整改单号", "text"], ["supplier_name", "供应商", "text"], ["source_type", "来源", "select", ["全部", "评分预警", "质量问题", "人工发起"]], ["rectification_status", "状态", "select", ["全部", "待提交", "已提交", "审核通过", "驳回", "已关闭"]]],
    columns: [["rectification_no", "整改单号"], ["supplier_name", "供应商"], ["source_type", "来源"], ["issue_desc", "问题描述"], ["deadline", "截止时间"], ["rectification_status", "状态"], ["submitted_at", "提交时间"], ["reviewed_by", "审核人"]],
    form: [["rectification_no", "整改单号", "text"], ["supplier_name", "供应商", "text"], ["source_type", "来源", "select", ["评分预警", "质量问题", "人工发起"]], ["issue_desc", "问题描述", "textarea"], ["deadline", "截止时间", "datetime-local"], ["rectification_status", "状态", "select", ["待提交", "已提交", "审核通过", "驳回", "已关闭"]], ["reviewed_by", "审核人", "text"]],
    actions: ["查看", "修改", "发起", "提交", "审核", "关闭"],
    rows: [["RECT20260628001", "杭州启明包装", "评分预警", "质量不良率连续两周偏高", "2026-07-05 18:00", "待提交", "-", "质量经理"], ["RECT20260625003", "深圳芯合电子", "质量问题", "控制板性能测试不稳定", "2026-07-03 18:00", "已提交", "2026-06-28 12:20", "质量经理"], ["RECT20260621004", "上海良材", "人工发起", "交付延迟且响应不及时", "2026-06-30 18:00", "驳回", "2026-06-24 10:00", "采购经理"]],
  },
  {
    id: "logs",
    title: "操作日志",
    desc: "查询供应商协同关键操作，支持审计和问题追踪。",
    no: "LOG20260628001",
    filters: [["operator", "操作人", "text"], ["supplier_name", "供应商", "text"], ["object_type", "对象类型", "select", ["全部", "订单协同", "ASN", "退供", "对账", "评分", "整改"]], ["result", "结果", "select", ["全部", "成功", "失败"]]],
    columns: [["operator", "操作人"], ["operator_type", "操作人类型"], ["supplier_name", "供应商"], ["object_type", "对象类型"], ["object_no", "对象单号"], ["action_type", "动作"], ["result", "结果"], ["created_at", "操作时间"]],
    form: [["operator", "操作人", "text"], ["operator_type", "操作人类型", "select", ["内部用户", "供应商用户", "系统"]], ["supplier_name", "供应商", "text"], ["object_type", "对象类型", "text"], ["object_no", "对象单号", "text"], ["action_type", "动作", "text"], ["result", "结果", "select", ["成功", "失败"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "导出"],
    rows: [["沈澜", "供应商用户", "苏州蓝海材料", "订单协同", "SC20260628001", "查看", "成功", "2026-06-28 10:30"], ["梁川", "供应商用户", "深圳芯合电子", "ASN", "ASN20260628003", "提交", "成功", "2026-06-28 10:08"], ["系统", "系统", "杭州启明包装", "评分", "SCORE202606", "生成预警", "成功", "2026-06-28 02:00"]],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护供应商系统枚举项，前端页面读取配置展示中文标签。",
    no: "ENUM20260628001",
    filters: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["enum_type", "枚举类型"], ["enum_value", "枚举值"], ["label", "显示名称"], ["sort_no", "排序"], ["status", "状态"], ["updated_by", "更新人"], ["updated_at", "更新时间"]],
    form: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["sort_no", "排序", "number"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "排序", "停用"],
    rows: [["PO_CONFIRM_STATUS", "DIFF_PENDING", "差异待处理", "30", "启用", "系统管理员", "2026-06-20 10:20"], ["ASN_STATUS", "SHIPPED", "已发货", "40", "启用", "系统管理员", "2026-06-20 10:18"], ["SUPPLIER_SCORE_LEVEL", "A", "A", "10", "启用", "系统管理员", "2026-06-18 09:15"]],
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
          <div class="brand-title">供应商系统原型</div>
          <div class="brand-subtitle">Supplier Collaboration Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules.map((m) => `<button class="nav-item ${m.id === active.id ? "active" : ""}" data-route="/${m.id}"><span class="nav-dot"></span><span>${esc(m.title)}</span></button>`).join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">供应商系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索供应商、单号、SKU" />
            <span class="user-chip">供应商管理员</span>
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
  const stats = [["待确认订单", "9", "2 单即将超时"], ["待发货 ASN", "6", "今日计划 3 单"], ["退供待确认", "3", "质检不良 2 单"], ["对账待确认", "5", "含 1 单差异"], ["评分预警", "4", "1 家冻结建议"]];
  return `
    ${head(modules[0])}
    <div class="stats-grid">${stats.map(([a, b, c]) => `<div class="stat-card"><div class="stat-title">${a}</div><div class="stat-value">${b}</div><div class="stat-note">${c}</div></div>`).join("")}</div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">供应商协同闭环</h2>
        <div class="flow-steps">
          ${["供应商准入与账号绑定", "供应商商品供货能力", "采购订单确认", "ASN 送货预告", "退供与对账协同", "质量整改", "绩效评分与预警"].map((x, i) => `<div class="flow-step"><span>${i + 1}. ${x}</span><span>进入</span></div>`).join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>订单确认</td><td>SC20260628001</td><td>${tag("待确认", "amber")}</td></tr>
            <tr><td>ASN</td><td>ASN20260628003</td><td>${tag("已预约", "green")}</td></tr>
            <tr><td>质量</td><td>QI20260628001</td><td>${tag("整改中", "purple")}</td></tr>
            <tr><td>评分</td><td>杭州启明包装</td><td>${tag("预警", "red")}</td></tr>
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
      <tbody>
        ${rows.map((r) => `<tr>${mod.columns.map(([k]) => `<td title="${esc(r[k])}">${cell(r[k])}</td>`).join("")}<td><div class="table-actions">${rowActions(mod, r)}</div></td></tr>`).join("")}
      </tbody>
    </table>
  `;
}

function rowActions(mod, record) {
  return mod.actions.map((a) => {
    if (a === "查看") return `<button class="btn text" data-route="/${mod.id}/detail/${record.id}">查看</button>`;
    if (a === "修改") return `<button class="btn text" data-route="/${mod.id}/edit/${record.id}">修改</button>`;
    const danger = ["拒绝", "取消", "关闭", "冻结", "解绑", "停用", "停供"].includes(a) ? "danger" : "";
    return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(a)}" data-no="${esc(primaryNo(mod, record))}">${esc(a)}</button>`;
  }).join("");
}

function cell(value) {
  const raw = String(value || "");
  if (["待确认", "草稿", "待提交", "待处理", "待审核", "差异待处理"].includes(raw)) return tag(raw, "amber");
  if (["合作中", "启用", "可供", "已确认", "已预约", "已收货", "已签收", "已开票", "正常", "成功", "审核通过"].includes(raw)) return tag(raw, "green");
  if (["暂停", "冻结", "淘汰", "停用", "停供", "已拒绝", "预警", "冻结建议", "失败", "驳回"].includes(raw)) return tag(raw, "red");
  if (["整改中", "已提交", "已发货", "已到仓"].includes(raw)) return tag(raw, "purple");
  return esc(raw);
}

function tag(value, color = "") {
  return `<span class="tag ${color}">${esc(value)}</span>`;
}

function primaryNo(mod, record) {
  const key = mod.columns.find(([k]) => k.endsWith("_no") || k.endsWith("_code"))?.[0] || mod.columns[0][0];
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
          <h2 class="detail-title">明细信息</h2>
          <table>
            <thead><tr><th>SKU/对象</th><th>说明</th><th>数量/分值</th><th>状态</th><th>备注</th></tr></thead>
            <tbody>
              <tr><td>SKU-PK-1182</td><td>环保包装盒 A 型</td><td>600</td><td>${tag("正常", "green")}</td><td>按约定交付</td></tr>
              <tr><td>SKU-EL-1008</td><td>控制板 X1</td><td>84.2</td><td>${tag("整改中", "purple")}</td><td>质量问题跟进</td></tr>
              <tr><td>评分维度</td><td>质量/价格/交付/响应</td><td>92/88/93/90</td><td>${tag("已计算", "green")}</td><td>月度评分</td></tr>
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
          <h2 class="detail-title">操作时间线</h2>
          <div class="timeline">${["创建记录", "提交协同", "业务确认", "事件发布", "日志归档"].map((s, i) => `<div class="timeline-item"><div class="timeline-main">${s}</div><div class="timeline-sub">2026-06-${24 + i} ${9 + i}:00，记录供应商操作日志</div></div>`).join("")}</div>
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
        <div class="field"><label>备注</label><textarea placeholder="记录协同说明、差异原因、整改意见或审批意见">${mode === "edit" ? "根据当前协同情况调整后保存。" : ""}</textarea></div>
      </div>
      <div class="form-actions">
        <button class="btn primary" data-action="save-form" data-module="${mod.id}">保存</button>
        <button class="btn" data-route="/${mod.id}">取消</button>
      </div>
    </div>
  `;
}

function formField(mod, record, [key, label, type, options], mode) {
  const defaultValue = mode === "new" && (key.endsWith("_no") || key.endsWith("_code")) ? mod.no : "";
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
