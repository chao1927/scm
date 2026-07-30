package com.chaobo.scm.supplier.application.shared;

/**
 * AuditLogRepository。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface AuditLogRepository {

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param context 业务处理参数或成员，类型为 {@code CommandContext}
     * @param operationType 业务处理参数或成员，类型为 {@code String}
     * @param targetType 业务处理参数或成员，类型为 {@code String}
     * @param targetId 业务或技术标识，类型为 {@code long}
     * @param targetNo 可追踪业务编码，类型为 {@code String}
     * @param beforeSnapshot 业务处理参数或成员，类型为 {@code String}
     * @param afterSnapshot 业务处理参数或成员，类型为 {@code String}
     */
    void save(CommandContext context, String operationType, String targetType, long targetId, String targetNo, String beforeSnapshot, String afterSnapshot);
}
