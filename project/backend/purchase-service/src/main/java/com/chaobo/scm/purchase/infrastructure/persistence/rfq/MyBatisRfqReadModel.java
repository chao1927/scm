package com.chaobo.scm.purchase.infrastructure.persistence.rfq;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.rfq.RfqReadModelPort;
import com.chaobo.scm.purchase.application.rfq.RfqView;
import com.chaobo.scm.purchase.domain.rfq.RfqStatus;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * MyBatisRfqReadModel。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisRfqReadModel implements RfqReadModelPort {

    /**
     * mapper（类型：{@code RfqMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final RfqMapper mapper;

    /**
     * queryMapper（类型：{@code RfqQueryMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final RfqQueryMapper queryMapper;

    /**
     * 创建 MyBatisRfqReadModel。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code RfqMapper}
     * @param queryMapper 持久化访问依赖，类型为 {@code RfqQueryMapper}
     */
    public MyBatisRfqReadModel(RfqMapper mapper, RfqQueryMapper queryMapper) {
        this.mapper = mapper;
        this.queryMapper = queryMapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param categoryCode 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code Long}
     * @param deadlineFrom 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param deadlineTo 业务处理参数或成员，类型为 {@code OffsetDateTime}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<RfqView>}
     */
    @Override
    public PageResult<RfqView> page(Long purchaseOrgId, Integer status, String categoryCode, Long supplierId, OffsetDateTime deadlineFrom, OffsetDateTime deadlineTo, int pageNo, int pageSize) {
        var total = queryMapper.count(purchaseOrgId, status, categoryCode, supplierId, deadlineFrom, deadlineTo);
        var records = queryMapper.page(purchaseOrgId, status, categoryCode, supplierId, deadlineFrom, deadlineTo, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<RfqView>}
     */
    @Override
    public Optional<RfqView> detail(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::view);
    }

    /**
     * 处理当前类型职责中的操作 {@code detailByNo}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<RfqView>}
     */
    @Override
    public Optional<RfqView> detailByNo(String rfqNo) {
        return Optional.ofNullable(mapper.findByNo(rfqNo)).map(this::view);
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code RfqMapper.HeaderRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code RfqView}
     */
    private RfqView view(RfqMapper.HeaderRow row) {
        var status = RfqStatus.of(row.status());
        var lines = mapper.findLines(row.id()).stream().map(line -> new RfqView.Line(line.lineId(), line.skuCode(), line.targetQty(), line.uom(), line.requiredDeliveryDate(), line.qualityRequirement())).toList();
        var invitations = mapper.findInvitations(row.id()).stream().map(invitation -> new RfqView.Invitation(invitation.invitationId(), invitation.supplierId(), invitation.quoteStatus())).toList();
        return new RfqView(row.id(), row.rfqNo(), row.rfqType(), row.purchaseOrgId(), row.categoryCode(), row.sourceRequisitionNo(), row.quoteDeadline(), row.status(), status.label(), row.publishedAt(), row.closeReason(), row.version(), invitations.size(), row.createdAt(), row.updatedAt(), lines, invitations);
    }
}
