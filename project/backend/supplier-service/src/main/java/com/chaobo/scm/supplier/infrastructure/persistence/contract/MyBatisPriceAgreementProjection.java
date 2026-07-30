package com.chaobo.scm.supplier.infrastructure.persistence.contract;

import com.chaobo.scm.supplier.application.contract.*;
import com.chaobo.scm.supplier.domain.contract.*;
import com.chaobo.scm.supplier.domain.quote.*;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import com.chaobo.scm.supplier.infrastructure.persistence.item.SupplierItemPriceSnapshotMapper;
import org.springframework.stereotype.Repository;

/**
 * MyBatisPriceAgreementProjection。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisPriceAgreementProjection implements PriceAgreementProjectionPort {

    /**
     * mapper（类型：{@code PriceAgreementProjectionMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PriceAgreementProjectionMapper mapper;

    /**
     * snapshots（类型：{@code SupplierItemPriceSnapshotMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final SupplierItemPriceSnapshotMapper snapshots;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * 创建 MyBatisPriceAgreementProjection。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PriceAgreementProjectionMapper}
     * @param snapshots 业务处理参数或成员，类型为 {@code SupplierItemPriceSnapshotMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public MyBatisPriceAgreementProjection(PriceAgreementProjectionMapper mapper, SupplierItemPriceSnapshotMapper snapshots, IdentifierGenerator ids) {
        this.mapper = mapper;
        this.snapshots = snapshots;
        this.ids = ids;
    }

    /**
     * 处理当前类型职责中的操作 {@code activate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     * @param quote 业务处理参数或成员，类型为 {@code SupplierQuoteAggregate}
     */
    public void activate(SupplierContractAggregate contract, SupplierQuoteAggregate quote) {
        mapper.upsert(new PriceAgreementProjectionMapper.Header(contract.agreement(), contract.id(), quote.id(), contract.supplierId(), quote.currency(), contract.from(), contract.to(), 1, contract.version()));
        Long agreementId = mapper.agreementId(contract.id());
        for (var line : quote.lines()) {
            mapper.upsertLine(new PriceAgreementProjectionMapper.Line(agreementId, line.skuCode(), line.unitPrice(), line.taxRate(), line.moq(), line.deliveryDays()));
            snapshots.upsert(ids.nextId(), contract.supplierId(), line.skuCode(), contract.agreement(), quote.currency(), line.unitPrice(), line.taxRate(), contract.from(), contract.to(), contract.id(), quote.id(), contract.version());
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code renew}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     */
    public void renew(SupplierContractAggregate contract) {
        mapper.renew(contract.id(), contract.to(), contract.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code terminate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param contract 业务处理参数或成员，类型为 {@code SupplierContractAggregate}
     */
    public void terminate(SupplierContractAggregate contract) {
        mapper.terminate(contract.id(), contract.version());
    }
}
