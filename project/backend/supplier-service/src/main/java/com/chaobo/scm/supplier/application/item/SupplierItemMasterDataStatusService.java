package com.chaobo.scm.supplier.application.item;

import com.chaobo.scm.supplier.application.shared.OutboxRepository;
import com.chaobo.scm.supplier.domain.item.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;

/**
 * SupplierItemMasterDataStatusService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class SupplierItemMasterDataStatusService {

    /**
     * repo（类型：{@code SupplierItemRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemRepository repo;

    /**
     * outbox（类型：{@code OutboxRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboxRepository outbox;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * 创建 SupplierItemMasterDataStatusService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code SupplierItemRepository}
     * @param outbox 业务处理参数或成员，类型为 {@code OutboxRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public SupplierItemMasterDataStatusService(SupplierItemRepository repo, OutboxRepository outbox, IdentifierGenerator ids) {
        this.repo = repo;
        this.outbox = outbox;
        this.ids = ids;
    }

    /**
     * 处理当前类型职责中的操作 {@code pauseBySupplier}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void pauseBySupplier(long supplierId, String reason) {
        for (var item : repo.findAvailableBySupplier(supplierId)) {
            pause(item, reason);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code pauseBySku}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void pauseBySku(String skuCode, String reason) {
        for (var item : repo.findAvailableBySku(skuCode)) {
            pause(item, reason);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code pause}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param item 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    private void pause(SupplierItemAggregate item, String reason) {
        item.pause(reason, 0, ids);
        repo.save(item, 0);
        outbox.saveAll(item.pullEvents());
    }
}
