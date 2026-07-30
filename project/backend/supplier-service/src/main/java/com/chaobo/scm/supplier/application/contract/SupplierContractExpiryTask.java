package com.chaobo.scm.supplier.application.contract;

import com.chaobo.scm.supplier.application.shared.*;
import com.chaobo.scm.supplier.domain.contract.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.contract.SupplierContractExpiryMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

/**
 * SupplierContractExpiryTask。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class SupplierContractExpiryTask {

    /**
     * candidates（类型：{@code SupplierContractExpiryMapper}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractExpiryMapper candidates;

    /**
     * contracts（类型：{@code SupplierContractRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierContractRepository contracts;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * audit（类型：{@code AuditLogRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AuditLogRepository audit;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * 创建 SupplierContractExpiryTask。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param candidates 业务或技术标识，类型为 {@code SupplierContractExpiryMapper}
     * @param contracts 业务处理参数或成员，类型为 {@code SupplierContractRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param audit 业务处理参数或成员，类型为 {@code AuditLogRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public SupplierContractExpiryTask(SupplierContractExpiryMapper candidates, SupplierContractRepository contracts, OutboxRepository outbox, AuditLogRepository audit, IdentifierGenerator ids) {
        this.candidates = candidates;
        this.contracts = contracts;
        this.outbox = outbox;
        this.audit = audit;
        this.ids = ids;
    }

    /**
     * 处理当前类型职责中的操作 {@code expire}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    @Scheduled(cron = "${scm.supplier.contract-expire-cron:0 10 0 * * *}")
    @Transactional(rollbackFor = Exception.class)
    public void expire() {
        for (long id : candidates.expiredIds()) {
            var contract = contracts.find(id).orElse(null);
            if (contract == null) {
                continue;
            }
            contract.expire(0, ids);
            var events = contract.pullEvents();
            if (events.isEmpty()) {
                continue;
            }
            contracts.save(contract, 0);
            outbox.saveAll(events);
            var context = new CommandContext(0, "SYSTEM", 0, null, "contract-expire-" + id, null, "contract-expire-" + id, Set.of());
            audit.save(context, "EXPIRE_CONTRACT", "SUPPLIER_CONTRACT", id, contract.no(), null, "{\"status\":" + contract.status().code() + "}");
        }
    }
}
