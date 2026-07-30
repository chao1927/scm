package com.chaobo.scm.inventory.application;

import java.util.function.Supplier;

/**
 * 事件处理事务边界端口。
 *
 * <p>业务处理与 Inbox 成功状态共用主事务；失败状态使用新事务持久化，避免主事务回滚时丢失失败证据。
 *
 * @author SCM Team
 */
public interface InventoryEventTransactions {

    /**
     * 在当前或新建主事务中执行。
     *
     * @param action 事务动作
     * @param <T> 返回类型
     * @return 动作结果
     */
    <T> T required(Supplier<T> action);

    /**
     * 在独立新事务中执行。
     *
     * @param action 事务动作
     * @param <T> 返回类型
     * @return 动作结果
     */
    <T> T requiresNew(Supplier<T> action);
}
