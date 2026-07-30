package com.chaobo.scm.supplier.application.shared;

/**
 * CommandResult。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public record CommandResult(long aggregateId, String businessNo, int status, String statusName, int version, String eventCode, boolean idempotentHit) {

    /**
     * 处理当前类型职责中的操作 {@code asIdempotentHit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CommandResult}
     */
    public CommandResult asIdempotentHit() {
        return new CommandResult(aggregateId, businessNo, status, statusName, version, eventCode, true);
    }
}
