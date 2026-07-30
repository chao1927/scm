package com.chaobo.scm.inventory.application;

import java.util.function.Supplier;

/**
 * 单元测试使用的同步事务执行器。
 *
 * @author SCM Team
 */
final class ImmediateInventoryEventTransactions implements InventoryEventTransactions {

    @Override
    public <T> T required(Supplier<T> action) {
        return action.get();
    }

    @Override
    public <T> T requiresNew(Supplier<T> action) {
        return action.get();
    }
}
