package com.chaobo.scm.supplier.application.shared;

import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import java.util.List;

/**
 * OutboxRepository。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface OutboxRepository {

    /**
     * 执行命令 {@code saveAll}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param events 业务处理参数或成员，类型为 {@code List<DomainEvent>}
     */
    void saveAll(List<DomainEvent> events);
}
