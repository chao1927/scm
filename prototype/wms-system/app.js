const modules = [
  { id: "workbench", title: "WMS工作台", desc: "集中查看待收货、待质检、待上架、待拣货、待复核和仓内异常。" },
  {
    id: "inbounds",
    title: "入库单",
    desc: "查看采购、调拨、销售退货等入库指令，并执行接单、取消、关闭。",
    no: "INB20260628001",
    filters: [["inbound_order_no", "入库单号", "text"], ["source_order_no", "来源单号", "text"], ["source_type", "来源类型", "select", ["全部", "采购", "调拨", "销售退货", "其他"]], ["inbound_status", "状态", "select", ["全部", "待到货", "到货中", "收货中", "待质检", "待上架", "部分上架", "已上架", "已关闭", "已取消"]]],
    columns: [["inbound_order_no", "入库单号"], ["source_type", "来源类型"], ["source_order_no", "来源单号"], ["warehouse", "仓库"], ["supplier_customer", "供应商/客户"], ["inbound_status", "状态"], ["expected_arrival_at", "预计到仓"], ["arrived_at", "到货时间"]],
    form: [["inbound_order_no", "入库单号", "text"], ["source_order_no", "来源单号", "text"], ["source_type", "来源类型", "select", ["采购", "调拨", "销售退货", "其他"]], ["warehouse", "仓库", "text"], ["supplier_customer", "供应商/客户", "text"], ["inbound_status", "状态", "select", ["待到货", "到货中", "收货中", "待质检", "待上架", "部分上架", "已上架", "已关闭", "已取消"]], ["expected_arrival_at", "预计到仓", "datetime-local"], ["arrived_at", "到货时间", "datetime-local"]],
    actions: ["查看", "修改", "接单", "取消", "关闭"],
    rows: [["INB20260628001", "采购", "ASN20260628003", "华东一仓", "苏州蓝海材料", "待到货", "2026-07-02 10:00", "-"], ["INB20260627006", "调拨", "TR20260627002", "华南中心仓", "华东一仓", "收货中", "2026-06-28 14:00", "2026-06-28 13:50"], ["INB20260625008", "销售退货", "AR20260625011", "华东一仓", "客户A", "部分上架", "2026-06-25 09:00", "2026-06-25 09:12"]],
  },
  {
    id: "receipts",
    title: "收货作业",
    desc: "到货登记、扫码点数、记录短收、超收、错货和破损差异。",
    no: "RCV20260628001",
    filters: [["receipt_order_no", "收货单号", "text"], ["inbound_order_no", "入库单号", "text"], ["receiver", "收货员", "text"], ["receipt_status", "状态", "select", ["全部", "待收货", "收货中", "部分收货", "已收货", "异常", "已关闭"]]],
    columns: [["receipt_order_no", "收货单号"], ["inbound_order_no", "入库单号"], ["warehouse", "仓库"], ["sku_code", "SKU"], ["expected_qty", "应收"], ["received_qty", "实收"], ["diff_qty", "差异"], ["receipt_status", "状态"], ["receiver", "收货员"]],
    form: [["receipt_order_no", "收货单号", "text"], ["inbound_order_no", "入库单号", "text"], ["warehouse", "仓库", "text"], ["sku_code", "SKU", "text"], ["expected_qty", "应收", "number"], ["received_qty", "实收", "number"], ["batch_no", "批次", "text"], ["receipt_status", "状态", "select", ["待收货", "收货中", "部分收货", "已收货", "异常", "已关闭"]], ["receiver", "收货员", "text"]],
    actions: ["查看", "修改", "开始收货", "提交", "异常登记"],
    rows: [["RCV20260628001", "INB20260628001", "华东一仓", "SKU-PK-1182", "1200", "0", "0", "待收货", "陈收"], ["RCV20260627005", "INB20260627006", "华南中心仓", "SKU-WH-3320", "500", "460", "-40", "部分收货", "王收"], ["RCV20260625009", "INB20260625008", "华东一仓", "SKU-EL-1008", "80", "82", "+2", "异常", "陈收"]],
  },
  {
    id: "qc",
    title: "质检作业",
    desc: "对收货商品执行抽检，判定合格、不合格、部分合格或冻结。",
    no: "QC20260628001",
    filters: [["qc_order_no", "质检单号", "text"], ["sku_code", "SKU", "text"], ["qc_result", "结果", "select", ["全部", "待检", "合格", "不合格", "部分合格"]], ["inspector", "质检员", "text"]],
    columns: [["qc_order_no", "质检单号"], ["receipt_order_no", "收货单号"], ["sku_code", "SKU"], ["sample_qty", "抽检数"], ["accepted_qty", "合格数"], ["rejected_qty", "不合格数"], ["qc_result", "结果"], ["reject_reason", "原因"], ["inspector", "质检员"]],
    form: [["qc_order_no", "质检单号", "text"], ["receipt_order_no", "收货单号", "text"], ["sku_code", "SKU", "text"], ["sample_qty", "抽检数", "number"], ["accepted_qty", "合格数", "number"], ["rejected_qty", "不合格数", "number"], ["qc_result", "结果", "select", ["待检", "合格", "不合格", "部分合格"]], ["reject_reason", "原因", "textarea"], ["inspector", "质检员", "text"]],
    actions: ["查看", "修改", "判定", "冻结", "提交"],
    rows: [["QC20260628001", "RCV20260628001", "SKU-PK-1182", "120", "120", "0", "合格", "-", "李检"], ["QC20260627004", "RCV20260627005", "SKU-WH-3320", "50", "45", "5", "部分合格", "外箱破损", "李检"], ["QC20260625008", "RCV20260625009", "SKU-EL-1008", "20", "0", "20", "不合格", "性能异常", "周检"]],
  },
  {
    id: "putaway",
    title: "上架任务",
    desc: "按推荐库区库位上架合格品，不合格品放入异常区或待退供区。",
    no: "PUT20260628001",
    filters: [["putaway_task_no", "任务号", "text"], ["inbound_order_no", "入库单号", "text"], ["task_status", "状态", "select", ["全部", "待上架", "上架中", "部分上架", "已完成", "异常"]], ["operator", "上架员", "text"]],
    columns: [["putaway_task_no", "任务号"], ["inbound_order_no", "入库单号"], ["sku_code", "SKU"], ["target_location", "目标库位"], ["quality_status", "质量状态"], ["required_qty", "应上架"], ["putaway_qty", "已上架"], ["task_status", "状态"], ["operator", "上架员"]],
    form: [["putaway_task_no", "任务号", "text"], ["inbound_order_no", "入库单号", "text"], ["sku_code", "SKU", "text"], ["target_location", "目标库位", "text"], ["quality_status", "质量状态", "select", ["合格", "不合格", "待处理"]], ["required_qty", "应上架", "number"], ["putaway_qty", "已上架", "number"], ["task_status", "状态", "select", ["待上架", "上架中", "部分上架", "已完成", "异常"]], ["operator", "上架员", "text"]],
    actions: ["查看", "修改", "领取", "确认上架", "改库位"],
    rows: [["PUT20260628001", "INB20260628001", "SKU-PK-1182", "A-01-02-03", "合格", "1200", "0", "待上架", "-"], ["PUT20260627006", "INB20260627006", "SKU-WH-3320", "B-02-01-05", "合格", "455", "300", "部分上架", "赵上"], ["PUT20260625010", "INB20260625008", "SKU-EL-1008", "NG-01-01-01", "不合格", "20", "20", "已完成", "赵上"]],
  },
  {
    id: "stocks",
    title: "库内库存",
    desc: "查询 WMS 仓内库位库存，支持冻结和解冻作业锁定。",
    no: "STK20260628001",
    filters: [["warehouse", "仓库", "text"], ["location", "库位", "text"], ["sku_code", "SKU", "text"], ["stock_status", "库存状态", "select", ["全部", "可拣", "冻结", "不合格", "待退供", "待报废"]]],
    columns: [["warehouse", "仓库"], ["zone", "库区"], ["location", "库位"], ["sku_code", "SKU"], ["batch_no", "批次"], ["stock_status", "库存状态"], ["qty", "数量"], ["locked_qty", "锁定"], ["updated_at", "更新时间"]],
    form: [["warehouse", "仓库", "text"], ["zone", "库区", "text"], ["location", "库位", "text"], ["sku_code", "SKU", "text"], ["batch_no", "批次", "text"], ["stock_status", "库存状态", "select", ["可拣", "冻结", "不合格", "待退供", "待报废"]], ["qty", "数量", "number"], ["locked_qty", "锁定数量", "number"]],
    actions: ["查看", "修改", "冻结", "解冻"],
    rows: [["华东一仓", "A区", "A-01-02-03", "SKU-PK-1182", "B20260601", "可拣", "1200", "180", "2026-06-28 10:20"], ["华南中心仓", "B区", "B-02-01-05", "SKU-WH-3320", "T20260602", "可拣", "455", "0", "2026-06-28 09:10"], ["华东一仓", "异常区", "NG-01-01-01", "SKU-EL-1008", "E20260612", "不合格", "20", "0", "2026-06-25 15:30"]],
  },
  {
    id: "outbounds",
    title: "出库单",
    desc: "接收销售、调拨、退供出库指令，并推动分配、拣货、复核和发货。",
    no: "OUT20260628001",
    filters: [["outbound_order_no", "出库单号", "text"], ["source_order_no", "来源单号", "text"], ["source_type", "来源类型", "select", ["全部", "销售", "调拨", "退供", "其他"]], ["outbound_status", "状态", "select", ["全部", "待接单", "待分配", "分配失败", "待拣货", "拣货中", "待复核", "复核异常", "待发货", "已发货", "已关闭", "已取消"]]],
    columns: [["outbound_order_no", "出库单号"], ["source_type", "来源类型"], ["source_order_no", "来源单号"], ["warehouse", "仓库"], ["priority", "优先级"], ["outbound_status", "状态"], ["carrier", "物流商"], ["shipped_at", "发货时间"]],
    form: [["outbound_order_no", "出库单号", "text"], ["source_order_no", "来源单号", "text"], ["source_type", "来源类型", "select", ["销售", "调拨", "退供", "其他"]], ["warehouse", "仓库", "text"], ["priority", "优先级", "number"], ["outbound_status", "状态", "select", ["待接单", "待分配", "分配失败", "待拣货", "拣货中", "待复核", "复核异常", "待发货", "已发货", "已关闭", "已取消"]], ["carrier", "物流商", "text"], ["shipped_at", "发货时间", "datetime-local"]],
    actions: ["查看", "修改", "接单", "取消", "关闭"],
    rows: [["OUT20260628001", "销售", "SO20260628008", "华东一仓", "80", "待拣货", "顺丰", "-"], ["OUT20260627005", "调拨", "TR20260627003", "华南中心仓", "50", "拣货中", "德邦", "-"], ["OUT20260626009", "退供", "SRR20260626002", "华东一仓", "60", "已发货", "安能", "2026-06-26 16:20"]],
  },
  {
    id: "waves",
    title: "波次管理",
    desc: "将多个出库单按策略合并，生成更高效的拣货波次。",
    no: "WAV20260628001",
    filters: [["wave_no", "波次号", "text"], ["warehouse", "仓库", "text"], ["wave_type", "波次类型", "select", ["全部", "单单拣", "批量拣", "边拣边分"]], ["wave_status", "状态", "select", ["全部", "草稿", "已释放", "拣货中", "已完成", "已取消"]]],
    columns: [["wave_no", "波次号"], ["warehouse", "仓库"], ["wave_type", "波次类型"], ["order_count", "订单数"], ["sku_count", "SKU数"], ["wave_status", "状态"], ["created_by", "创建人"], ["released_at", "释放时间"]],
    form: [["wave_no", "波次号", "text"], ["warehouse", "仓库", "text"], ["wave_type", "波次类型", "select", ["单单拣", "批量拣", "边拣边分"]], ["order_count", "订单数", "number"], ["sku_count", "SKU数", "number"], ["wave_status", "状态", "select", ["草稿", "已释放", "拣货中", "已完成", "已取消"]], ["created_by", "创建人", "text"]],
    actions: ["查看", "修改", "生成", "释放", "取消"],
    rows: [["WAV20260628001", "华东一仓", "批量拣", "28", "56", "已释放", "仓库主管", "2026-06-28 10:00"], ["WAV20260627002", "华南中心仓", "边拣边分", "18", "44", "拣货中", "仓库主管", "2026-06-27 15:00"], ["WAV20260626003", "华东一仓", "单单拣", "6", "12", "已完成", "仓库主管", "2026-06-26 09:00"]],
  },
  {
    id: "picking-orders",
    title: "拣货单",
    desc: "管理拣货员可执行的拣货单，支持分配、领取、完成和异常处理。",
    no: "PKO20260628001",
    filters: [["picking_order_no", "拣货单号", "text"], ["wave_no", "波次号", "text"], ["picking_mode", "拣货模式", "select", ["全部", "单单拣", "批量拣", "边拣边分"]], ["picking_status", "状态", "select", ["全部", "待分配", "待拣货", "拣货中", "部分拣货", "拣货异常", "已拣货", "已交接复核"]]],
    columns: [["picking_order_no", "拣货单号"], ["wave_no", "波次号"], ["warehouse", "仓库"], ["zone", "库区"], ["picking_mode", "拣货模式"], ["picking_status", "状态"], ["picker", "拣货员"], ["completed_at", "完成时间"]],
    form: [["picking_order_no", "拣货单号", "text"], ["wave_no", "波次号", "text"], ["warehouse", "仓库", "text"], ["zone", "库区", "text"], ["picking_mode", "拣货模式", "select", ["单单拣", "批量拣", "边拣边分"]], ["picking_status", "状态", "select", ["待分配", "待拣货", "拣货中", "部分拣货", "拣货异常", "已拣货", "已交接复核"]], ["picker", "拣货员", "text"]],
    actions: ["查看", "修改", "分配", "领取", "完成", "异常"],
    rows: [["PKO20260628001", "WAV20260628001", "华东一仓", "A区", "批量拣", "待拣货", "-", "-"], ["PKO20260627006", "WAV20260627002", "华南中心仓", "B区", "边拣边分", "拣货中", "钱拣", "-"], ["PKO20260626008", "WAV20260626003", "华东一仓", "A区", "单单拣", "已交接复核", "钱拣", "2026-06-26 11:20"]],
  },
  {
    id: "picking-tasks",
    title: "拣货任务",
    desc: "指导拣货员从指定库区库位拿取指定 SKU、批次和数量，并放入容器。",
    no: "PKT20260628001",
    filters: [["picking_task_no", "任务号", "text"], ["outbound_order_no", "出库单号", "text"], ["location", "库位", "text"], ["task_status", "状态", "select", ["全部", "待拣货", "拣货中", "已完成", "异常"]]],
    columns: [["picking_task_no", "任务号"], ["outbound_order_no", "出库单"], ["zone", "库区"], ["location", "库位"], ["sku_code", "SKU"], ["batch_no", "批次"], ["required_qty", "应拣"], ["picked_qty", "已拣"], ["container_code", "容器"], ["task_status", "状态"]],
    form: [["picking_task_no", "任务号", "text"], ["outbound_order_no", "出库单", "text"], ["zone", "库区", "text"], ["location", "库位", "text"], ["sku_code", "SKU", "text"], ["batch_no", "批次", "text"], ["required_qty", "应拣", "number"], ["picked_qty", "已拣", "number"], ["container_code", "容器", "text"], ["task_status", "状态", "select", ["待拣货", "拣货中", "已完成", "异常"]]],
    actions: ["查看", "修改", "扫库位", "扫商品", "确认数量", "异常"],
    rows: [["PKT20260628001", "OUT20260628001", "A区", "A-01-02-03", "SKU-PK-1182", "B20260601", "12", "0", "BOX-001", "待拣货"], ["PKT20260627008", "OUT20260627005", "B区", "B-02-01-05", "SKU-WH-3320", "T20260602", "20", "12", "BOX-018", "拣货中"], ["PKT20260626006", "OUT20260626009", "异常区", "NG-01-01-01", "SKU-EL-1008", "E20260612", "20", "20", "BOX-009", "已完成"]],
  },
  {
    id: "containers",
    title: "容器管理",
    desc: "管理周转箱、播种车、格口和托盘的绑定、解绑、流转、清空。",
    no: "BOX20260628001",
    filters: [["container_code", "容器编码", "text"], ["container_type", "类型", "select", ["全部", "周转箱", "播种车", "格口", "托盘"]], ["container_status", "状态", "select", ["全部", "空闲", "已绑定", "拣货中", "待复核", "已清空", "停用"]], ["warehouse", "仓库", "text"]],
    columns: [["container_code", "容器编码"], ["container_type", "类型"], ["warehouse", "仓库"], ["bind_object", "绑定对象"], ["container_status", "状态"], ["current_location", "当前位置"], ["updated_at", "更新时间"]],
    form: [["container_code", "容器编码", "text"], ["container_type", "类型", "select", ["周转箱", "播种车", "格口", "托盘"]], ["warehouse", "仓库", "text"], ["bind_object", "绑定对象", "text"], ["container_status", "状态", "select", ["空闲", "已绑定", "拣货中", "待复核", "已清空", "停用"]], ["current_location", "当前位置", "text"]],
    actions: ["查看", "修改", "绑定", "解绑", "流转", "清空"],
    rows: [["BOX-001", "周转箱", "华东一仓", "PKO20260628001", "已绑定", "A区入口", "2026-06-28 10:10"], ["CART-018", "播种车", "华南中心仓", "WAV20260627002", "拣货中", "B区", "2026-06-28 09:50"], ["SLOT-09", "格口", "华东一仓", "OUT20260626009", "待复核", "复核台01", "2026-06-26 11:30"]],
  },
  {
    id: "pack-orders",
    title: "复核包装",
    desc: "扫描容器和商品，完成复核、打包、称重、打印面单。",
    no: "PACK20260628001",
    filters: [["pack_order_no", "包装单号", "text"], ["outbound_order_no", "出库单", "text"], ["pack_status", "状态", "select", ["全部", "待复核", "复核中", "复核异常", "待打包", "已打包", "已关闭"]], ["reviewer", "复核员", "text"]],
    columns: [["pack_order_no", "包装单号"], ["outbound_order_no", "出库单"], ["container_code", "容器"], ["reviewer", "复核员"], ["packer", "打包员"], ["pack_status", "状态"], ["weight", "重量"], ["volume", "体积"], ["completed_at", "完成时间"]],
    form: [["pack_order_no", "包装单号", "text"], ["outbound_order_no", "出库单", "text"], ["container_code", "容器", "text"], ["reviewer", "复核员", "text"], ["packer", "打包员", "text"], ["pack_status", "状态", "select", ["待复核", "复核中", "复核异常", "待打包", "已打包", "已关闭"]], ["weight", "重量", "number"], ["volume", "体积", "number"]],
    actions: ["查看", "修改", "复核", "打包", "称重", "打印面单"],
    rows: [["PACK20260628001", "OUT20260628001", "BOX-001", "孙复", "郑包", "待复核", "-", "-", "-"], ["PACK20260627003", "OUT20260627005", "CART-018", "孙复", "郑包", "复核中", "-", "-", "-"], ["PACK20260626002", "OUT20260626009", "SLOT-09", "孙复", "郑包", "已打包", "12.6kg", "0.08m3", "2026-06-26 15:10"]],
  },
  {
    id: "shipments",
    title: "发货交接",
    desc: "承运商交接包裹，扫描包裹并确认出库完成。",
    no: "SHP20260628001",
    filters: [["handover_no", "交接单号", "text"], ["carrier", "承运商", "text"], ["handover_status", "状态", "select", ["全部", "待交接", "交接中", "已交接", "已取消"]], ["warehouse", "仓库", "text"]],
    columns: [["handover_no", "交接单号"], ["warehouse", "仓库"], ["carrier", "承运商"], ["package_count", "包裹数"], ["handover_status", "状态"], ["handover_by", "交接人"], ["confirmed_at", "确认时间"]],
    form: [["handover_no", "交接单号", "text"], ["warehouse", "仓库", "text"], ["carrier", "承运商", "text"], ["package_count", "包裹数", "number"], ["handover_status", "状态", "select", ["待交接", "交接中", "已交接", "已取消"]], ["handover_by", "交接人", "text"], ["confirmed_at", "确认时间", "datetime-local"]],
    actions: ["查看", "修改", "扫包裹", "交接", "确认发货"],
    rows: [["SHP20260628001", "华东一仓", "顺丰", "36", "待交接", "吴发", "-"], ["SHP20260627004", "华南中心仓", "德邦", "22", "交接中", "吴发", "-"], ["SHP20260626003", "华东一仓", "安能", "8", "已交接", "吴发", "2026-06-26 16:20"]],
  },
  {
    id: "return-receipts",
    title: "退货入库",
    desc: "处理售后退货到仓验收，判断可售、不可售、拒收并生成上架。",
    no: "RET20260628001",
    filters: [["return_receipt_no", "退货单号", "text"], ["after_sale_no", "售后单号", "text"], ["accept_result", "验收结果", "select", ["全部", "待验收", "可售", "不可售", "拒收"]], ["return_status", "状态", "select", ["全部", "待收货", "验收中", "待上架", "已完成", "已拒收"]]],
    columns: [["return_receipt_no", "退货单号"], ["after_sale_no", "售后单"], ["sku_code", "SKU"], ["received_qty", "收货数"], ["accept_result", "验收结果"], ["return_status", "状态"], ["operator", "操作人"], ["completed_at", "完成时间"]],
    form: [["return_receipt_no", "退货单号", "text"], ["after_sale_no", "售后单", "text"], ["sku_code", "SKU", "text"], ["received_qty", "收货数", "number"], ["accept_result", "验收结果", "select", ["待验收", "可售", "不可售", "拒收"]], ["return_status", "状态", "select", ["待收货", "验收中", "待上架", "已完成", "已拒收"]], ["operator", "操作人", "text"]],
    actions: ["查看", "修改", "收货", "验收", "上架", "拒收"],
    rows: [["RET20260628001", "AS20260628006", "SKU-PK-1182", "2", "待验收", "验收中", "陈收", "-"], ["RET20260627002", "AS20260627004", "SKU-WH-3320", "1", "可售", "待上架", "陈收", "-"], ["RET20260626003", "AS20260626009", "SKU-EL-1008", "1", "拒收", "已拒收", "陈收", "2026-06-26 12:00"]],
  },
  {
    id: "rejected",
    title: "不合格品",
    desc: "管理不合格、待退供和待报废库存，支持暂存、转待退、报废和退供。",
    no: "NG20260628001",
    filters: [["sku_code", "SKU", "text"], ["supplier", "供应商", "text"], ["rejected_status", "状态", "select", ["全部", "暂存", "待退供", "待报废", "已退供", "已报废"]], ["location", "位置", "text"]],
    columns: [["rejected_no", "不合格单号"], ["supplier", "供应商"], ["sku_code", "SKU"], ["batch_no", "批次"], ["qty", "数量"], ["location", "位置"], ["rejected_status", "状态"], ["reason", "原因"]],
    form: [["rejected_no", "不合格单号", "text"], ["supplier", "供应商", "text"], ["sku_code", "SKU", "text"], ["batch_no", "批次", "text"], ["qty", "数量", "number"], ["location", "位置", "text"], ["rejected_status", "状态", "select", ["暂存", "待退供", "待报废", "已退供", "已报废"]], ["reason", "原因", "textarea"]],
    actions: ["查看", "修改", "暂存", "转待退", "报废", "退供"],
    rows: [["NG20260628001", "深圳芯合电子", "SKU-EL-1008", "E20260612", "20", "NG-01-01-01", "待退供", "性能异常"], ["NG20260627002", "宁波丰达", "SKU-WH-3320", "T20260602", "5", "NG-01-01-02", "暂存", "外箱破损"], ["NG20260625004", "杭州启明包装", "SKU-PK-2031", "P20260608", "12", "SCRAP-01", "待报废", "污染破损"]],
  },
  {
    id: "counts",
    title: "盘点管理",
    desc: "创建盘点计划、下发盘点任务、复盘并确认差异。",
    no: "CNT20260628001",
    filters: [["count_plan_no", "盘点计划号", "text"], ["warehouse", "仓库", "text"], ["count_type", "盘点类型", "select", ["全部", "全盘", "动盘", "循环盘", "抽盘"]], ["plan_status", "状态", "select", ["全部", "草稿", "已下发", "盘点中", "差异处理中", "已完成", "已取消"]]],
    columns: [["count_plan_no", "盘点计划号"], ["warehouse", "仓库"], ["count_type", "盘点类型"], ["scope", "范围"], ["book_qty", "账面数"], ["counted_qty", "实盘数"], ["diff_qty", "差异"], ["plan_status", "状态"], ["created_by", "创建人"]],
    form: [["count_plan_no", "盘点计划号", "text"], ["warehouse", "仓库", "text"], ["count_type", "盘点类型", "select", ["全盘", "动盘", "循环盘", "抽盘"]], ["scope", "范围", "text"], ["book_qty", "账面数", "number"], ["counted_qty", "实盘数", "number"], ["diff_qty", "差异", "number"], ["plan_status", "状态", "select", ["草稿", "已下发", "盘点中", "差异处理中", "已完成", "已取消"]], ["created_by", "创建人", "text"]],
    actions: ["查看", "修改", "下发", "复盘", "确认差异", "取消"],
    rows: [["CNT20260628001", "华东一仓", "循环盘", "A区", "12000", "11992", "-8", "差异处理中", "仓库主管"], ["CNT20260627002", "华南中心仓", "抽盘", "B区", "8600", "8600", "0", "已完成", "仓库主管"], ["CNT20260626003", "华东一仓", "动盘", "异常区", "120", "118", "-2", "盘点中", "仓库主管"]],
  },
  {
    id: "exceptions",
    title: "异常处理",
    desc: "处理短收、超收、错货、破损、库位不符、复核差异和盘点差异。",
    no: "EXP20260628001",
    filters: [["exception_no", "异常单号", "text"], ["source_type", "来源", "select", ["全部", "收货", "质检", "上架", "分配", "拣货", "复核", "发货", "盘点"]], ["exception_type", "异常类型", "select", ["全部", "短收", "超收", "错货", "破损", "库位不符", "复核差异", "盘点差异"]], ["exception_status", "状态", "select", ["全部", "待处理", "处理中", "已关闭"]]],
    columns: [["exception_no", "异常单号"], ["source_type", "来源"], ["warehouse", "仓库"], ["exception_type", "异常类型"], ["responsible_party", "责任方"], ["exception_status", "状态"], ["description", "说明"], ["created_at", "创建时间"]],
    form: [["exception_no", "异常单号", "text"], ["source_type", "来源", "select", ["收货", "质检", "上架", "分配", "拣货", "复核", "发货", "盘点"]], ["warehouse", "仓库", "text"], ["exception_type", "异常类型", "select", ["短收", "超收", "错货", "破损", "库位不符", "复核差异", "盘点差异"]], ["responsible_party", "责任方", "select", ["供应商", "仓库", "物流", "客户", "系统"]], ["exception_status", "状态", "select", ["待处理", "处理中", "已关闭"]], ["description", "说明", "textarea"]],
    actions: ["查看", "修改", "分派", "处理", "关闭"],
    rows: [["EXP20260628001", "收货", "华东一仓", "短收", "供应商", "待处理", "ASN 少到 40 件", "2026-06-28 10:20"], ["EXP20260627003", "拣货", "华南中心仓", "库位不符", "仓库", "处理中", "库位实物与任务不一致", "2026-06-27 15:20"], ["EXP20260626008", "复核", "华东一仓", "复核差异", "仓库", "已关闭", "少拣 1 件已返拣", "2026-06-26 12:10"]],
  },
  {
    id: "logs",
    title: "操作日志",
    desc: "查询仓内关键作业操作，支持审计和问题追踪。",
    no: "LOG20260628001",
    filters: [["operator", "操作人", "text"], ["warehouse", "仓库", "text"], ["object_type", "对象类型", "select", ["全部", "入库", "收货", "质检", "上架", "出库", "波次", "拣货", "复核", "包裹", "盘点", "异常"]], ["result", "结果", "select", ["全部", "成功", "失败"]]],
    columns: [["operator", "操作人"], ["warehouse", "仓库"], ["object_type", "对象类型"], ["object_no", "对象单号"], ["action_type", "动作"], ["result", "结果"], ["fail_reason", "失败原因"], ["created_at", "操作时间"]],
    form: [["operator", "操作人", "text"], ["warehouse", "仓库", "text"], ["object_type", "对象类型", "text"], ["object_no", "对象单号", "text"], ["action_type", "动作", "text"], ["result", "结果", "select", ["成功", "失败"]], ["fail_reason", "失败原因", "textarea"]],
    actions: ["查看", "修改", "导出"],
    rows: [["钱拣", "华南中心仓", "拣货", "PKT20260627008", "扫库位", "成功", "-", "2026-06-28 10:25"], ["陈收", "华东一仓", "收货", "RCV20260625009", "异常登记", "成功", "-", "2026-06-25 10:40"], ["孙复", "华东一仓", "复核", "PACK20260626002", "称重", "成功", "-", "2026-06-26 15:08"]],
  },
  {
    id: "enums",
    title: "枚举配置",
    desc: "维护 WMS 页面枚举项的中文标签、排序、颜色和启停状态。",
    no: "ENUM20260628001",
    filters: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["status", "状态", "select", ["全部", "启用", "停用"]]],
    columns: [["enum_type", "枚举类型"], ["enum_value", "枚举值"], ["label", "显示名称"], ["sort_no", "排序"], ["status", "状态"], ["updated_by", "更新人"], ["updated_at", "更新时间"]],
    form: [["enum_type", "枚举类型", "text"], ["enum_value", "枚举值", "text"], ["label", "显示名称", "text"], ["sort_no", "排序", "number"], ["status", "状态", "select", ["启用", "停用"]], ["remark", "备注", "textarea"]],
    actions: ["查看", "修改", "排序", "停用"],
    rows: [["WMS_OUTBOUND_STATUS", "PENDING_PICK", "待拣货", "40", "启用", "系统管理员", "2026-06-20 10:18"], ["PICKING_TASK_STATUS", "EXCEPTION", "异常", "40", "启用", "系统管理员", "2026-06-20 10:20"], ["WMS_EXCEPTION_TYPE", "REVIEW_DIFF", "复核差异", "60", "启用", "系统管理员", "2026-06-18 09:15"]],
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
          <div class="brand-title">WMS系统原型</div>
          <div class="brand-subtitle">Warehouse Management Prototype</div>
        </div>
        <nav class="nav-group">
          ${modules.map((m) => `<button class="nav-item ${m.id === active.id ? "active" : ""}" data-route="/${m.id}"><span class="nav-dot"></span><span>${esc(m.title)}</span></button>`).join("")}
        </nav>
      </aside>
      <main class="main">
        <header class="topbar">
          <div class="breadcrumb">WMS系统 / ${esc(active.title)}</div>
          <div class="top-actions">
            <input class="global-search" placeholder="搜索单号、SKU、库位、容器" />
            <span class="user-chip">仓库主管</span>
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
  const stats = [["待收货", "16", "采购到货 9 单"], ["待质检", "8", "含 2 单紧急"], ["待上架", "21", "不合格 3 单"], ["待拣货", "34", "波次 5 个"], ["仓内异常", "7", "复核差异 2 单"]];
  return `
    ${head(modules[0])}
    <div class="stats-grid">${stats.map(([a, b, c]) => `<div class="stat-card"><div class="stat-title">${a}</div><div class="stat-value">${b}</div><div class="stat-note">${c}</div></div>`).join("")}</div>
    <div class="dashboard-grid">
      <div class="panel">
        <h2 class="panel-title">仓内作业闭环</h2>
        <div class="flow-steps">
          ${["入库接单", "收货扫码", "质检判定", "推荐库位上架", "出库接单与波次", "库位拣货与容器绑定", "复核包装", "发货交接", "盘点与异常处理"].map((x, i) => `<div class="flow-step"><span>${i + 1}. ${x}</span><span>进入</span></div>`).join("")}
        </div>
      </div>
      <div class="panel">
        <h2 class="panel-title">今日待办</h2>
        <table>
          <thead><tr><th>类型</th><th>对象</th><th>状态</th></tr></thead>
          <tbody>
            <tr><td>收货</td><td>RCV20260627005</td><td>${tag("部分收货", "purple")}</td></tr>
            <tr><td>质检</td><td>QC20260625008</td><td>${tag("不合格", "red")}</td></tr>
            <tr><td>拣货</td><td>PKT20260627008</td><td>${tag("拣货中", "purple")}</td></tr>
            <tr><td>异常</td><td>EXP20260628001</td><td>${tag("待处理", "amber")}</td></tr>
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
    const danger = ["取消", "关闭", "异常", "拒收", "报废", "停用", "解绑"].includes(a) ? "danger" : "";
    return `<button class="btn text ${danger}" data-action="row-op" data-op="${esc(a)}" data-no="${esc(primaryNo(mod, record))}">${esc(a)}</button>`;
  }).join("");
}

function cell(value) {
  const raw = String(value || "");
  if (["待到货", "待收货", "待检", "待上架", "待接单", "待分配", "待拣货", "待复核", "待发货", "待交接", "待验收", "待处理", "草稿"].includes(raw)) return tag(raw, "amber");
  if (["合格", "可拣", "已上架", "已收货", "已完成", "已发货", "已释放", "已拣货", "已交接", "已打包", "可售", "已关闭", "成功", "启用"].includes(raw)) return tag(raw, "green");
  if (["不合格", "异常", "分配失败", "复核异常", "已取消", "已拒收", "待报废", "已报废", "停用", "失败"].includes(raw)) return tag(raw, "red");
  if (["到货中", "收货中", "部分收货", "部分上架", "部分拣货", "拣货中", "复核中", "交接中", "验收中", "处理中", "差异处理中", "待退供"].includes(raw)) return tag(raw, "purple");
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
          <h2 class="detail-title">作业明细</h2>
          <table>
            <thead><tr><th>库区</th><th>库位</th><th>SKU</th><th>批次</th><th>应作业</th><th>已作业</th><th>容器/包裹</th></tr></thead>
            <tbody>
              <tr><td>A区</td><td>A-01-02-03</td><td>SKU-PK-1182</td><td>B20260601</td><td>120</td><td>80</td><td>BOX-001</td></tr>
              <tr><td>B区</td><td>B-02-01-05</td><td>SKU-WH-3320</td><td>T20260602</td><td>50</td><td>50</td><td>CART-018</td></tr>
              <tr><td>异常区</td><td>NG-01-01-01</td><td>SKU-EL-1008</td><td>E20260612</td><td>20</td><td>20</td><td>BOX-009</td></tr>
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
          <h2 class="detail-title">作业流转</h2>
          <div class="timeline">${["接收外部单据", "生成仓内任务", "扫描库位/SKU/容器", "确认作业数量", "发布实物事实事件"].map((s, i) => `<div class="timeline-item"><div class="timeline-main">${s}</div><div class="timeline-sub">2026-06-${24 + i} ${9 + i}:00，记录 WMS 操作日志</div></div>`).join("")}</div>
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
        <div class="field"><label>备注</label><textarea placeholder="记录扫描结果、作业差异、改库位原因或异常处理意见">${mode === "edit" ? "根据当前仓内作业情况调整后保存。" : ""}</textarea></div>
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
