package com.chaobo.scm.wms.application.returning;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.returning.ReturnOperationAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.returning.ReturnOperationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ReturnOperationApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class ReturnOperationApplicationService {

    /**
     * mapper（类型：{@code ReturnOperationMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final ReturnOperationMapper mapper;

    /**
     * inbound（类型：{@code InboundOrderApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundOrderApplicationService inbound;

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
     * 创建 ReturnOperationApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code ReturnOperationMapper}
     * @param inbound 业务处理参数或成员，类型为 {@code InboundOrderApplicationService}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public ReturnOperationApplicationService(ReturnOperationMapper mapper, InboundOrderApplicationService inbound, WmsEventPublisher events) {
        this.mapper = mapper;
        this.inbound = inbound;
        this.events = events;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param c 业务处理参数或成员，类型为 {@code Create}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result create(Create c, long operator) {
        var existing = mapper.find(c.afterSaleNo());
        if (existing != null) {
            return view(existing, true);
        }
        var a = new ReturnOperationAggregate(ids.incrementAndGet(), c.afterSaleNo(), c.rmaNo(), c.ownerId(), c.warehouseId(), c.sku(), c.batchNo(), c.qty(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, ReturnOperationAggregate.RECEIVING, 0);
        mapper.insert(row(a));
        inbound.create(new InboundOrderApplicationService.Create("AFTERSALE_RETURN", c.afterSaleNo(),
            c.warehouseId(), c.ownerId(), null, "return-inbound-" + c.afterSaleNo()), operator);
        return view(row(a), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result receive(String no, BigDecimal qty, int version) {
        var a = load(no);
        int old = a.version();
        a.receive(qty, version);
        save(a, old);
        events.publish("ReturnReceived", "RETURN_OPERATION", no, a.version(), payload(a));
        return view(row(a), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code inspect}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param c 业务处理参数或成员，类型为 {@code Inspect}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result inspect(String no, Inspect c) {
        var a = load(no);
        int old = a.version();
        a.inspect(c.sellableQty(), c.defectiveQty(), c.frozenQty(), c.scrappedQty(), c.unmatchedQty(), c.version());
        save(a, old);
        events.publish("ReturnInspected", "RETURN_OPERATION", no, a.version(), payload(a));
        return view(row(a), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    public Result detail(String no) {
        return view(row(load(no)), false);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code ReturnOperationAggregate}
     */
    private ReturnOperationAggregate load(String no) {
        var r = mapper.find(no);
        if (r == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退货作业不存在");
        }
        return new ReturnOperationAggregate(r.id(), r.afterSaleNo(), r.rmaNo(), r.ownerId(), r.warehouseId(), r.sku(), r.batchNo(), r.expectedQty(), r.receivedQty(), r.sellableQty(), r.defectiveQty(), r.frozenQty(), r.scrappedQty(), r.unmatchedQty(), r.status(), r.version());
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code ReturnOperationAggregate}
     * @param old 业务处理参数或成员，类型为 {@code int}
     */
    private void save(ReturnOperationAggregate a, int old) {
        if (mapper.update(row(a), old) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "退货作业持久化版本冲突");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code ReturnOperationAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(ReturnOperationAggregate a) {
        return "{\"afterSaleNo\":\"" + a.afterSaleNo() + "\",\"rmaNo\":\"" + a.rmaNo() + "\",\"ownerId\":" + a.ownerId() + ",\"warehouseId\":" + a.warehouseId() + ",\"sku\":\"" + a.sku() + "\",\"batchNo\":" + (a.batchNo() == null ? "null" : "\"" + a.batchNo() + "\"") + ",\"receivedQty\":" + a.receivedQty() + ",\"sellableQty\":" + a.sellableQty() + ",\"defectiveQty\":" + a.defectiveQty() + ",\"frozenQty\":" + a.frozenQty() + ",\"scrappedQty\":" + a.scrappedQty() + ",\"unmatchedQty\":" + a.unmatchedQty() + ",\"version\":" + a.version() + "}";
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code ReturnOperationAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReturnOperationMapper.Row}
     */
    private static ReturnOperationMapper.Row row(ReturnOperationAggregate a) {
        return new ReturnOperationMapper.Row(a.id(), a.afterSaleNo(), a.rmaNo(), a.ownerId(), a.warehouseId(), a.sku(), a.batchNo(), a.expectedQty(), a.receivedQty(), a.sellableQty(), a.defectiveQty(), a.frozenQty(), a.scrappedQty(), a.unmatchedQty(), a.status(), a.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code ReturnOperationMapper.Row}
     * @param d 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result view(ReturnOperationMapper.Row r, boolean d) {
        return new Result(r.afterSaleNo(), r.rmaNo(), r.ownerId(), r.warehouseId(), r.sku(), r.expectedQty(), r.receivedQty(), r.sellableQty(), r.defectiveQty(), r.frozenQty(), r.scrappedQty(), r.unmatchedQty(), r.status(), r.version(), d);
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(String afterSaleNo, String rmaNo, long ownerId, long warehouseId, String sku, String batchNo, BigDecimal qty) {
    }

    /**
     * Inspect。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Inspect(BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty, int version) {
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(String afterSaleNo, String rmaNo, long ownerId, long warehouseId, String sku, BigDecimal expectedQty, BigDecimal receivedQty, BigDecimal sellableQty, BigDecimal defectiveQty, BigDecimal frozenQty, BigDecimal scrappedQty, BigDecimal unmatchedQty, int status, int version, boolean duplicated) {
    }
}
