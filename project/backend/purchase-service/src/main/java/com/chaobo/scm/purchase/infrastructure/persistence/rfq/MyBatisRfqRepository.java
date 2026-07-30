package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import com.chaobo.scm.purchase.domain.rfq.RfqAggregate;
import com.chaobo.scm.purchase.domain.rfq.RfqInvitation;
import com.chaobo.scm.purchase.domain.rfq.RfqLine;
import com.chaobo.scm.purchase.domain.rfq.RfqRepository;
import com.chaobo.scm.purchase.domain.rfq.RfqStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisRfqRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisRfqRepository implements RfqRepository {

    /**
     * mapper（类型：{@code RfqMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final RfqMapper mapper;

    /**
     * 创建 MyBatisRfqRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code RfqMapper}
     */
    public MyBatisRfqRepository(RfqMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<RfqAggregate>}
     */
    @Override
    public Optional<RfqAggregate> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::aggregate);
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<RfqAggregate>}
     */
    @Override
    public Optional<RfqAggregate> findByNo(String rfqNo) {
        return Optional.ofNullable(mapper.findByNo(rfqNo)).map(this::aggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code RfqAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(RfqAggregate aggregate, long operatorId) {
        var existed = mapper.findById(aggregate.id()) != null;
        if (existed) {
            mapper.updateHeader(aggregate.id(), aggregate.status().code(), aggregate.publishedAt(), aggregate.closeReason(), aggregate.version(), operatorId);
            mapper.deleteLines(aggregate.id());
            mapper.deleteInvitations(aggregate.id());
        } else {
            mapper.insertHeader(aggregate.id(), aggregate.rfqNo(), aggregate.rfqType(), aggregate.purchaseOrgId(), aggregate.categoryCode(), aggregate.sourceRequisitionNo(), aggregate.quoteDeadline(), aggregate.status().code(), aggregate.publishedAt(), aggregate.closeReason(), aggregate.version(), operatorId);
        }
        for (RfqLine line : aggregate.lines()) {
            mapper.insertLine(new RfqMapper.LineRow(line.lineId(), aggregate.id(), line.skuCode(), line.targetQty(), line.uom(), line.requiredDeliveryDate(), line.qualityRequirement()));
        }
        for (RfqInvitation invitation : aggregate.invitations()) {
            mapper.insertInvitation(new RfqMapper.InvitationRow(invitation.invitationId(), aggregate.id(), invitation.supplierId(), invitation.quoteStatus()));
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code RfqMapper.HeaderRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code RfqAggregate}
     */
    private RfqAggregate aggregate(RfqMapper.HeaderRow row) {
        var lines = mapper.findLines(row.id()).stream().map(line -> new RfqLine(line.lineId(), line.skuCode(), line.targetQty(), line.uom(), line.requiredDeliveryDate(), line.qualityRequirement())).toList();
        var invitations = mapper.findInvitations(row.id()).stream().map(invitation -> new RfqInvitation(invitation.invitationId(), invitation.supplierId(), invitation.quoteStatus())).toList();
        return new RfqAggregate(row.id(), row.rfqNo(), row.rfqType(), row.purchaseOrgId(), row.categoryCode(), row.sourceRequisitionNo(), row.quoteDeadline(), RfqStatus.of(row.status()), row.publishedAt(), row.closeReason(), row.version(), lines, invitations);
    }
}
