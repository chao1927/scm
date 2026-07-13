# MDM-REQ-002 主数据记录创建、审核与版本闭环

## 1. 目标

补齐主数据记录从草稿、提交审核、审核通过、驳回、冻结、停用到版本快照生成的首轮闭环，使 SKU、供应商、仓库、物流商等基础资料具备统一事实源和版本追溯能力。

## 2. 范围

| 项 | 内容 |
| --- | --- |
| 对应计划 | `MDM-API-002 主数据记录创建、审核与状态切换` |
| 写接口 | `POST /api/mdm/v1/master-data-records`；`PUT /api/mdm/v1/master-data-records/{recordNo}`；`POST /api/mdm/v1/master-data-records/{recordNo}/submit-review`；`POST /api/mdm/v1/master-data-records/{recordNo}/approve`；`POST /api/mdm/v1/master-data-records/{recordNo}/reject`；`POST /api/mdm/v1/master-data-records/{recordNo}/freeze`；`POST /api/mdm/v1/master-data-records/{recordNo}/disable` |
| 读接口 | `GET /api/mdm/v1/master-data-records`；`GET /api/mdm/v1/master-data-records/{recordNo}`；`GET /api/mdm/v1/master-data-records/{recordNo}/versions` |
| 聚合 | `MasterDataRecordAggregate`、`MasterDataVersionAggregate` |
| 事件 | `MasterDataDraftCreated`、`MasterDataChanged`、`MasterDataSubmitted`、`MasterDataEnabled`、`MasterDataRejected`、`MasterDataFrozen`、`MasterDataDisabled`、`MasterDataVersionGenerated` |
| 持久化 | `mdm_master_data_record`、`mdm_master_data_version`，复用 `mdm_outbox_event`、`mdm_operation_log` |

## 3. 验收标准

1. 同一类型下 `dataCode` 唯一，重复创建返回冲突。
2. 记录草稿和驳回状态可修改，待审核和已启用不可直接覆盖。
3. 审核通过生成不可变版本快照，并写 Outbox 事件。
4. 冻结和停用必须写原因，并产生可发布事件。
5. 记录、版本、事件和操作日志具备应用服务测试覆盖。

## 4. 风险与后续

首轮用字符串 payload 表达动态字段，后续生产化需要 JSON Schema 校验、字段唯一索引、引用主数据校验、审批系统真实联调和主数据快照缓存。
