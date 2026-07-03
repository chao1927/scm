const modules = [
  {
    id: "workbench",
    title: "采购工作台",
    desc: "集中查看请购、询价、订单、到货和异常待办。",
    icon: "总",
  },
  {
    id: "requisitions",
    title: "请购管理",
    desc: "需求部门提交采购需求，采购侧跟踪审批和转采购进度。",
    no: "PR20260628001",
    filters: [
      ["requisition_no", "请购单号", "text"],
      ["request_org", "需求部门", "text"],
      ["requester", "申请人", "text"],
      ["requisition_status", "请购状态", "select", ["全部", "草稿", "待审批", "已批准", "已转采购", "已关闭"]],
    ],
    columns: [
      ["requisition_no", "请购单号"],
      ["requisition_type", "类型"],
      ["request_org", "需求部门"],
      ["requester", "申请人"],
      ["expected_arrival_date", "期望到货日"],
      ["budget_amount", "预算金额"],
      ["approval_status", "审批状态"],
      ["requisition_status", "请购状态"],
    ],
    form: [
      ["requisition_no", "请购单号", "text"],
      ["requisition_type", "请购类型", "select", ["常规", "紧急", "项目", "补货"]],
      ["request_org", "需求部门", "text"],
      ["requester", "申请人", "text"],
      ["expected_arrival_date", "期望到货日", "date"],
      ["budget_amount", "预算金额", "number"],
      ["currency", "币种", "select", ["CNY", "USD", "EUR"]],
      ["purpose", "采购用途", "textarea"],
    ],
    actions: ["查看", "修改", "提交", "撤回", "关闭"],
    rows: [
      ["PR20260628001", "常规", "研发一部", "陈晨", "2026-07-05", "18,600.00", "待审批", "待审批"],
      ["PR20260627008", "紧急", "华东运营", "林雨", "2026-07-02", "42,300.00", "已批准", "已批准"],
      ["PR20260626012", "补货", "售后中心", "周宁", "2026-07-09", "9,280.00", "草稿", "草稿"],
      ["PR20260625019", "项目", "项目交付部", "赵倩", "2026-07-12", "76,500.00", "已批准", "已转采购"],
      ["PR20260624007", "常规", "仓储部", "王浩", "2026-07-08", "12,800.00", "已驳回", "草稿"],
      ["PR20260623010", "补货", "华南运营", "刘敏", "2026-07-11", "31,600.00", "已批准", "已关闭"],
    ],
  },
  {
    id: "approvals",
    title: "请购审批",
    desc: "审批采购需求，记录通过、驳回和转交意见。",
    no: "APR20260628001",
    filters: [
      ["requisition_no", "请购单号", "text"],
      ["requester", "申请人", "text"],
      ["approval_status", "审批状态", "select", ["全部", "待审批", "已批准", "已驳回"]],
      ["approver", "当前审批人", "text"],
    ],
    columns: [
      ["requisition_no", "请购单号"],
      ["requester", "申请人"],
      ["budget_amount", "预算金额"],
      ["purpose", "用途"],
      ["approver", "当前审批人"],
      ["approval_status", "审批状态"],
      ["submitted_at", "提交时间"],
    ],
    form: [
      ["requisition_no", "请购单号", "text"],
      ["requester", "申请人", "text"],
      ["budget_amount", "预算金额", "number"],
      ["purpose", "用途", "textarea"],
      ["approver", "审批人", "text"],
      ["approval_status", "审批状态", "select", ["待审批", "已批准", "已驳回"]],
    ],
    actions: ["查看", "修改", "通过", "驳回", "转交"],
    rows: [
      ["PR20260628001", "陈晨", "18,600.00", "实验室耗材补充", "采购经理", "待审批", "2026-06-28 09:15"],
      ["PR20260627008", "林雨", "42,300.00", "大促备货", "采购经理", "待审批", "2026-06-27 18:22"],
      ["PR20260624007", "王浩", "12,800.00", "货架标签打印耗材", "采购经理", "已驳回", "2026-06-24 14:05"],
    ],
  },
  {
    id: "rfqs",
    title: "询价单",
    desc: "创建询价，邀请供应商报价，并管理询价生命周期。",
    no: "RFQ20260628001",
    filters: [
      ["rfq_no", "询价单号", "text"],
      ["rfq_type", "询价类型", "select", ["全部", "公开询价", "定向询价", "议价"]],
      ["buyer", "采购员", "text"],
      ["rfq_status", "状态", "select", ["全部", "草稿", "已发布", "报价中", "已截标", "已定标", "已取消", "已关闭"]],
    ],
    columns: [
      ["rfq_no", "询价单号"],
      ["rfq_type", "询价类型"],
      ["category", "采购品类"],
      ["buyer", "采购员"],
      ["supplier_count", "供应商数"],
      ["quote_deadline", "截止时间"],
      ["rfq_status", "状态"],
    ],
    form: [
      ["rfq_no", "询价单号", "text"],
      ["rfq_type", "询价类型", "select", ["公开询价", "定向询价", "议价"]],
      ["category", "采购品类", "text"],
      ["buyer", "采购员", "text"],
      ["supplier_count", "邀请供应商数", "number"],
      ["quote_deadline", "报价截止时间", "datetime-local"],
      ["rfq_status", "状态", "select", ["草稿", "已发布", "报价中", "已截标", "已定标", "已取消", "已关闭"]],
    ],
    actions: ["查看", "修改", "发布", "取消", "关闭"],
    rows: [
      ["RFQ20260628001", "定向询价", "包装材料", "许诺", "4", "2026-06-30 18:00", "报价中"],
      ["RFQ20260626004", "公开询价", "电子元件", "许诺", "8", "2026-06-29 12:00", "已发布"],
      ["RFQ20260622009", "议价", "仓储耗材", "马越", "2", "2026-06-25 17:00", "已定标"],
    ],
  },
  {
    id: "quotations",
    title: "报价管理",
    desc: "维护供应商报价、有效期和附件，作为比价定标依据。",
    no: "QT20260628001",
    filters: [
      ["quotation_no", "报价单号", "text"],
      ["rfq_no", "询价单号", "text"],
      ["supplier_name", "供应商", "text"],
      ["quote_status", "状态", "select", ["全部", "草稿", "已提交", "已确认", "已作废", "未中标", "中标"]],
    ],
    columns: [
      ["quotation_no", "报价单号"],
      ["rfq_no", "询价单号"],
      ["supplier_name", "供应商"],
      ["total_amount", "报价总额"],
      ["currency", "币种"],
      ["valid_to", "有效期至"],
      ["quote_status", "状态"],
    ],
    form: [
      ["quotation_no", "报价单号", "text"],
      ["rfq_no", "询价单号", "text"],
      ["supplier_name", "供应商", "text"],
      ["total_amount", "报价总额", "number"],
      ["currency", "币种", "select", ["CNY", "USD", "EUR"]],
      ["valid_to", "有效期至", "date"],
      ["quote_status", "状态", "select", ["草稿", "已提交", "已确认", "已作废", "未中标", "中标"]],
    ],
    actions: ["查看", "修改", "确认", "作废"],
    rows: [
      ["QT20260628001", "RFQ20260628001", "杭州启明包装", "68,200.00", "CNY", "2026-07-15", "已提交"],
      ["QT20260628002", "RFQ20260628001", "苏州蓝海材料", "66,850.00", "CNY", "2026-07-12", "已确认"],
      ["QT20260626007", "RFQ20260626004", "深圳芯合电子", "128,000.00", "CNY", "2026-07-20", "草稿"],
    ],
  },
  {
    id: "compare",
    title: "比价定标",
    desc: "综合价格、交期、供应商评分和质量表现，选择推荐供应商。",
    no: "CMP20260628001",
    filters: [
      ["compare_no", "比价单号", "text"],
      ["rfq_no", "询价单号", "text"],
      ["decision_supplier", "定标供应商", "text"],
      ["compare_status", "状态", "select", ["全部", "待比价", "已推荐", "已定标", "已驳回"]],
    ],
    columns: [
      ["compare_no", "比价单号"],
      ["rfq_no", "询价单号"],
      ["recommended_supplier", "推荐供应商"],
      ["decision_supplier", "定标供应商"],
      ["supplier_score", "供应商评分"],
      ["compare_status", "状态"],
      ["decided_by", "定标人"],
    ],
    form: [
      ["compare_no", "比价单号", "text"],
      ["rfq_no", "询价单号", "text"],
      ["recommended_supplier", "推荐供应商", "text"],
      ["decision_supplier", "定标供应商", "text"],
      ["supplier_score", "供应商评分", "number"],
      ["decision_reason", "定标理由", "textarea"],
      ["compare_status", "状态", "select", ["待比价", "已推荐", "已定标", "已驳回"]],
    ],
    actions: ["查看", "修改", "生成比价", "定标", "驳回"],
    rows: [
      ["CMP20260628001", "RFQ20260628001", "苏州蓝海材料", "苏州蓝海材料", "92", "已定标", "采购经理"],
      ["CMP20260626004", "RFQ20260626004", "深圳芯合电子", "-", "88", "已推荐", "采购经理"],
      ["CMP20260622009", "RFQ20260622009", "宁波丰达", "宁波丰达", "85", "已定标", "采购总监"],
    ],
  },
  {
    id: "orders",
    title: "采购订单",
    desc: "管理 PO 创建、审批、发布、供应商确认、变更、取消和关闭。",
    no: "PO20260628001",
    filters: [
      ["purchase_order_no", "PO 单号", "text"],
      ["supplier_name", "供应商", "text"],
      ["po_status", "订单状态", "select", ["全部", "草稿", "待审批", "已审批", "待供应商确认", "供应商已确认", "供应商差异", "部分入库", "已完成", "已取消", "已关闭"]],
      ["confirm_status", "确认状态", "select", ["全部", "待确认", "已确认", "差异", "已拒绝"]],
    ],
    columns: [
      ["purchase_order_no", "PO 单号"],
      ["supplier_name", "供应商"],
      ["warehouse", "目的仓"],
      ["tax_included_amount", "含税金额"],
      ["approval_status", "审批状态"],
      ["po_status", "订单状态"],
      ["confirm_status", "确认状态"],
      ["inbound_progress", "入库进度"],
    ],
    form: [
      ["purchase_order_no", "PO 单号", "text"],
      ["purchase_type", "采购类型", "select", ["常规", "紧急", "补货", "项目"]],
      ["supplier_name", "供应商", "text"],
      ["buyer", "采购员", "text"],
      ["warehouse", "目的仓", "text"],
      ["tax_included_amount", "含税金额", "number"],
      ["approval_status", "审批状态", "select", ["草稿", "待审批", "已批准", "已驳回"]],
      ["po_status", "订单状态", "select", ["草稿", "待审批", "已审批", "待供应商确认", "供应商已确认", "供应商差异", "部分入库", "已完成", "已取消", "已关闭"]],
    ],
    actions: ["查看", "修改", "提交", "审批", "发布", "变更", "取消", "关闭"],
    rows: [
      ["PO20260628001", "苏州蓝海材料", "华东一仓", "66,850.00", "已批准", "待供应商确认", "待确认", "0/1200"],
      ["PO20260627006", "深圳芯合电子", "华南中心仓", "128,000.00", "已批准", "部分入库", "已确认", "600/1000"],
      ["PO20260625011", "宁波丰达", "华东一仓", "24,560.00", "已批准", "已完成", "已确认", "300/300"],
      ["PO20260623018", "杭州启明包装", "华北仓", "31,200.00", "待审批", "待审批", "待确认", "0/800"],
      ["PO20260621005", "上海良材", "华东一仓", "9,900.00", "已批准", "供应商差异", "差异", "0/200"],
    ],
  },
  {
    id: "confirms",
    title: "供应商确认",
    desc: "处理供应商确认、拒绝、交期差异和数量差异。",
    no: "SC20260628001",
    filters: [
      ["purchase_order_no", "PO 单号", "text"],
      ["supplier_name", "供应商", "text"],
      ["diff_type", "差异类型", "select", ["全部", "无差异", "交期差异", "数量差异", "价格差异"]],
      ["process_status", "处理状态", "select", ["全部", "待处理", "处理中", "已处理"]],
    ],
    columns: [
      ["purchase_order_no", "PO 单号"],
      ["supplier_name", "供应商"],
      ["confirm_result", "确认结果"],
      ["diff_type", "差异类型"],
      ["promised_date", "承诺交期"],
      ["process_status", "处理状态"],
      ["created_at", "创建时间"],
    ],
    form: [
      ["purchase_order_no", "PO 单号", "text"],
      ["supplier_name", "供应商", "text"],
      ["confirm_result", "确认结果", "select", ["确认", "拒绝", "差异反馈"]],
      ["diff_type", "差异类型", "select", ["无差异", "交期差异", "数量差异", "价格差异"]],
      ["promised_date", "承诺交期", "date"],
      ["process_status", "处理状态", "select", ["待处理", "处理中", "已处理"]],
    ],
    actions: ["查看", "修改", "接受差异", "重新协商", "关闭"],
    rows: [
      ["PO20260628001", "苏州蓝海材料", "差异反馈", "交期差异", "2026-07-06", "待处理", "2026-06-28 10:40"],
      ["PO20260627006", "深圳芯合电子", "确认", "无差异", "2026-07-03", "已处理", "2026-06-27 16:12"],
      ["PO20260621005", "上海良材", "差异反馈", "数量差异", "2026-07-08", "处理中", "2026-06-21 11:25"],
    ],
  },
  {
    id: "inbound",
    title: "到货跟踪",
    desc: "消费 ASN、收货、质检和上架结果，展示 PO 执行进度。",
    no: "INB20260628001",
    filters: [
      ["purchase_order_no", "PO 单号", "text"],
      ["asn_no", "ASN 单号", "text"],
      ["sku_code", "SKU", "text"],
      ["track_status", "状态", "select", ["全部", "待到货", "部分收货", "质检中", "部分上架", "已完成", "异常"]],
    ],
    columns: [
      ["purchase_order_no", "PO 单号"],
      ["sku_code", "SKU"],
      ["asn_no", "ASN"],
      ["inbound_no", "入库单"],
      ["notice_qty", "通知量"],
      ["received_qty", "收货量"],
      ["qualified_qty", "合格量"],
      ["putaway_qty", "上架量"],
      ["track_status", "状态"],
    ],
    form: [
      ["purchase_order_no", "PO 单号", "text"],
      ["sku_code", "SKU", "text"],
      ["asn_no", "ASN", "text"],
      ["inbound_no", "入库单", "text"],
      ["notice_qty", "通知量", "number"],
      ["received_qty", "收货量", "number"],
      ["qualified_qty", "合格量", "number"],
      ["putaway_qty", "上架量", "number"],
      ["track_status", "状态", "select", ["待到货", "部分收货", "质检中", "部分上架", "已完成", "异常"]],
    ],
    actions: ["查看", "修改", "催交", "关闭剩余"],
    rows: [
      ["PO20260627006", "SKU-EL-1008", "ASN20260628003", "IN20260628002", "1000", "600", "580", "420", "部分上架"],
      ["PO20260625011", "SKU-PK-2031", "ASN20260625006", "IN20260625008", "300", "300", "296", "296", "已完成"],
      ["PO20260628001", "SKU-PK-1182", "-", "-", "1200", "0", "0", "0", "待到货"],
    ],
  },
  {
    id: "returns",
    title: "退供申请",
    desc: "发起质检不良、错发、超收等退供应商申请并跟踪处理。",
    no: "SRR20260628001",
    filters: [
      ["supplier_return_no", "退供申请号", "text"],
      ["supplier_name", "供应商", "text"],
      ["return_reason", "退供原因", "select", ["全部", "质检不良", "供应商错发", "超收退回", "合同取消"]],
      ["return_status", "退供状态", "select", ["全部", "草稿", "待审批", "已批准", "已出库", "已取消", "已关闭"]],
    ],
    columns: [
      ["supplier_return_no", "退供申请号"],
      ["supplier_name", "供应商"],
      ["source_po", "来源 PO"],
      ["warehouse", "退货仓"],
      ["return_reason", "退供原因"],
      ["return_qty", "退货数量"],
      ["approval_status", "审批状态"],
      ["return_status", "退供状态"],
    ],
    form: [
      ["supplier_return_no", "退供申请号", "text"],
      ["supplier_name", "供应商", "text"],
      ["source_po", "来源 PO", "text"],
      ["warehouse", "退货仓", "text"],
      ["return_reason", "退供原因", "select", ["质检不良", "供应商错发", "超收退回", "合同取消"]],
      ["return_qty", "退货数量", "number"],
      ["approval_status", "审批状态", "select", ["草稿", "待审批", "已批准", "已驳回"]],
      ["return_status", "退供状态", "select", ["草稿", "待审批", "已批准", "已出库", "已取消", "已关闭"]],
    ],
    actions: ["查看", "修改", "提交", "审批", "取消"],
    rows: [
      ["SRR20260628001", "深圳芯合电子", "PO20260627006", "华南中心仓", "质检不良", "20", "待审批", "待审批"],
      ["SRR20260626002", "杭州启明包装", "PO20260624004", "华东一仓", "供应商错发", "50", "已批准", "已出库"],
      ["SRR20260623006", "宁波丰达", "PO20260621002", "华北仓", "超收退回", "12", "已批准", "已关闭"],
    ],
  },
  {
    id: "prices",
    title: "采购价格",
    desc: "维护供应商商品价格、税率、币种和生效期。",
    no: "PRICE20260628001",
    filters: [
      ["supplier_name", "供应商", "text"],
      ["sku_code", "SKU", "text"],
      ["price_type", "价格类型", "select", ["全部", "标准价", "合同价", "阶梯价"]],
      ["price_status", "状态", "select", ["全部", "草稿", "已启用", "已停用", "已过期"]],
    ],
    columns: [
      ["supplier_name", "供应商"],
      ["sku_code", "SKU"],
      ["price_type", "价格类型"],
      ["unit_price", "未税单价"],
      ["tax_rate", "税率"],
      ["tax_included_price", "含税单价"],
      ["currency", "币种"],
      ["valid_period", "生效期"],
      ["price_status", "状态"],
    ],
    form: [
      ["supplier_name", "供应商", "text"],
      ["sku_code", "SKU", "text"],
      ["price_type", "价格类型", "select", ["标准价", "合同价", "阶梯价"]],
      ["unit_price", "未税单价", "number"],
      ["tax_rate", "税率", "number"],
      ["tax_included_price", "含税单价", "number"],
      ["currency", "币种", "select", ["CNY", "USD", "EUR"]],
      ["valid_period", "生效期", "text"],
      ["price_status", "状态", "select", ["草稿", "已启用", "已停用", "已过期"]],
    ],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [
      ["苏州蓝海材料", "SKU-PK-1182", "合同价", "49.20", "13%", "55.60", "CNY", "2026-06-01 至 2026-12-31", "已启用"],
      ["深圳芯合电子", "SKU-EL-1008", "标准价", "112.40", "13%", "127.01", "CNY", "2026-05-01 至 2026-09-30", "已启用"],
      ["宁波丰达", "SKU-WH-3320", "阶梯价", "8.20", "13%", "9.27", "CNY", "2026-01-01 至 2026-12-31", "已启用"],
    ],
  },
  {
    id: "settings",
    title: "采购参数",
    desc: "维护采购类型、原因码、超收策略、审批阈值和单据规则。",
    no: "CFG20260628001",
    filters: [
      ["param_code", "参数编码", "text"],
      ["param_name", "参数名称", "text"],
      ["param_group", "参数分组", "select", ["全部", "单据规则", "审批规则", "收货策略", "原因码"]],
      ["param_status", "状态", "select", ["全部", "已启用", "已停用"]],
    ],
    columns: [
      ["param_code", "参数编码"],
      ["param_name", "参数名称"],
      ["param_group", "参数分组"],
      ["param_value", "参数值"],
      ["param_status", "状态"],
      ["updated_by", "更新人"],
      ["updated_at", "更新时间"],
    ],
    form: [
      ["param_code", "参数编码", "text"],
      ["param_name", "参数名称", "text"],
      ["param_group", "参数分组", "select", ["单据规则", "审批规则", "收货策略", "原因码"]],
      ["param_value", "参数值", "text"],
      ["param_status", "状态", "select", ["已启用", "已停用"]],
      ["remark", "备注", "textarea"],
    ],
    actions: ["查看", "修改", "启用", "停用"],
    rows: [
      ["PO_APPROVAL_LIMIT", "PO 审批阈值", "审批规则", "50000", "已启用", "系统管理员", "2026-06-20 09:30"],
      ["OVER_RECEIVE_RATE", "超收比例", "收货策略", "5%", "已启用", "系统管理员", "2026-06-18 16:20"],
      ["PO_NO_RULE", "PO 编码规则", "单据规则", "POyyyyMMdd####", "已启用", "系统管理员", "2026-06-10 11:00"],
    ],
  },
  {
    id: "logs",
    title: "操作日志",
    desc: "查询采购关键写操作，满足审计和问题追踪。",
    no: "LOG20260628001",
    filters: [
      ["operator", "操作人", "text"],
      ["object_type", "对象类型", "select", ["全部", "请购单", "询价单", "报价单", "采购订单", "退供申请", "采购价格"]],
      ["object_no", "对象单号", "text"],
      ["result", "结果", "select", ["全部", "成功", "失败"]],
    ],
    columns: [
      ["operator", "操作人"],
      ["object_type", "对象类型"],
      ["object_no", "对象单号"],
      ["action", "动作"],
      ["result", "结果"],
      ["ip", "来源 IP"],
      ["operated_at", "操作时间"],
    ],
    form: [
      ["operator", "操作人", "text"],
      ["object_type", "对象类型", "text"],
      ["object_no", "对象单号", "text"],
      ["action", "动作", "text"],
      ["result", "结果", "select", ["成功", "失败"]],
      ["ip", "来源 IP", "text"],
      ["operated_at", "操作时间", "datetime-local"],
    ],
    actions: ["查看", "修改"],
    rows: [
      ["许诺", "采购订单", "PO20260628001", "发布", "成功", "10.10.3.22", "2026-06-28 10:28"],
      ["采购经理", "比价单", "CMP20260628001", "定标", "成功", "10.10.3.18", "2026-06-28 09:56"],
      ["陈晨", "请购单", "PR20260628001", "提交", "成功", "10.10.2.41", "2026-06-28 09:15"],
    ],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护采购系统页面可配置枚举，支持启停和排序。",
    no: "ENUM20260628001",
    filters: [
      ["enum_type", "枚举类型", "text"],
      ["enum_value", "枚举值", "text"],
      ["label", "显示名称", "text"],
      ["enum_status", "状态", "select", ["全部", "已启用", "已停用"]],
    ],
    columns: [
      ["enum_type", "枚举类型"],
      ["enum_value", "枚举值"],
      ["label", "显示名称"],
      ["sort_no", "排序"],
      ["enum_status", "状态"],
      ["updated_by", "更新人"],
      ["updated_at", "更新时间"],
    ],
    form: [
      ["enum_type", "枚举类型", "text"],
      ["enum_value", "枚举值", "text"],
      ["label", "显示名称", "text"],
      ["sort_no", "排序", "number"],
      ["enum_status", "状态", "select", ["已启用", "已停用"]],
      ["remark", "备注", "textarea"],
    ],
    actions: ["查看", "修改", "停用"],
    rows: [
      ["PURCHASE_ORDER_STATUS", "WAIT_SUPPLIER_CONFIRM", "待供应商确认", "40", "已启用", "系统管理员", "2026-06-19 14:20"],
      ["RFQ_STATUS", "QUOTE_OPEN", "报价中", "30", "已启用", "系统管理员", "2026-06-19 14:18"],
      ["RETURN_REASON", "QC_FAILED", "质检不良", "10", "已启用", "系统管理员", "2026-06-15 10:12"],
    ],
  },
];

const state = {
  filters: {},
  page: {},
  pageSize: 5,
};

const app = document.querySelector("#app");

function makeRecords(mod) {
  if (!mod.columns || !mod.rows) return [];
  return mod.rows.map((row, index) => {
    const record = { id: `${mod.id}-${index + 1}` };
    mod.columns.forEach(([key], idx) => {
      record[key] = row[idx] || "";
    });
    return record;
  });
}

const recordsByModule = Object.fromEntries(
  modules.filter((mod) => mod.columns).map((mod) => [mod.id, makeRecords(mod)])
);

function esc(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function currentRoute() {
  const raw = window.location.hash.replace(/^#\/?/, "");
  const [moduleId = "workbench", view = "list", recordId = ""] = raw.split("/");
  return { moduleId, view, recordId };
}

function moduleById(id) {
  return modules.find((mod) => mod.id === id) || modules[0];
}

function setRoute(path) {
  window.location.hash = path;
}

function shell(activeId, content) {
  const active = moduleById(activeId);
  app.innerHTML = `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-title">采购系统原型</div>
          <div class="brand-subtitle">Purchase Management Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules
            .map(
              (mod) => `
                <button class="nav-item ${mod.id === activeId ? "active" : ""}" data-route="/${mod.id}">
                  <span class="nav-dot"></span>
                  <span>${esc(mod.title)}</span>
                </button>
              `
            )
            .join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">采购系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索单号、供应商、SKU" data-global-search />
            <span class="user-chip">采购经理</span>
          </div>
        </header>
        <section class="content">${content}</section>
      </main>
    </div>
    <div id="toast" class="toast hidden"></div>
  `;
}

function render() {
  const { moduleId, view, recordId } = currentRoute();
  const mod = moduleById(moduleId);
  if (mod.id === "workbench") return shell(mod.id, renderWorkbench());
  if (view === "detail") return shell(mod.id, renderDetail(mod, recordId));
  if (view === "new") return shell(mod.id, renderForm(mod, "new"));
  if (view === "edit") return shell(mod.id, renderForm(mod, "edit", recordId));
  return shell(mod.id, renderList(mod));
}

function pageHead(mod, titleSuffix = "") {
  return `
    <div class="page-head">
      <div>
        <h1 class="page-title">${esc(mod.title)}${titleSuffix}</h1>
        <p class="page-desc">${esc(mod.desc)}</p>
      </div>
      <div class="button-row">
        <button class="btn" data-action="refresh">刷新</button>
        <button class="btn primary" data-route="/${mod.id}/new">新增</button>
      </div>
    </div>
  `;
}

function renderWorkbench() {
  const stats = [
    ["待审批请购", "12", "较昨日 +3"],
    ["待发布订单", "7", "含 2 单紧急"],
    ["供应商差异", "4", "需采购确认"],
    ["今日到货", "18", "预计 6 单质检"],
    ["退供待审", "3", "质量原因 2 单"],
  ];
  return `
    ${pageHead(modules[0], "")}
    <div class="stats-grid">
      ${stats
        .map(
          ([title, value, note]) => `
            <div class="stat-card">
              <div class="stat-title">${title}</div>
              <div class="stat-value">${value}</div>
              <div class="stat-note">${note}</div>
            </div>
          `
        )
        .join("")}
    </div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">采购业务闭环</h2>
        <div class="flow-steps">
          ${["请购提交", "请购审批", "询价报价", "比价定标", "采购订单", "供应商确认", "到货入库跟踪", "退供异常处理"]
            .map((name, idx) => `<div class="flow-step"><span>${idx + 1}. ${name}</span><span>进入</span></div>`)
            .join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>审批</td><td>PR20260628001</td><td>${tag("待审批")}</td></tr>
            <tr><td>差异</td><td>PO20260628001</td><td>${tag("供应商差异")}</td></tr>
            <tr><td>入库</td><td>ASN20260628003</td><td>${tag("部分上架")}</td></tr>
            <tr><td>退供</td><td>SRR20260628001</td><td>${tag("待审批")}</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  `;
}

function filteredRecords(mod) {
  const filters = state.filters[mod.id] || {};
  return (recordsByModule[mod.id] || []).filter((record) =>
    Object.entries(filters).every(([key, value]) => {
      if (!value || value === "全部") return true;
      return String(record[key] || "").includes(value);
    })
  );
}

function renderList(mod) {
  const allRows = filteredRecords(mod);
  const totalPages = Math.max(1, Math.ceil(allRows.length / state.pageSize));
  const page = Math.min(state.page[mod.id] || 1, totalPages);
  state.page[mod.id] = page;
  const start = (page - 1) * state.pageSize;
  const rows = allRows.slice(start, start + state.pageSize);
  return `
    ${pageHead(mod)}
    <div class="filter-panel">
      <div class="filter-grid">
        ${mod.filters.map((field) => renderFilterField(mod, field)).join("")}
        <div class="filter-actions">
          <button class="btn primary" data-action="search" data-module="${mod.id}">查询</button>
          <button class="btn" data-action="reset" data-module="${mod.id}">重置</button>
        </div>
      </div>
    </div>
    <div class="table-panel">
      <div class="table-toolbar">
        <div class="table-title">${esc(mod.title)}列表</div>
        <div class="button-row">
          <button class="btn" data-action="export">导出</button>
          <button class="btn" data-action="batch">批量操作</button>
        </div>
      </div>
      ${renderTable(mod, rows)}
      <div class="pagination">
        <span>共 ${allRows.length} 条，每页 ${state.pageSize} 条，当前第 ${page}/${totalPages} 页</span>
        <div class="pager-buttons">
          <button class="btn" data-action="prev-page" data-module="${mod.id}" ${page <= 1 ? "disabled" : ""}>上一页</button>
          <button class="btn" data-action="next-page" data-module="${mod.id}" ${page >= totalPages ? "disabled" : ""}>下一页</button>
        </div>
      </div>
    </div>
  `;
}

function renderFilterField(mod, [key, label, type, options]) {
  const value = state.filters[mod.id]?.[key] || "";
  if (type === "select") {
    return `
      <div class="field">
        <label>${esc(label)}</label>
        <select data-filter="${esc(key)}" data-module="${mod.id}">
          ${options.map((option) => `<option ${option === value ? "selected" : ""}>${esc(option)}</option>`).join("")}
        </select>
      </div>
    `;
  }
  return `
    <div class="field">
      <label>${esc(label)}</label>
      <input type="${esc(type)}" value="${esc(value)}" placeholder="请输入${esc(label)}" data-filter="${esc(key)}" data-module="${mod.id}" />
    </div>
  `;
}

function renderTable(mod, rows) {
  if (!rows.length) return `<div class="empty">暂无数据，请调整查询条件。</div>`;
  return `
    <table>
      <thead>
        <tr>
          ${mod.columns.map(([, label]) => `<th>${esc(label)}</th>`).join("")}
          <th style="width: 220px;">操作</th>
        </tr>
      </thead>
      <tbody>
        ${rows
          .map(
            (record) => `
              <tr>
                ${mod.columns.map(([key]) => `<td title="${esc(record[key])}">${cell(record[key])}</td>`).join("")}
                <td>
                  <div class="table-actions">
                    ${renderRowActions(mod, record)}
                  </div>
                </td>
              </tr>
            `
          )
          .join("")}
      </tbody>
    </table>
  `;
}

function renderRowActions(mod, record) {
  return mod.actions
    .map((action) => {
      if (action === "查看") {
        return `<button class="btn text" data-route="/${mod.id}/detail/${record.id}">查看</button>`;
      }
      if (action === "修改") {
        return `<button class="btn text" data-route="/${mod.id}/edit/${record.id}">修改</button>`;
      }
      const danger = ["取消", "关闭", "作废", "驳回", "停用"].includes(action) ? "danger" : "";
      return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(action)}" data-no="${esc(primaryNo(mod, record))}">${esc(action)}</button>`;
    })
    .join("");
}

function cell(value) {
  const text = esc(value);
  const raw = String(value || "");
  if (["待审批", "待确认", "报价中", "待到货", "处理中", "草稿"].includes(raw)) return tag(text, "amber");
  if (["已批准", "已确认", "已完成", "已启用", "成功", "已定标", "已处理"].includes(raw)) return tag(text, "green");
  if (["已驳回", "已取消", "已停用", "失败", "已作废"].includes(raw)) return tag(text, "red");
  if (["供应商差异", "差异", "部分入库", "部分上架", "已推荐"].includes(raw)) return tag(text, "purple");
  return text;
}

function tag(value, color = "") {
  return `<span class="tag ${color}">${esc(value).replaceAll("&amp;lt;", "&lt;")}</span>`;
}

function primaryNo(mod, record) {
  const noKey = mod.columns.find(([key]) => key.endsWith("_no"))?.[0] || mod.columns[0][0];
  return record[noKey] || mod.no || record.id;
}

function findRecord(mod, recordId) {
  return (recordsByModule[mod.id] || []).find((record) => record.id === recordId) || (recordsByModule[mod.id] || [])[0] || {};
}

function renderDetail(mod, recordId) {
  const record = findRecord(mod, recordId);
  return `
    ${pageHead(mod, "详情")}
    <div class="detail-layout">
      <div>
        <div class="detail-card">
          <h2 class="detail-title">基本信息</h2>
          <div class="detail-grid">
            ${mod.columns
              .map(
                ([key, label]) => `
                  <div>
                    <div class="kv-label">${esc(label)}</div>
                    <div class="kv-value">${cell(record[key])}</div>
                  </div>
                `
              )
              .join("")}
          </div>
        </div>
        <div class="detail-card">
          <h2 class="detail-title">明细信息</h2>
          <table>
            <thead><tr><th>SKU</th><th>商品名称</th><th>数量</th><th>单位</th><th>单价</th><th>金额</th></tr></thead>
            <tbody>
              <tr><td>SKU-PK-1182</td><td>环保包装盒 A 型</td><td>600</td><td>个</td><td>55.60</td><td>33,360.00</td></tr>
              <tr><td>SKU-PK-1183</td><td>缓冲填充物 B 型</td><td>400</td><td>包</td><td>18.20</td><td>7,280.00</td></tr>
              <tr><td>SKU-WH-3320</td><td>仓储标签纸</td><td>200</td><td>卷</td><td>9.27</td><td>1,854.00</td></tr>
            </tbody>
          </table>
        </div>
      </div>
      <aside>
        <div class="detail-card">
          <h2 class="detail-title">操作</h2>
          <div class="button-row">
            <button class="btn" data-route="/${mod.id}">返回</button>
            ${
              mod.actions.includes("修改")
                ? `<button class="btn primary" data-route="/${mod.id}/edit/${record.id}">修改</button>`
                : ""
            }
            <button class="btn" data-action="export">导出</button>
          </div>
        </div>
        <div class="detail-card">
          <h2 class="detail-title">状态流转</h2>
          <div class="timeline">
            ${["创建草稿", "提交审批", "审批通过", "发布或执行", "完成或关闭"]
              .map(
                (step, idx) => `
                  <div class="timeline-item">
                    <div class="timeline-main">${step}</div>
                    <div class="timeline-sub">2026-06-${24 + idx} ${9 + idx}:00，系统记录操作日志</div>
                  </div>
                `
              )
              .join("")}
          </div>
        </div>
      </aside>
    </div>
  `;
}

function renderForm(mod, mode, recordId = "") {
  const record = mode === "edit" ? findRecord(mod, recordId) : {};
  const title = mode === "edit" ? "修改" : "新增";
  return `
    ${pageHead(mod, title)}
    <div class="form-panel">
      <div class="form-section">
        <h2 class="section-title">${esc(mod.title)}${title}信息</h2>
        <div class="form-grid">
          ${mod.form.map((field) => renderFormField(record, field, mod, mode)).join("")}
        </div>
      </div>
      <div class="form-section">
        <h2 class="section-title">业务备注</h2>
        <div class="field">
          <label>备注</label>
          <textarea placeholder="记录业务说明、审批意见或异常原因">${mode === "edit" ? "按当前业务需要调整后保存。" : ""}</textarea>
        </div>
      </div>
      <div class="form-actions">
        <button class="btn primary" data-action="save-form" data-module="${mod.id}" data-mode="${mode}">保存</button>
        <button class="btn" data-route="/${mod.id}">取消</button>
      </div>
    </div>
  `;
}

function renderFormField(record, [key, label, type, options], mod, mode) {
  const defaultValue = mode === "new" && key.endsWith("_no") ? mod.no : "";
  const value = record[key] || defaultValue;
  if (type === "select") {
    return `
      <div class="field">
        <label>${esc(label)}</label>
        <select>
          ${options.map((option) => `<option ${option === value ? "selected" : ""}>${esc(option)}</option>`).join("")}
        </select>
      </div>
    `;
  }
  if (type === "textarea") {
    return `
      <div class="field">
        <label>${esc(label)}</label>
        <textarea placeholder="请输入${esc(label)}">${esc(value)}</textarea>
      </div>
    `;
  }
  return `
    <div class="field">
      <label>${esc(label)}</label>
      <input type="${esc(type)}" value="${esc(value)}" placeholder="请输入${esc(label)}" />
    </div>
  `;
}

function showToast(message) {
  const toast = document.querySelector("#toast");
  toast.textContent = message;
  toast.classList.remove("hidden");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => toast.classList.add("hidden"), 1800);
}

document.addEventListener("click", (event) => {
  const routeEl = event.target.closest("[data-route]");
  if (routeEl) {
    setRoute(routeEl.dataset.route);
    return;
  }
  const actionEl = event.target.closest("[data-action]");
  if (!actionEl) return;
  const action = actionEl.dataset.action;
  const moduleId = actionEl.dataset.module;
  const mod = moduleById(moduleId || currentRoute().moduleId);
  if (action === "search") {
    state.page[mod.id] = 1;
    render();
    showToast("已按当前条件查询");
  }
  if (action === "reset") {
    state.filters[mod.id] = {};
    state.page[mod.id] = 1;
    render();
    showToast("查询条件已重置");
  }
  if (action === "prev-page") {
    state.page[mod.id] = Math.max(1, (state.page[mod.id] || 1) - 1);
    render();
  }
  if (action === "next-page") {
    state.page[mod.id] = (state.page[mod.id] || 1) + 1;
    render();
  }
  if (action === "row-op") {
    showToast(`${actionEl.dataset.no} 已执行：${actionEl.dataset.op}`);
  }
  if (action === "save-form") {
    showToast("已保存原型数据，返回列表");
    window.setTimeout(() => setRoute(`/${mod.id}`), 350);
  }
  if (["refresh", "export", "batch"].includes(action)) {
    showToast(`已触发：${actionEl.textContent.trim()}`);
  }
});

document.addEventListener("input", (event) => {
  const input = event.target.closest("[data-filter]");
  if (!input) return;
  const modId = input.dataset.module;
  state.filters[modId] = state.filters[modId] || {};
  state.filters[modId][input.dataset.filter] = input.value;
});

document.addEventListener("change", (event) => {
  const input = event.target.closest("[data-filter]");
  if (!input) return;
  const modId = input.dataset.module;
  state.filters[modId] = state.filters[modId] || {};
  state.filters[modId][input.dataset.filter] = input.value;
});

window.addEventListener("hashchange", render);
if (!window.location.hash) {
  window.location.hash = "/workbench";
} else {
  render();
}
