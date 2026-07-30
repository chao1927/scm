package com.chaobo.scm.purchase.infrastructure.persistence.orderchange;

import com.chaobo.scm.purchase.domain.orderchange.*;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * MyBatisPurchaseOrderChangeRepository。
 *
 * <p>位于基础设施层，负责实现持久化、远程调用、消息或安全等技术端口，不决定业务规则。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Repository
public class MyBatisPurchaseOrderChangeRepository implements PurchaseOrderChangeRepository {

    /**
     * mapper（类型：{@code PurchaseOrderChangeMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final PurchaseOrderChangeMapper mapper;

    /**
     * 创建 MyBatisPurchaseOrderChangeRepository。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code PurchaseOrderChangeMapper}
     */
    public MyBatisPurchaseOrderChangeRepository(PurchaseOrderChangeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param changeNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<PurchaseOrderChangeAggregate>}
     */
    @Override
    public Optional<PurchaseOrderChangeAggregate> findByNo(String changeNo) {
        return Optional.ofNullable(mapper.findByNo(changeNo)).map(this::aggregate);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该实现遵守上游端口契约；异常、幂等和返回语义必须与接口约定保持一致。
     * @param change 业务处理参数或成员，类型为 {@code PurchaseOrderChangeAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    @Override
    public void save(PurchaseOrderChangeAggregate change, long operatorId) {
        var existed = mapper.findByNo(change.changeNo()) != null;
        if (existed) {
            mapper.updateStatus(change.id(), change.status().code(), change.version(), operatorId);
        } else {
            mapper.insert(new PurchaseOrderChangeMapper.ChangeRow(change.id(), change.changeNo(), change.orderNo(), change.changeType(), change.beforeSnapshot(), change.afterSnapshot(), change.changeReason(), change.status().code(), change.version()), operatorId);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code PurchaseOrderChangeMapper.ChangeRow}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderChangeAggregate}
     */
    private PurchaseOrderChangeAggregate aggregate(PurchaseOrderChangeMapper.ChangeRow row) {
        return new PurchaseOrderChangeAggregate(row.id(), row.changeNo(), row.orderNo(), row.changeType(), row.beforeSnapshot(), row.afterSnapshot(), row.changeReason(), PurchaseOrderChangeStatus.of(row.status()), row.version());
    }
}
