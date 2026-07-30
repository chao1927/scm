package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;

/**
 * IntegrationCommandGateway。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。定义跨进程或跨层协作端口，隔离调用方与具体技术实现。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface IntegrationCommandGateway {

    /**
     * 执行命令 {@code dispatch}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code IntegrationCommandMapper.CommandRow}
     * @return 执行命令的结果，类型为 {@code DispatchReceipt}
     */
    DispatchReceipt dispatch(IntegrationCommandMapper.CommandRow command);

    /**
     * DispatchReceipt。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    record DispatchReceipt(String remoteReference) {
    }
}
