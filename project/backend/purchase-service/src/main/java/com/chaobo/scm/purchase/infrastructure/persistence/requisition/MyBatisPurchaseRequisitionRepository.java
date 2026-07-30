package com.chaobo.scm.purchase.infrastructure.persistence.requisition;

import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionAggregate;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionLine;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionRepository;
import com.chaobo.scm.purchase.domain.requisition.PurchaseRequisitionStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisPurchaseRequisitionRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisPurchaseRequisitionRepository implements PurchaseRequisitionRepository {

    /**
     * mapper（类型：{@code PurchaseRequisitionMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseRequisitionMapper mapper;

    /**
     * 创建 MyBatisPurchaseRequisitionRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PurchaseRequisitionMapper}
     */
    public MyBatisPurchaseRequisitionRepository(PurchaseRequisitionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<PurchaseRequisitionAggregate>}
     */
    @Override
    public Optional<PurchaseRequisitionAggregate> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::aggregate);
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param requisitionNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<PurchaseRequisitionAggregate>}
     */
    @Override
    public Optional<PurchaseRequisitionAggregate> findByNo(String requisitionNo) {
        return Optional.ofNullable(mapper.findByNo(requisitionNo)).map(this::aggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code PurchaseRequisitionAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(PurchaseRequisitionAggregate aggregate, long operatorId) {
        var existed = mapper.findById(aggregate.id()) != null;
        if (existed) {
            mapper.updateHeader(aggregate.id(), aggregate.status().code(), aggregate.reason(), aggregate.version(), operatorId);
            mapper.deleteLines(aggregate.id());
        } else {
            mapper.insertHeader(aggregate.id(), aggregate.requisitionNo(), aggregate.applicantId(), aggregate.purchaseOrgId(), aggregate.demandDepartmentId(), aggregate.status().code(), aggregate.reason(), aggregate.version(), operatorId);
        }
        for (PurchaseRequisitionLine line : aggregate.lines()) {
            mapper.insertLine(new PurchaseRequisitionMapper.LineRow(line.lineId(), aggregate.id(), line.skuCode(), line.requestedQty(), line.approvedQty(), line.convertedQty(), line.purchaseUnit(), line.requiredDate(), line.remark()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PurchaseRequisitionMapper.HeaderRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseRequisitionAggregate}
     */
    private PurchaseRequisitionAggregate aggregate(PurchaseRequisitionMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream().map(line -> new PurchaseRequisitionLine(line.lineId(), line.skuCode(), line.requestedQty(), line.approvedQty(), line.convertedQty(), line.purchaseUnit(), line.requiredDate(), line.remark())).toList();
        return new PurchaseRequisitionAggregate(row.id(), row.requisitionNo(), row.applicantId(), row.purchaseOrgId(), row.demandDepartmentId(), PurchaseRequisitionStatus.of(row.status()), row.reason(), row.version(), lines);
    }
}
