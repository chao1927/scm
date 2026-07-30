package com.chaobo.scm.purchase.infrastructure.persistence.inbound;

import com.chaobo.scm.purchase.domain.inbound.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisInboundTrackingRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisInboundTrackingRepository implements InboundTrackingRepository {

    /**
     * mapper（类型：{@code InboundTrackingMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundTrackingMapper mapper;

    /**
     * 创建 MyBatisInboundTrackingRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code InboundTrackingMapper}
     */
    public MyBatisInboundTrackingRepository(InboundTrackingMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<InboundTrackingAggregate>}
     */
    @Override
    public Optional<InboundTrackingAggregate> findByNo(String inboundNo) {
        return Optional.ofNullable(mapper.findByNo(inboundNo)).map(this::aggregate);
    }

    /**
     * 查询并返回 {@code findByAsnNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<InboundTrackingAggregate>}
     */
    @Override
    public Optional<InboundTrackingAggregate> findByAsnNo(String asnNo) {
        return Optional.ofNullable(mapper.findByAsnNo(asnNo)).map(this::aggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param aggregate 业务处理参数或成员，类型为 {@code InboundTrackingAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(InboundTrackingAggregate aggregate, long operatorId) {
        var row = row(aggregate);
        if (mapper.findByNo(aggregate.inboundNo()) == null) {
            mapper.insert(row, operatorId);
        } else {
            mapper.update(row, operatorId);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code InboundTrackingAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundTrackingMapper.Row}
     */
    private InboundTrackingMapper.Row row(InboundTrackingAggregate a) {
        return new InboundTrackingMapper.Row(a.id(), a.inboundNo(), a.orderNo(), a.asnNo(), a.supplierId(), a.purchaseOrgId(), a.warehouseCode(), a.skuCode(), a.notifiedQty(), a.receivedQty(), a.qualifiedQty(), a.unqualifiedQty(), a.putawayQty(), a.status().code(), a.exceptionReason(), a.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code InboundTrackingMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundTrackingAggregate}
     */
    private InboundTrackingAggregate aggregate(InboundTrackingMapper.Row row) {
        return new InboundTrackingAggregate(row.id(), row.inboundNo(), row.orderNo(), row.asnNo(), row.supplierId(), row.purchaseOrgId(), row.warehouseCode(), row.skuCode(), row.notifiedQty(), row.receivedQty(), row.qualifiedQty(), row.unqualifiedQty(), row.putawayQty(), InboundStatus.of(row.status()), row.exceptionReason(), row.version());
    }
}
