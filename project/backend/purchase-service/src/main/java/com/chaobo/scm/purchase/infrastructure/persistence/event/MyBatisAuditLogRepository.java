package com.chaobo.scm.purchase.infrastructure.persistence.event;

import com.chaobo.scm.purchase.application.shared.AuditLogRepository;
import com.chaobo.scm.purchase.application.shared.CommandContext;
import org.springframework.stereotype.Repository;

/**
 * MyBatisAuditLogRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisAuditLogRepository implements AuditLogRepository {

    /**
     * mapper（类型：{@code EventPersistenceMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final EventPersistenceMapper mapper;

    /**
     * 创建 MyBatisAuditLogRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code EventPersistenceMapper}
     */
    public MyBatisAuditLogRepository(EventPersistenceMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operation 业务处理参数或成员，类型为 {@code String}
     * @param targetType 业务处理参数或成员，类型为 {@code String}
     * @param targetId 业务或技术标识，类型为 {@code long}
     * @param targetNo 可追踪业务编码，类型为 {@code String}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param afterSnapshot 业务处理参数或成员，类型为 {@code String}
     */
    @Override
    public void save(CommandContext context, String operation, String targetType, long targetId, String targetNo, String beforeSnapshot, String afterSnapshot) {
        mapper.insertAuditLog(context.requestId(), context.traceId(), context.operatorId(), context.operatorName(), operation, targetType, targetId, targetNo, beforeSnapshot, afterSnapshot);
    }
}
