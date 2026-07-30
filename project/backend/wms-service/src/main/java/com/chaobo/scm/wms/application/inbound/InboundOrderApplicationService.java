package com.chaobo.scm.wms.application.inbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.domain.inbound.InboundOrderAggregate;
import com.chaobo.scm.wms.domain.inbound.InboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

/**
 * InboundOrderApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class InboundOrderApplicationService {

    /**
     * repository（类型：{@code InboundOrderRepository}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final InboundOrderRepository repository;

    /**
     * sequence（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 InboundOrderApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repository 持久化访问依赖，类型为 {@code InboundOrderRepository}
     */
    public InboundOrderApplicationService(InboundOrderRepository repository) {
        this.repository = repository;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code Create}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code WmsCommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public WmsCommandResult create(Create command, long operatorId) {
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "外部创建入库单必须提供幂等键");
        }
        var existed = repository.findBySource(command.sourceType(), command.sourceNo(), command.warehouseId());
        if (existed.isPresent()) {
            return result(existed.get(), true);
        }
        var id = sequence.incrementAndGet();
        var inboundNo = "WIB" + LocalDate.now().toString().replace("-", "") + id;
        var order = InboundOrderAggregate.create(id, inboundNo, command.sourceType(), command.sourceNo(), command.warehouseId(), command.ownerId(), command.expectedArrivalAt());
        repository.save(order, operatorId);
        return result(order, false);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param command 用例输入命令，类型为 {@code Cancel}
     * @param warehouseScope 业务处理参数或成员，类型为 {@code long}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code WmsCommandResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public WmsCommandResult cancel(long id, Cancel command, long warehouseScope, long operatorId) {
        var order = repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "WMS入库单不存在"));
        if (warehouseScope > 0 && warehouseScope != order.warehouseId()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无仓库数据权限");
        }
        if (order.version() != command.version()) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "WMS入库单已被其他人修改");
        }
        order.cancel(command.reason());
        repository.save(order, operatorId);
        return result(order, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param order 业务处理参数或成员，类型为 {@code InboundOrderAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WmsCommandResult}
     */
    private static WmsCommandResult result(InboundOrderAggregate order, boolean duplicated) {
        return new WmsCommandResult(order.id(), order.inboundNo(), order.status().code(), order.status().label(), order.version(), duplicated);
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(String sourceType, String sourceNo, long warehouseId, long ownerId, OffsetDateTime expectedArrivalAt, String idempotencyKey) {
    }

    /**
     * Cancel。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Cancel(int version, String reason) {
    }
}
