# Implementation Plan

## 3. 执行批次

| 批次 | 可并行任务 | 检查点 |
| --- | --- | --- |
| P0 | `PAR-NEXT-001`、`PAR-NEXT-002` | CP-0 |
| 1 | `SUP-NEXT-*` | CP-1 |
| 3 | `WMS-NEXT-001B/C` | CP-3 |

## 4. Agent 领取协议

这里重复提到 `PAR-NEXT-001` 不属于批次任务集合。
