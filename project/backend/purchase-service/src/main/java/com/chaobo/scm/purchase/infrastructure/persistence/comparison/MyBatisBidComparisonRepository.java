package com.chaobo.scm.purchase.infrastructure.persistence.comparison;

import com.chaobo.scm.purchase.domain.comparison.BidCandidate;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonAggregate;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonRepository;
import com.chaobo.scm.purchase.domain.comparison.BidComparisonStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisBidComparisonRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisBidComparisonRepository implements BidComparisonRepository {

    /**
     * mapper（类型：{@code BidComparisonMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final BidComparisonMapper mapper;

    /**
     * 创建 MyBatisBidComparisonRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code BidComparisonMapper}
     */
    public MyBatisBidComparisonRepository(BidComparisonMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param compareNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<BidComparisonAggregate>}
     */
    @Override
    public Optional<BidComparisonAggregate> findByNo(String compareNo) {
        return Optional.ofNullable(mapper.findByNo(compareNo)).map(this::aggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code BidComparisonAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(BidComparisonAggregate aggregate, long operatorId) {
        var existed = mapper.findByNo(aggregate.compareNo()) != null;
        if (existed) {
            mapper.updateHeader(aggregate.id(), aggregate.status().code(), aggregate.awardedCandidateId(), aggregate.decisionReason(), aggregate.decidedBy(), aggregate.decidedAt(), aggregate.version(), operatorId);
            mapper.deleteCandidates(aggregate.id());
        } else {
            mapper.insertHeader(aggregate.id(), aggregate.compareNo(), aggregate.rfqNo(), aggregate.purchaseOrgId(), aggregate.currency(), aggregate.status().code(), aggregate.awardedCandidateId(), aggregate.decisionReason(), aggregate.decidedBy(), aggregate.decidedAt(), aggregate.version(), operatorId);
        }
        for (BidCandidate candidate : aggregate.candidates()) {
            mapper.insertCandidate(new BidComparisonMapper.CandidateRow(candidate.candidateId(), aggregate.id(), candidate.supplierId(), candidate.supplierName(), candidate.quoteNo(), candidate.skuCode(), candidate.quoteQty(), candidate.unitPrice(), candidate.taxRate(), candidate.deliveryDays(), candidate.supplierScore(), candidate.transportScore(), candidate.estimatedFreightCost(), candidate.totalCost(), candidate.compositeScore(), candidate.awarded()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code BidComparisonMapper.HeaderRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BidComparisonAggregate}
     */
    private BidComparisonAggregate aggregate(BidComparisonMapper.HeaderRow row) {
        var candidates = mapper.findCandidates(row.id()).stream().map(candidate -> new BidCandidate(candidate.candidateId(), candidate.supplierId(), candidate.supplierName(), candidate.quoteNo(), candidate.skuCode(), candidate.quoteQty(), candidate.unitPrice(), candidate.taxRate(), candidate.deliveryDays(), candidate.supplierScore(), candidate.transportScore(), candidate.estimatedFreightCost(), candidate.awarded())).toList();
        return new BidComparisonAggregate(row.id(), row.compareNo(), row.rfqNo(), row.purchaseOrgId(), row.currency(), BidComparisonStatus.of(row.status()), row.awardedCandidateId(), row.decisionReason(), row.decidedBy(), row.decidedAt(), row.version(), candidates);
    }
}
