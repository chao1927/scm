package com.chaobo.scm.purchase.infrastructure.persistence.inbound;

import com.chaobo.scm.common.api.PageResult;
import com.chaobo.scm.purchase.application.inbound.*;
import com.chaobo.scm.purchase.domain.inbound.InboundStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisInboundTrackingReadModel。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisInboundTrackingReadModel implements InboundTrackingReadModelPort {

    /**
     * mapper（类型：{@code InboundTrackingMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundTrackingMapper mapper;

    /**
     * 创建 MyBatisInboundTrackingReadModel。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code InboundTrackingMapper}
     */
    public MyBatisInboundTrackingReadModel(InboundTrackingMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 处理当前类型职责中的操作 {@code page}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param purchaseOrgId 业务或技术标识，类型为 {@code Long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code Integer}
     * @param pageNo 可追踪业务编码，类型为 {@code int}
     * @param pageSize 业务处理参数或成员，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PageResult<InboundTrackingView>}
     */
    @Override
    public PageResult<InboundTrackingView> page(Long purchaseOrgId, String orderNo, String asnNo, String warehouseCode, Integer status, int pageNo, int pageSize) {
        var total = mapper.count(purchaseOrgId, orderNo, asnNo, warehouseCode, status);
        var records = mapper.page(purchaseOrgId, orderNo, asnNo, warehouseCode, status, (pageNo - 1) * pageSize, pageSize).stream().map(this::view).toList();
        return new PageResult<>(pageNo, pageSize, total, records);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Optional<InboundTrackingView>}
     */
    @Override
    public Optional<InboundTrackingView> detail(String inboundNo) {
        return Optional.ofNullable(mapper.findByNo(inboundNo)).map(this::view);
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code InboundTrackingMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundTrackingView}
     */
    private InboundTrackingView view(InboundTrackingMapper.Row row) {
        var status = InboundStatus.of(row.status());
        return new InboundTrackingView(row.id(), row.inboundNo(), row.orderNo(), row.asnNo(), row.supplierId(), row.purchaseOrgId(), row.warehouseCode(), row.skuCode(), row.notifiedQty(), row.receivedQty(), row.qualifiedQty(), row.unqualifiedQty(), row.putawayQty(), row.status(), status.label(), row.exceptionReason(), row.version(), true);
    }
}
