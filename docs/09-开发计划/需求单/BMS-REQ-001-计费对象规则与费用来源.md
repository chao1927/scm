# BMS-REQ-001 计费对象、计费规则与费用来源

## 目标

建立 BMS 计费结算的首个业务闭环入口：维护计费对象和计费规则，接收 WMS/TMS/采购/OMS 等系统的费用来源事件，并按已发布规则生成可追溯费用明细。

## 范围

| 类型 | 内容 |
| --- | --- |
| 接口 | `POST/GET /api/bms/v1/billing-subjects`、`POST /billing-subjects/{objectCode}/enable|disable`、`POST/GET /billing-rules`、`POST /billing-rules/{ruleNo}/publish`、`POST /openapi/bms/v1/charge-sources`、`GET /charge-sources`、`POST /charge-sources/{sourceNo}/replay` |
| 领域 | `BillingObjectAggregate`、`BillingRuleAggregate`、`ChargeSourceAggregate`、`ChargeDetailAggregate` |
| 不变量 | 停用计费对象不可计费；已发布规则同对象+费用类型+有效期不可重叠；费用来源按来源系统+幂等键只处理一次；费用明细必须保留来源、规则版本、数量、单价、税额和账期 |
| 事件 | `BillingObjectCreated/Enabled/Disabled`、`BillingRulePublished`、`ChargeSourceAccepted/Failed`、`ChargeCalculated` |
| 持久化 | `bms_billing_object`、`bms_billing_rule`、`bms_charge_source`、`bms_charge_detail`、`bms_domain_event`、`bms_event_consume_log`、`bms_operation_audit_log` |

## 验收

- 能创建计费对象、规则并发布规则。
- 费用来源重复提交返回同一来源记录。
- 规则缺失时来源进入失败状态，发布规则后可重放生成费用明细。
- 领域、应用服务和接口测试通过。
