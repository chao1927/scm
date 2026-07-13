# BMS-REQ-002 费用调整、对账、账单、发票与财务交接

## 目标

将费用明细推进到对账、账单、发票和财务交接，保证金额守恒、状态可审计、对外结算事实可追溯。

## 范围

| 类型 | 内容 |
| --- | --- |
| 接口 | `GET /api/bms/v1/charges`、`POST /charges/{chargeNo}/recalculate|void`、`POST /charge-adjustments`、`POST /charge-adjustments/{adjustmentNo}/execute`、`POST /reconciliation-statements`、`POST /reconciliation-statements/{reconciliationNo}/confirm|difference`、`POST /bills`、`POST /bills/{billNo}/confirm`、`POST /invoices`、`POST /invoices/{invoiceNo}/issue`、`POST /financial-handovers`、`POST /financial-handovers/{handoverNo}/post|fail` |
| 领域 | `ChargeAdjustmentAggregate`、`ReconciliationAggregate`、`BillAggregate`、`InvoiceAggregate`、`FinanceHandoverAggregate` |
| 不变量 | 已确认费用不可直接重算；调整必须审批后执行；对账确认金额必须等于系统金额；确认对账后才可生成账单；发票金额不得超过账单金额；财务交接成功后账单进入已过账状态 |
| 事件 | `ChargeRecalculated/Voided/Adjusted`、`ReconciliationIssued/Confirmed/DifferenceRaised`、`BillGenerated/Confirmed`、`InvoiceRequested/Issued`、`FinanceHandoverRequested`、`FinancialPosted/Failed` |
| 持久化 | `bms_adjustment`、`bms_reconciliation`、`bms_bill`、`bms_invoice`、`bms_finance_handover` |

## 验收

- 费用可重算、作废和通过调整单产生正负调整明细。
- 待对账费用能汇总生成对账单，确认后费用状态同步确认。
- 已确认对账单生成账单，账单可确认、开票、交财务并接收财务结果。
- 领域和应用服务测试覆盖金额与状态机约束。
