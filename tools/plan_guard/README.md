# 计划与契约一致性校验

这是 `PAR-NEXT-002` 的只读校验入口，不会修改需求、计划、任务或业务代码。

在仓库根目录执行：

```bash
python3 -m tools.plan_guard.validator
python3 -m unittest discover -s tools/plan_guard/tests -v
```

校验范围：

1. `需求单/` 的 64 张正式需求与状态矩阵 12.3 精确索引一一对应，且不存在重复归属；
2. Maven Reactor 和服务目录严格保留九个可部署业务服务，`scm-common` 存在且不配置 Spring Boot 打包插件；
3. `06-多Agent并行执行计划.md`、`tasks/plan.md` 执行批次与 `tasks/todo.md` 的 `NEXT` 任务集合一致；
4. 进行中任务不存在重复编号、重复 Agent 或文件范围包含/重叠，且文件范围可以被机器解析。

退出码为 `0` 表示通过，`1` 表示存在违规，可直接接入本地检查或 CI。
