package com.chaobo.scm.supplier.application.outbox;

/**
 * MessageBrokerPort。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface MessageBrokerPort {

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param message 业务处理参数或成员，类型为 {@code OutboxMessage}
     * @throws Exception 当消息代理暂时不可用或发布失败时抛出，由 Outbox 调度器重试
     */
    void publish(OutboxMessage message) throws Exception;
}
