package com.chaobo.scm.wms.infrastructure.persistence.inbound;

import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import com.chaobo.scm.wms.domain.inbound.InboundOrderStatus;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisInboundOrderRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisInboundOrderRepository implements InboundOrderRepository {

    /**
     * mapper（类型：{@code InboundOrderMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundOrderMapper mapper;

    /**
     * 创建 MyBatisInboundOrderRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code InboundOrderMapper}
     */
    public MyBatisInboundOrderRepository(InboundOrderMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<InboundOrderAggregate>}
     */
    @Override
    public Optional<InboundOrderAggregate> findById(long id) {
        return Optional.ofNullable(mapper.findById(id)).map(this::toAggregate);
    }

    /**
     * 查询并返回 {@code findBySource}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<InboundOrderAggregate>}
     */
    @Override
    public Optional<InboundOrderAggregate> findBySource(String sourceType, String sourceNo, long warehouseId) {
        return Optional.ofNullable(mapper.findBySource(sourceType, sourceNo, warehouseId)).map(this::toAggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param order 业务处理参数或成员，类型为 {@code InboundOrderAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(InboundOrderAggregate order, long operatorId) {
        if (mapper.findById(order.id()) == null) {
            mapper.insert(order.id(), order.inboundNo(), order.sourceType(), order.sourceNo(), order.warehouseId(),
                order.ownerId(), order.status().code(), order.expectedArrivalAt(), order.cancelReason(),
                order.version(), operatorId);
            return;
        }
        mapper.update(order.id(), order.status().code(), order.cancelReason(), order.version(), operatorId);
    }

    /**
     * 转换数据模型 {@code toAggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code InboundOrderMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code InboundOrderAggregate}
     */
    private InboundOrderAggregate toAggregate(InboundOrderMapper.Row row) {
        return new InboundOrderAggregate(row.id(), row.inboundNo(), row.sourceType(), row.sourceNo(),
            row.warehouseId(), row.ownerId(), InboundOrderStatus.of(row.status()),
            row.expectedArrivalAt(), row.cancelReason(), row.version());
    }
}
