# QA-REQ-002 Spring Web 与 MySQL/Flyway 集成测试基线

## 1. 目标

建立可在本机和 CI 重复执行的真实 MySQL 集成测试，覆盖 Flyway、MyBatis SQL、数据库唯一约束和乐观锁；HTTP/JWT 鉴权继续由各服务 MockMvc Web 测试覆盖。

## 2. 执行

```bash
cd project/backend
mvn -Pmysql-it -pl inventory-service -am test
```

默认 `mvn test` 会跳过 Docker 集成测试；启用 `mysql-it` Profile 后 Testcontainers 启动 `mysql:8.0.36`。

## 3. 验收

- Inventory 全部 Flyway 迁移在空 MySQL 数据库成功应用。
- MyBatis 真实插入/更新 SQL 可执行。
- 相同旧版本第二次更新返回 0，证明乐观锁生效。
- 相同来源+事件号第二次 Inbox 插入触发 MySQL 唯一约束。
- 现有 `PurchaseSecurityWebTest` 等 MockMvc 测试覆盖匿名、权限命名空间、数据范围与幂等头 HTTP 契约。
