package com.chaobo.scm.wms.application.outbound;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.outbound.OutboundOrderAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.outbound.OutboundMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OutboundApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class OutboundApplicationService {

    /**
     * mapper（类型：{@code OutboundMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final OutboundMapper mapper;

    /**
     * events（类型：{@code WmsEventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final WmsEventPublisher events;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 OutboundApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code OutboundMapper}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public OutboundApplicationService(OutboundMapper mapper, WmsEventPublisher events) {
        this.mapper = mapper;
        this.events = events;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result create(String sourceType, String sourceNo, long warehouseId, long operator) {
        var existed = mapper.source(sourceType, sourceNo, warehouseId);
        if (existed != null) {
            return view(toAggregate(existed), true);
        }
        long id = ids.incrementAndGet();
        var outboundNo = "WOB" + id;
        mapper.insert(id, outboundNo, sourceType, sourceNo, warehouseId, operator);
        events.publish("WmsOutboundOrderCreated", "OUTBOUND", outboundNo, 0, payload(outboundNo));
        return new Result(id, outboundNo, 1, 0, false);
    }

    /**
     * 处理当前类型职责中的操作 {@code allocate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result allocate(String sourceType, String sourceNo, long warehouseId, int version, long operator) {
        var outbound = toAggregate(required(sourceType, sourceNo, warehouseId));
        if (outbound.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "出库单版本冲突");
        }
        outbound.allocate();
        save(outbound, version, operator);
        events.publish("WmsOutboundAllocated", "OUTBOUND", outbound.no(), outbound.version(), payload(outbound.no()));
        return view(outbound, false);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result cancel(String sourceType, String sourceNo, long warehouseId, int version, String reason, long operator) {
        var outbound = toAggregate(required(sourceType, sourceNo, warehouseId));
        if (outbound.version() != version) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "出库单版本冲突");
        }
        outbound.cancel(reason);
        save(outbound, version, operator);
        events.publish("WmsOutboundCancelled", "OUTBOUND", outbound.no(), outbound.version(), payload(outbound.no()));
        return view(outbound, false);
    }

    /**
     * 查询并返回 {@code required}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code OutboundMapper.Row}
     */
    private OutboundMapper.Row required(String sourceType, String sourceNo, long warehouseId) {
        var row = mapper.source(sourceType, sourceNo, warehouseId);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "出库单不存在");
        }
        return row;
    }

    /**
     * 转换数据模型 {@code toAggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code OutboundMapper.Row}
     * @return 转换数据模型的结果，类型为 {@code OutboundOrderAggregate}
     */
    private OutboundOrderAggregate toAggregate(OutboundMapper.Row row) {
        return new OutboundOrderAggregate(row.id(), row.no(), row.sourceType(), row.sourceNo(), row.warehouseId(), row.status(), row.version());
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param outbound 业务处理参数或成员，类型为 {@code OutboundOrderAggregate}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    private void save(OutboundOrderAggregate outbound, int oldVersion, long operator) {
        if (mapper.update(outbound.id(), outbound.status(), outbound.version(), oldVersion, operator) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "出库单版本冲突");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param outbound 业务处理参数或成员，类型为 {@code OutboundOrderAggregate}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result view(OutboundOrderAggregate outbound, boolean duplicated) {
        return new Result(outbound.id(), outbound.no(), outbound.status(), outbound.version(), duplicated);
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(String outboundNo) {
        return """
            {"outboundNo":"%s"}
            """.formatted(outboundNo).trim();
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(long id, String no, int status, int version, boolean duplicated) {
    }
}
