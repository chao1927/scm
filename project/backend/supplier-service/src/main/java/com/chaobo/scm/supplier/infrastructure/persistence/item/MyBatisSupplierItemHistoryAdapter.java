package com.chaobo.scm.supplier.infrastructure.persistence.item;

import com.chaobo.scm.supplier.application.item.SupplierItemHistoryPort;
import com.chaobo.scm.supplier.domain.item.SupplierItemAggregate;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Repository;

/**
 * MyBatisSupplierItemHistoryAdapter。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisSupplierItemHistoryAdapter implements SupplierItemHistoryPort {

    /**
     * mapper（类型：{@code SupplierItemHistoryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemHistoryMapper mapper;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * 创建 MyBatisSupplierItemHistoryAdapter。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code SupplierItemHistoryMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public MyBatisSupplierItemHistoryAdapter(SupplierItemHistoryMapper mapper, IdentifierGenerator ids) {
        this.mapper = mapper;
        this.ids = ids;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordCondition}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param aggregate 业务处理参数或成员，类型为 {@code SupplierItemAggregate}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    public void recordCondition(SupplierItemAggregate aggregate, String type, long operator) {
        var condition = aggregate.condition();
        mapper.insert(ids.nextId(), aggregate.itemId(), aggregate.supplierId(), aggregate.skuCode(), aggregate.supplierSkuCode(), condition.moq(), condition.mpq(), condition.leadTimeDays(), condition.purchaseUnit(), condition.effectiveFrom(), condition.effectiveTo(), aggregate.version(), type, operator);
    }
}
