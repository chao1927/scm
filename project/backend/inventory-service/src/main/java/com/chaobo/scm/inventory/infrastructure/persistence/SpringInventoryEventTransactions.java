package com.chaobo.scm.inventory.infrastructure.persistence;

import com.chaobo.scm.inventory.application.InventoryEventTransactions;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Spring 事件事务执行器。
 *
 * <p>主处理事务保证库存账户、流水、Outbox、游标和 Inbox 成功状态原子提交；失败记录使用独立事务。
 *
 * @author SCM Team
 */
@Component
public class SpringInventoryEventTransactions implements InventoryEventTransactions {

    private final TransactionTemplate required;
    private final TransactionTemplate requiresNew;

    public SpringInventoryEventTransactions(PlatformTransactionManager transactionManager) {
        required = new TransactionTemplate(transactionManager);
        requiresNew = new TransactionTemplate(transactionManager);
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public <T> T required(Supplier<T> action) {
        return required.execute(status -> action.get());
    }

    @Override
    public <T> T requiresNew(Supplier<T> action) {
        return requiresNew.execute(status -> action.get());
    }
}
