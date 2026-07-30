package com.chaobo.scm.wms.application.shared;

/**
 * WmsEventPublisher。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。以稳定接口声明调用方所需能力，具体实现可在不影响调用方的前提下替换。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface WmsEventPublisher {

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param payload 业务处理参数或成员，类型为 {@code String}
     */
    void publish(String eventType, String aggregateType, String aggregateId, int version, String payload);
}
