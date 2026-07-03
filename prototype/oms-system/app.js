const modules = [
  { id: "workbench", title: "OMS工作台", desc: "集中查看待审核、缺货、待发仓、出库异常和售后待办。" },
  {
    id: "channel-orders",
    title: "渠道订单接入",
    desc: "查看渠道订单同步、幂等处理、失败原因和手工创建入口。",
    no: "CHO20260628001",
    filters: [["channel", "渠道", "select", ["全部", "天猫", "京东", "抖音", "手工", "API"]], ["external_order_no", "外部单号", "text"], ["access_status", "接入状态", "select", ["全部", "待处理", "成功", "失败", "已忽略"]], ["customer_name", "客户", "text"]],
    columns: [["channel_order_no", "接入单号"], ["channel", "渠道"], ["external_order_no", "外部单号"], ["customer_name", "客户"], ["access_status", "接入状态"], ["fail_reason", "失败原因"], ["received_at", "接收时间"], ["processed_at", "处理时间"]],
    form: [["channel_order_no", "接入单号", "text"], ["channel", "渠道", "select", ["天猫", "京东", "抖音", "手工", "API"]], ["external_order_no", "外部单号", "text"], ["customer_name", "客户", "text"], ["access_status", "接入状态", "select", ["待处理", "成功", "失败", "已忽略"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "重试", "忽略", "手工创建"],
    rows: [["CHO20260628001", "天猫", "TM202606280088", "陈晨", "成功", "-", "2026-06-28 09:10", "2026-06-28 09:10"], ["CHO20260628002", "京东", "JD202606280112", "林雨", "失败", "地址缺少区县", "2026-06-28 09:18", "2026-06-28 09:19"], ["CHO20260627008", "抖音", "DY202606270006", "周宁", "已忽略", "重复订单", "2026-06-27 21:40", "2026-06-27 21:41"]],
  },
  {
    id: "sales-orders",
    title: "销售订单",
    desc: "管理内部销售订单，支持新增、编辑、审核、取消和重审。",
    no: "SO20260628001",
    filters: [["sales_order_no", "订单号", "text"], ["channel", "渠道", "select", ["全部", "天猫", "京东", "抖音", "手工"]], ["customer_name", "客户", "text"], ["order_status", "订单状态", "select", ["全部", "已创建", "待审核", "异常待处理", "待预占", "缺货待处理", "已预占", "已下发仓库", "出库中", "已发货", "已签收", "已完成", "已取消"]]],
    columns: [["sales_order_no", "订单号"], ["channel", "渠道"], ["external_order_no", "外部单号"], ["customer_name", "客户"], ["pay_amount", "实付金额"], ["pay_status", "支付状态"], ["audit_status", "审单状态"], ["order_status", "订单状态"], ["fulfillment_status", "履约状态"]],
    form: [["sales_order_no", "订单号", "text"], ["channel", "渠道", "select", ["天猫", "京东", "抖音", "手工"]], ["external_order_no", "外部单号", "text"], ["customer_name", "客户", "text"], ["order_type", "订单类型", "select", ["普通", "预售", "换货补发", "手工"]], ["pay_amount", "实付金额", "number"], ["pay_status", "支付状态", "select", ["未支付", "已支付", "部分退款", "已退款"]], ["audit_status", "审单状态", "select", ["待审核", "通过", "异常", "驳回"]], ["order_status", "订单状态", "select", ["已创建", "待审核", "异常待处理", "待预占", "缺货待处理", "已预占", "已下发仓库", "出库中", "已发货", "已签收", "已完成", "已取消"]]],
    actions: ["查看", "修改", "审核", "取消", "重审"],
    rows: [["SO20260628001", "天猫", "TM202606280088", "陈晨", "368.00", "已支付", "通过", "已预占", "履约中"], ["SO20260628002", "京东", "JD202606280112", "林雨", "1280.00", "已支付", "异常", "异常待处理", "未履约"], ["SO20260627006", "抖音", "DY202606270006", "周宁", "99.00", "已支付", "通过", "已下发仓库", "履约中"], ["SO20260626009", "手工", "MAN20260626001", "赵倩", "560.00", "已支付", "通过", "缺货待处理", "履约失败"], ["SO20260625012", "天猫", "TM202606250120", "王浩", "245.00", "部分退款", "通过", "已完成", "已履约"], ["SO20260624008", "京东", "JD202606240088", "刘敏", "186.00", "已退款", "通过", "已取消", "未履约"]],
  },
  {
    id: "audits",
    title: "订单审单",
    desc: "处理商品、客户、地址、价格、风控和信用校验异常。",
    no: "AUD20260628001",
    filters: [["sales_order_no", "订单号", "text"], ["audit_type", "审单类型", "select", ["全部", "商品", "客户", "地址", "价格", "风控", "信用"]], ["audit_result", "审单结果", "select", ["全部", "通过", "拦截", "警告"]], ["processed_status", "处理状态", "select", ["全部", "待处理", "已放行", "已驳回", "已修正"]]],
    columns: [["audit_no", "审单记录"], ["sales_order_no", "订单号"], ["audit_type", "审单类型"], ["rule_name", "命中规则"], ["audit_result", "审单结果"], ["exception_reason", "异常原因"], ["processed_status", "处理状态"], ["processed_by", "处理人"], ["processed_at", "处理时间"]],
    form: [["audit_no", "审单记录", "text"], ["sales_order_no", "订单号", "text"], ["audit_type", "审单类型", "select", ["商品", "客户", "地址", "价格", "风控", "信用"]], ["rule_name", "命中规则", "text"], ["audit_result", "审单结果", "select", ["通过", "拦截", "警告"]], ["exception_reason", "异常原因", "textarea"], ["processed_status", "处理状态", "select", ["待处理", "已放行", "已驳回", "已修正"]], ["processed_by", "处理人", "text"]],
    actions: ["查看", "修改", "通过", "驳回", "修正", "重审"],
    rows: [["AUD20260628001", "SO20260628002", "地址", "地址完整性校验", "拦截", "地址缺少区县", "待处理", "-", "-"], ["AUD20260626004", "SO20260626009", "库存", "可售库存校验", "警告", "华东仓缺货", "已修正", "订单运营", "2026-06-26 11:20"], ["AUD20260625007", "SO20260625012", "风控", "高风险订单", "通过", "-", "已放行", "风控", "2026-06-25 10:05"]],
  },
  {
    id: "fulfillments",
    title: "分仓履约",
    desc: "查看和调整分仓、拆单、合单、承诺发货和物流产品。",
    no: "FUL20260628001",
    filters: [["fulfillment_order_no", "履约单号", "text"], ["sales_order_no", "订单号", "text"], ["warehouse", "发货仓", "text"], ["fulfillment_status", "履约状态", "select", ["全部", "待预占", "已预占", "待出库", "已下发", "出库中", "已发货", "已取消", "失败"]]],
    columns: [["fulfillment_order_no", "履约单号"], ["sales_order_no", "销售订单"], ["warehouse", "发货仓"], ["carrier", "物流商"], ["logistics_product", "物流产品"], ["promise_ship_at", "承诺发货"], ["promise_arrive_at", "承诺送达"], ["fulfillment_status", "履约状态"]],
    form: [["fulfillment_order_no", "履约单号", "text"], ["sales_order_no", "销售订单", "text"], ["warehouse", "发货仓", "text"], ["carrier", "物流商", "text"], ["logistics_product", "物流产品", "text"], ["promise_ship_at", "承诺发货", "datetime-local"], ["promise_arrive_at", "承诺送达", "datetime-local"], ["fulfillment_status", "履约状态", "select", ["待预占", "已预占", "待出库", "已下发", "出库中", "已发货", "已取消", "失败"]], ["split_reason", "拆单原因", "textarea"]],
    actions: ["查看", "修改", "分仓", "换仓", "拆单", "合单"],
    rows: [["FUL20260628001", "SO20260628001", "华东一仓", "顺丰", "标快", "2026-06-28 18:00", "2026-06-30 18:00", "已预占"], ["FUL20260627003", "SO20260627006", "华南中心仓", "德邦", "大件快运", "2026-06-28 16:00", "2026-07-01 18:00", "已下发"], ["FUL20260626007", "SO20260626009", "华东一仓", "顺丰", "标快", "2026-06-27 18:00", "2026-06-29 18:00", "失败"]],
  },
  {
    id: "reservations",
    title: "库存预占",
    desc: "查看 OMS 发起的预占请求、中央库存预占号、结果和释放状态。",
    no: "RSVREF20260628001",
    filters: [["sales_order_no", "订单号", "text"], ["reservation_no", "预占号", "text"], ["warehouse", "仓库", "text"], ["reservation_status", "状态", "select", ["全部", "待预占", "预占成功", "预占失败", "已释放", "已扣减"]]],
    columns: [["reservation_ref_no", "预占引用"], ["sales_order_no", "订单号"], ["fulfillment_order_no", "履约单"], ["warehouse", "仓库"], ["sku_code", "SKU"], ["reserve_qty", "请求数量"], ["reserved_qty", "预占数量"], ["reservation_no", "预占号"], ["reservation_status", "状态"], ["fail_reason", "失败原因"]],
    form: [["reservation_ref_no", "预占引用", "text"], ["sales_order_no", "订单号", "text"], ["fulfillment_order_no", "履约单", "text"], ["warehouse", "仓库", "text"], ["sku_code", "SKU", "text"], ["reserve_qty", "请求数量", "number"], ["reserved_qty", "预占数量", "number"], ["reservation_no", "预占号", "text"], ["reservation_status", "状态", "select", ["待预占", "预占成功", "预占失败", "已释放", "已扣减"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "重试预占", "释放预占"],
    rows: [["RSVREF20260628001", "SO20260628001", "FUL20260628001", "华东一仓", "SKU-PK-1182", "2", "2", "RSV20260628001", "预占成功", "-"], ["RSVREF20260626004", "SO20260626009", "FUL20260626007", "华东一仓", "SKU-EL-1008", "5", "0", "-", "预占失败", "可用库存不足"], ["RSVREF20260625006", "SO20260625012", "FUL20260625005", "华东一仓", "SKU-WH-3320", "1", "0", "RSV20260625006", "已扣减", "-"]],
  },
  {
    id: "outbounds",
    title: "出库单",
    desc: "管理下发 WMS 的出库指令，支持创建、下发、取消和重推。",
    no: "OBO20260628001",
    filters: [["outbound_order_no", "出库单号", "text"], ["sales_order_no", "订单号", "text"], ["warehouse", "仓库", "text"], ["outbound_status", "状态", "select", ["全部", "草稿", "已下发", "WMS已接单", "拣货中", "已发货", "已取消", "异常"]]],
    columns: [["outbound_order_no", "出库单号"], ["sales_order_no", "销售订单"], ["fulfillment_order_no", "履约单"], ["warehouse", "仓库"], ["outbound_type", "出库类型"], ["wms_order_no", "WMS单号"], ["outbound_status", "状态"], ["released_at", "下发时间"], ["shipped_at", "发货时间"]],
    form: [["outbound_order_no", "出库单号", "text"], ["sales_order_no", "销售订单", "text"], ["fulfillment_order_no", "履约单", "text"], ["warehouse", "仓库", "text"], ["outbound_type", "出库类型", "select", ["销售出库", "换货补发", "手工补发"]], ["wms_order_no", "WMS单号", "text"], ["outbound_status", "状态", "select", ["草稿", "已下发", "WMS已接单", "拣货中", "已发货", "已取消", "异常"]], ["released_at", "下发时间", "datetime-local"], ["shipped_at", "发货时间", "datetime-local"]],
    actions: ["查看", "修改", "创建", "下发", "取消", "重推"],
    rows: [["OBO20260628001", "SO20260628001", "FUL20260628001", "华东一仓", "销售出库", "OUT20260628001", "WMS已接单", "2026-06-28 10:30", "-"], ["OBO20260627005", "SO20260627006", "FUL20260627003", "华南中心仓", "销售出库", "OUT20260627005", "拣货中", "2026-06-27 16:10", "-"], ["OBO20260625008", "SO20260625012", "FUL20260625005", "华东一仓", "销售出库", "OUT20260625008", "已发货", "2026-06-25 11:00", "2026-06-25 16:20"]],
  },
  {
    id: "cancels",
    title: "取消管理",
    desc: "处理客户、客服、渠道或系统发起的取消申请，校验 WMS 和库存释放状态。",
    no: "CAN20260628001",
    filters: [["cancel_request_no", "取消单号", "text"], ["sales_order_no", "订单号", "text"], ["cancel_source", "来源", "select", ["全部", "客户", "客服", "渠道", "系统"]], ["cancel_status", "状态", "select", ["全部", "待审核", "已同意", "已拒绝", "取消中", "已完成", "转售后"]]],
    columns: [["cancel_request_no", "取消单号"], ["sales_order_no", "订单号"], ["cancel_source", "来源"], ["cancel_reason", "原因"], ["cancel_status", "取消状态"], ["wms_cancel_status", "WMS取消"], ["stock_release_status", "库存释放"], ["created_by", "创建人"], ["processed_at", "处理时间"]],
    form: [["cancel_request_no", "取消单号", "text"], ["sales_order_no", "订单号", "text"], ["cancel_source", "来源", "select", ["客户", "客服", "渠道", "系统"]], ["cancel_reason", "原因", "select", ["不想要", "地址错误", "缺货", "风控", "其他"]], ["cancel_status", "取消状态", "select", ["待审核", "已同意", "已拒绝", "取消中", "已完成", "转售后"]], ["wms_cancel_status", "WMS取消", "select", ["未请求", "请求中", "成功", "失败"]], ["stock_release_status", "库存释放", "select", ["未释放", "释放中", "已释放", "释放失败"]], ["created_by", "创建人", "text"]],
    actions: ["查看", "修改", "审核", "同意", "拒绝", "转售后"],
    rows: [["CAN20260628001", "SO20260628001", "客服", "地址错误", "待审核", "未请求", "未释放", "客服A", "-"], ["CAN20260626004", "SO20260626009", "系统", "缺货", "已同意", "未请求", "已释放", "系统", "2026-06-26 12:10"], ["CAN20260625008", "SO20260625012", "客户", "不想要", "转售后", "失败", "未释放", "客户", "2026-06-25 18:00"]],
  },
  {
    id: "after-sales",
    title: "售后管理",
    desc: "处理仅退款、退货退款、换货补发，跟踪退款、退货入库和补发状态。",
    no: "AS20260628001",
    filters: [["after_sale_no", "售后单号", "text"], ["sales_order_no", "订单号", "text"], ["after_sale_type", "售后类型", "select", ["全部", "仅退款", "退货退款", "换货补发"]], ["after_sale_status", "状态", "select", ["全部", "已创建", "待审核", "审核驳回", "待退货", "待验收", "待退款", "待补发", "异常待处理", "已完成", "已关闭"]]],
    columns: [["after_sale_no", "售后单号"], ["sales_order_no", "销售订单"], ["after_sale_type", "售后类型"], ["after_sale_reason", "原因"], ["refund_amount", "退款金额"], ["return_warehouse", "退货仓"], ["after_sale_status", "状态"], ["created_by", "创建人"], ["created_at", "创建时间"]],
    form: [["after_sale_no", "售后单号", "text"], ["sales_order_no", "销售订单", "text"], ["after_sale_type", "售后类型", "select", ["仅退款", "退货退款", "换货补发"]], ["after_sale_reason", "原因", "select", ["质量问题", "错发", "少发", "不想要", "其他"]], ["refund_amount", "退款金额", "number"], ["return_warehouse", "退货仓", "text"], ["after_sale_status", "状态", "select", ["已创建", "待审核", "审核驳回", "待退货", "待验收", "待退款", "待补发", "异常待处理", "已完成", "已关闭"]], ["created_by", "创建人", "text"]],
    actions: ["查看", "修改", "创建", "审核", "驳回", "关闭"],
    rows: [["AS20260628001", "SO20260625012", "退货退款", "质量问题", "245.00", "华东退货仓", "待审核", "客服A", "2026-06-28 10:00"], ["AS20260627004", "SO20260627006", "换货补发", "错发", "0.00", "华南退货仓", "待验收", "客服B", "2026-06-27 15:30"], ["AS20260626009", "SO20260624008", "仅退款", "不想要", "186.00", "-", "已完成", "客服A", "2026-06-26 11:20"]],
  },
  {
    id: "exceptions",
    title: "异常处理",
    desc: "集中处理缺货、地址不可达、风控拦截、仓库拒单和履约异常。",
    no: "EXP20260628001",
    filters: [["exception_no", "异常单号", "text"], ["sales_order_no", "订单号", "text"], ["exception_type", "异常类型", "select", ["全部", "缺货", "地址不可达", "风控拦截", "仓库拒单", "物流异常", "售后异常"]], ["exception_status", "状态", "select", ["全部", "待处理", "处理中", "已关闭"]]],
    columns: [["exception_no", "异常单号"], ["sales_order_no", "订单号"], ["exception_type", "异常类型"], ["responsible_party", "责任方"], ["exception_status", "状态"], ["description", "说明"], ["assigned_to", "处理人"], ["created_at", "创建时间"]],
    form: [["exception_no", "异常单号", "text"], ["sales_order_no", "订单号", "text"], ["exception_type", "异常类型", "select", ["缺货", "地址不可达", "风控拦截", "仓库拒单", "物流异常", "售后异常"]], ["responsible_party", "责任方", "select", ["订单运营", "客服", "仓库", "物流", "系统"]], ["exception_status", "状态", "select", ["待处理", "处理中", "已关闭"]], ["description", "说明", "textarea"], ["assigned_to", "处理人", "text"]],
    actions: ["查看", "修改", "分派", "处理", "关闭", "重试"],
    rows: [["EXP20260628001", "SO20260628002", "地址不可达", "客服", "待处理", "地址缺少区县", "-", "2026-06-28 09:20"], ["EXP20260626004", "SO20260626009", "缺货", "订单运营", "处理中", "华东仓可用不足", "订单运营", "2026-06-26 11:10"], ["EXP20260625006", "SO20260625012", "仓库拒单", "仓库", "已关闭", "WMS 地址超区已换仓", "仓配运营", "2026-06-25 13:15"]],
  },
  {
    id: "rules",
    title: "规则配置",
    desc: "配置审单、分仓、取消、售后和承运商规则。",
    no: "RULE20260628001",
    filters: [["rule_code", "规则编码", "text"], ["rule_type", "规则类型", "select", ["全部", "审单", "分仓", "取消", "售后", "承运商"]], ["status", "状态", "select", ["全部", "启用", "停用"]], ["rule_name", "规则名称", "text"]],
    columns: [["rule_code", "规则编码"], ["rule_name", "规则名称"], ["rule_type", "规则类型"], ["priority", "优先级"], ["status", "状态"], ["effective_from", "生效时间"], ["effective_to", "失效时间"], ["updated_by", "更新人"]],
    form: [["rule_code", "规则编码", "text"], ["rule_name", "规则名称", "text"], ["rule_type", "规则类型", "select", ["审单", "分仓", "取消", "售后", "承运商"]], ["priority", "优先级", "number"], ["status", "状态", "select", ["启用", "停用"]], ["effective_from", "生效时间", "datetime-local"], ["effective_to", "失效时间", "datetime-local"], ["condition_config", "条件配置", "textarea"], ["action_config", "动作配置", "textarea"]],
    actions: ["查看", "修改", "启用", "停用", "发布"],
    rows: [["AUDIT_ADDRESS_REQUIRED", "地址完整性校验", "审单", "10", "启用", "2026-01-01 00:00", "-", "管理员"], ["ROUTE_NEAREST_WAREHOUSE", "就近分仓", "分仓", "20", "启用", "2026-01-01 00:00", "-", "管理员"], ["CANCEL_BEFORE_SHIP", "发货前可取消", "取消", "10", "启用", "2026-01-01 00:00", "-", "管理员"]],
  },
  {
    id: "logs",
    title: "操作日志",
    desc: "查询 OMS 关键写操作，满足订单、履约、售后审计追踪。",
    no: "LOG20260628001",
    filters: [["operator", "操作人", "text"], ["object_type", "对象类型", "select", ["全部", "订单", "履约单", "出库单", "取消单", "售后单", "规则"]], ["action_type", "动作", "text"], ["result", "结果", "select", ["全部", "成功", "失败"]]],
    columns: [["operator", "操作人"], ["object_type", "对象类型"], ["object_no", "对象单号"], ["action_type", "动作"], ["result", "结果"], ["fail_reason", "失败原因"], ["ip_address", "IP"], ["created_at", "操作时间"]],
    form: [["operator", "操作人", "text"], ["object_type", "对象类型", "text"], ["object_no", "对象单号", "text"], ["action_type", "动作", "text"], ["result", "结果", "select", ["成功", "失败"]], ["fail_reason", "失败原因", "textarea"], ["ip_address", "IP", "text"]],
    actions: ["查看", "修改", "导出"],
    rows: [["订单运营", "订单", "SO20260628001", "审核", "成功", "-", "10.10.3.11", "2026-06-28 10:02"], ["系统", "履约单", "FUL20260628001", "分仓", "成功", "-", "10.10.1.8", "2026-06-28 10:05"], ["客服A", "售后单", "AS20260628001", "创建", "成功", "-", "10.10.5.21", "2026-06-28 10:00"]],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护 OMS 页面枚举项中文标签、颜色、排序和启停状态。",
    no: "ENUM20260628001",
    filters: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["enum_type", "枚举类型"], ["enum_value", "枚举值"], ["label", "显示名称"], ["sort_no", "排序"], ["status", "状态"], ["updated_by", "更新人"], ["updated_at", "更新时间"]],
    form: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["sort_no", "排序", "number"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "排序", "停用"],
    rows: [["SALES_ORDER_STATUS", "OUT_OF_STOCK", "缺货待处理", "50", "启用", "系统管理员", "2026-06-20 10:18"], ["AFTER_SALE_TYPE", "RETURN_REFUND", "退货退款", "20", "启用", "系统管理员", "2026-06-20 10:20"], ["CANCEL_REASON", "ADDRESS_ERROR", "地址错误", "20", "启用", "系统管理员", "2026-06-18 09:15"]],
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
          <div class="brand-title">OMS系统原型</div>
          <div class="brand-subtitle">Order Management Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules.map((m) => `<button class="nav-item ${m.id === active.id ? "active" : ""}" data-route="/${m.id}"><span class="nav-dot"></span><span>${esc(m.title)}</span></button>`).join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">OMS系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索订单、客户、售后、出库单" />
            <span class="user-chip">订单运营</span>
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
  const stats = [["待审核", "18", "地址异常 5 单"], ["缺货待处理", "7", "可换仓 3 单"], ["待发仓", "22", "今日需下发"], ["出库异常", "4", "WMS 拒单 1 单"], ["售后待审", "9", "退货退款 6 单"]];
  return `
    ${head(modules[0])}
    <div class="stats-grid">${stats.map(([a, b, c]) => `<div class="stat-card"><div class="stat-title">${a}</div><div class="stat-value">${b}</div><div class="stat-note">${c}</div></div>`).join("")}</div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">OMS履约闭环</h2>
        <div class="flow-steps">
          ${["渠道订单接入", "创建销售订单", "审单校验", "分仓履约", "库存预占", "生成出库单", "下发 WMS", "发货签收", "取消/售后"].map((x, i) => `<div class="flow-step"><span>${i + 1}. ${x}</span><span>进入</span></div>`).join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>审单</td><td>SO20260628002</td><td>${tag("异常待处理", "amber")}</td></tr>
            <tr><td>预占</td><td>SO20260626009</td><td>${tag("预占失败", "red")}</td></tr>
            <tr><td>出库</td><td>OBO20260627005</td><td>${tag("拣货中", "purple")}</td></tr>
            <tr><td>售后</td><td>AS20260628001</td><td>${tag("待审核", "amber")}</td></tr>
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
    const danger = ["取消", "驳回", "拒绝", "忽略", "释放预占", "关闭", "停用"].includes(a) ? "danger" : "";
    return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(a)}" data-no="${esc(primaryNo(mod, record))}">${esc(a)}</button>`;
  }).join("");
}

function cell(value) {
  const raw = String(value || "");
  if (["待处理", "待审核", "待预占", "缺货待处理", "待出库", "草稿", "待退款", "待退货", "待验收", "待补发", "警告", "请求中", "释放中"].includes(raw)) return tag(raw, "amber");
  if (["成功", "已支付", "通过", "已预占", "预占成功", "已下发", "WMS已接单", "已发货", "已签收", "已完成", "已履约", "已同意", "已释放", "启用", "已放行"].includes(raw)) return tag(raw, "green");
  if (["失败", "异常", "拦截", "已驳回", "预占失败", "已取消", "已拒绝", "审核驳回", "停用", "释放失败"].includes(raw)) return tag(raw, "red");
  if (["异常待处理", "出库中", "拣货中", "履约中", "部分退款", "部分履约", "转售后", "处理中", "取消中"].includes(raw)) return tag(raw, "purple");
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
          <h2 class="detail-title">订单/履约明细</h2>
          <table>
            <thead><tr><th>SKU</th><th>商品名称</th><th>订单数</th><th>预占数</th><th>出库数</th><th>发货数</th><th>状态</th></tr></thead>
            <tbody>
              <tr><td>SKU-PK-1182</td><td>环保包装盒 A 型</td><td>2</td><td>2</td><td>2</td><td>0</td><td>${tag("已预占", "green")}</td></tr>
              <tr><td>SKU-EL-1008</td><td>控制板 X1</td><td>1</td><td>0</td><td>0</td><td>0</td><td>${tag("缺货待处理", "amber")}</td></tr>
              <tr><td>SKU-WH-3320</td><td>仓储标签纸</td><td>1</td><td>1</td><td>1</td><td>1</td><td>${tag("已发货", "green")}</td></tr>
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
          <h2 class="detail-title">履约流转</h2>
          <div class="timeline">${["渠道接入", "审单校验", "分仓履约", "库存预占", "出库下发", "发货/售后"].map((s, i) => `<div class="timeline-item"><div class="timeline-main">${s}</div><div class="timeline-sub">2026-06-${23 + i} ${9 + i}:00，记录 OMS 操作日志</div></div>`).join("")}</div>
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
        <div class="field"><label>备注</label><textarea placeholder="记录审单意见、换仓原因、取消原因、售后说明或异常处理意见">${mode === "edit" ? "根据当前订单履约情况调整后保存。" : ""}</textarea></div>
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
