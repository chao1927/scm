# INT-REQ-006 九服务 Dubbo Provider 仓库闭环

> 状态：实现完成、跨进程实跑待本机 Docker 恢复。真实生产网络和容量验收仍归 `INT-REQ-005B`。

## 1. 目标

把各限界上下文内部可完成的 Dubbo Provider/Consumer、契约测试和跨进程冒烟从“外部联调阻塞”中拆出，确保同步命令和查询不是只有接口定义或本地模拟。

## 2. 范围

- 盘点九服务需要暴露的 `@DubboService` Provider、版本、分组、方法、超时和错误语义。
- Consumer 只依赖发布接口和防腐层 DTO，不依赖 Provider 内部实现。
- 使用本地 Nacos 和独立 JVM 完成注册、发现、调用、超时、Provider 重启恢复和未注册失败关闭。
- RPC 不承担已发生领域事实的广播；业务事件仍走 RocketMQ。

## 3. 验收

- 每个声明的 Dubbo 契约存在真实 Provider 或有明确“不提供同步接口”的决定。
- 契约兼容、超时、异常映射、幂等命令和数据范围有测试。
- 跨进程冒烟不是同 JVM Mock；Provider 重启后 Consumer 可恢复发现。
- 外部地址、ACL 和容量条件缺失只阻塞 `INT-REQ-005B`，不得阻塞本需求的仓库交付。

## 4. 契约冻结与实现证据（2026-08-06）

| 契约 | Provider | 实际落点 |
| --- | --- | --- |
| `WmsCollaborationApi` | WMS | ASN 预约创建/取消真实入库单，退供创建真实出库单，持久化幂等收据 |
| `TmsCollaborationApi` | TMS | 入库/退供运输请求持久化、取消 CAS、幂等冲突校验 |
| `InventoryCollaborationApi` | Inventory | 退供库存锁定调用真实冻结聚合，释放调用真实解冻并产生台账/Outbox |
| `BmsCollaborationApi` | BMS | 退供结算请求持久化并写入 RocketMQ Outbox |
| `MasterDataCollaborationApi` | MDM | 供应商映射到真实主数据审核生命周期，状态同步支持启用/冻结/停用 |
| `IamCollaborationApi` | IAM | 用户供应商数据范围持久化、快照失效、审计与 Outbox |

- 六个 Provider 统一 group=`scm-collaboration`、version=`1.0.0`、timeout=`2000ms`、retries=`0`，通过各模块契约测试。
- Purchase、OMS、Supplier 不额外暴露同步 Provider：它们拥有的已发生业务事实继续只通过 RocketMQ 发布；Supplier 作为上述六个契约的 Consumer。
- 六模块连同依赖在 JDK 17 下执行 267 个测试，0 失败、1 个真实 MySQL 条件测试跳过；另有 TMS 幂等/冲突/取消和 MDM 重新启用行为测试 7/7 通过。
- `project/qa/dubbo-local-smoke.sh` 已提供独立 JVM 注册发现、Provider 下线失败关闭、重启恢复和重复命令幂等脚本；当前 Docker Desktop 因宿主机 `resource temporarily unavailable` 未能启动，本项不伪造实跑结果。

## 继续上下文

当前结论：Dubbo Provider/Consumer、持久化和测试代码闭环已完成；本地跨 JVM 实跑仍需 Docker daemon 恢复。

关键假设：本地 Nacos 环境可按部署手册启动。

待决问题：各上下文最终同步接口清单需在任务开始时冻结。

下一步：Docker 恢复后执行 `project/qa/dubbo-local-smoke.sh`，通过后关闭 `INT-NEXT-002`。
