package com.chaobo.scm.wms.application.transfer;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.wms.application.inbound.InboundOrderApplicationService;
import com.chaobo.scm.wms.application.outbound.OutboundApplicationService;
import com.chaobo.scm.wms.application.shared.WmsEventPublisher;
import com.chaobo.scm.wms.domain.transfer.TransferOperationAggregate;
import com.chaobo.scm.wms.infrastructure.persistence.transfer.TransferOperationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TransferOperationApplicationService。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class TransferOperationApplicationService {

    /**
     * mapper（类型：{@code TransferOperationMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final TransferOperationMapper mapper;

    /**
     * outboundOrders（类型：{@code OutboundApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final OutboundApplicationService outboundOrders;

    /**
     * inboundOrders（类型：{@code InboundOrderApplicationService}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final InboundOrderApplicationService inboundOrders;

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
     * 创建 TransferOperationApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code TransferOperationMapper}
     * @param outboundOrders 业务处理参数或成员，类型为 {@code OutboundApplicationService}
     * @param inboundOrders 业务处理参数或成员，类型为 {@code InboundOrderApplicationService}
     * @param events 业务处理参数或成员，类型为 {@code WmsEventPublisher}
     */
    public TransferOperationApplicationService(TransferOperationMapper mapper, OutboundApplicationService outboundOrders, InboundOrderApplicationService inboundOrders, WmsEventPublisher events) {
        this.mapper = mapper;
        this.outboundOrders = outboundOrders;
        this.inboundOrders = inboundOrders;
        this.events = events;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code Create}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result create(Create command, long operatorId) {
        var existing = mapper.find(command.transferNo());
        if (existing != null) {
            return view(existing, true);
        }
        long id = ids.incrementAndGet();
        var aggregate = new TransferOperationAggregate(id, command.transferNo(), command.ownerId(), command.sourceWarehouseId(), command.targetWarehouseId(), command.sku(), command.batchNo(), command.qty(), BigDecimal.ZERO, BigDecimal.ZERO, TransferOperationAggregate.OUTBOUND_PENDING, 0);
        mapper.insert(row(aggregate));
        outboundOrders.create("INVENTORY_TRANSFER", command.transferNo(), command.sourceWarehouseId(), operatorId);
        return view(row(aggregate), false);
    }

    /**
     * 执行命令 {@code completeOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result completeOutbound(String transferNo, BigDecimal qty, int version) {
        var aggregate = load(transferNo);
        int oldVersion = aggregate.version();
        aggregate.completeOutbound(qty, version);
        save(aggregate, oldVersion);
        publish("TransferOutboundCompleted", aggregate, qty, true);
        return view(row(aggregate), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code prepareInbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param transferVersion 乐观锁或契约版本，类型为 {@code int}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result prepareInbound(String transferNo, int transferVersion, long operatorId) {
        var aggregate = load(transferNo);
        if (aggregate.status() == TransferOperationAggregate.INBOUND_PENDING || aggregate.status() == TransferOperationAggregate.RECEIVED) {
            return view(row(aggregate), true);
        }
        int oldVersion = aggregate.version();
        aggregate.prepareInbound(oldVersion);
        save(aggregate, oldVersion);
        inboundOrders.create(new InboundOrderApplicationService.Create("INVENTORY_TRANSFER", transferNo, aggregate.targetWarehouseId(), null, "transfer-inbound-" + transferNo + "-" + transferVersion), operatorId);
        return view(row(aggregate), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param finalReceipt 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result receive(String transferNo, BigDecimal qty, boolean finalReceipt, int version) {
        var aggregate = load(transferNo);
        int oldVersion = aggregate.version();
        aggregate.receive(qty, finalReceipt, version);
        save(aggregate, oldVersion);
        publish("TransferReceived", aggregate, qty, finalReceipt || aggregate.status() == TransferOperationAggregate.RECEIVED);
        return view(row(aggregate), false);
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code Result}
     */
    @Transactional(rollbackFor = Exception.class)
    public Result cancel(String transferNo) {
        var aggregate = load(transferNo);
        if (aggregate.status() == TransferOperationAggregate.CANCELLED) {
            return view(row(aggregate), true);
        }
        int oldVersion = aggregate.version();
        aggregate.cancel(oldVersion);
        save(aggregate, oldVersion);
        var outbound = outboundOrders.create("INVENTORY_TRANSFER", transferNo, aggregate.sourceWarehouseId(), 0);
        outboundOrders.cancel("INVENTORY_TRANSFER", transferNo, aggregate.sourceWarehouseId(), outbound.version(), "中央库存调拨取消", 0);
        events.publish("TransferCancellationCompensated", "TRANSFER_OPERATION", transferNo, aggregate.version(), payload(aggregate, BigDecimal.ZERO, false));
        return view(row(aggregate), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    public Result detail(String transferNo) {
        return view(row(load(transferNo)), false);
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code TransferOperationAggregate}
     */
    private TransferOperationAggregate load(String transferNo) {
        var row = mapper.find(transferNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "WMS调拨任务不存在");
        }
        return new TransferOperationAggregate(row.id(), row.transferNo(), row.ownerId(), row.sourceWarehouseId(), row.targetWarehouseId(), row.sku(), row.batchNo(), row.requestedQty(), row.outboundQty(), row.receivedQty(), row.status(), row.version());
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code TransferOperationAggregate}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     */
    private void save(TransferOperationAggregate aggregate, int oldVersion) {
        if (mapper.update(aggregate.id(), aggregate.outboundQty(), aggregate.receivedQty(), aggregate.status(), aggregate.version(), oldVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "WMS调拨任务持久化版本冲突");
        }
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregate 业务处理参数或成员，类型为 {@code TransferOperationAggregate}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param finalReceipt 业务处理参数或成员，类型为 {@code boolean}
     */
    private void publish(String type, TransferOperationAggregate aggregate, BigDecimal qty, boolean finalReceipt) {
        events.publish(type, "TRANSFER_OPERATION", aggregate.transferNo(), aggregate.version(), payload(aggregate, qty, finalReceipt));
    }

    /**
     * 处理当前类型职责中的操作 {@code payload}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code TransferOperationAggregate}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param finalReceipt 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String payload(TransferOperationAggregate a, BigDecimal qty, boolean finalReceipt) {
        return "{\"transferNo\":\"" + a.transferNo() + "\",\"qty\":" + qty + ",\"finalReceipt\":" + finalReceipt + ",\"version\":" + a.version() + "}";
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param a 业务处理参数或成员，类型为 {@code TransferOperationAggregate}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferOperationMapper.Row}
     */
    private static TransferOperationMapper.Row row(TransferOperationAggregate a) {
        return new TransferOperationMapper.Row(a.id(), a.transferNo(), a.ownerId(), a.sourceWarehouseId(), a.targetWarehouseId(), a.sku(), a.batchNo(), a.requestedQty(), a.outboundQty(), a.receivedQty(), a.status(), a.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param r 业务处理参数或成员，类型为 {@code TransferOperationMapper.Row}
     * @param duplicate 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Result}
     */
    private static Result view(TransferOperationMapper.Row r, boolean duplicate) {
        return new Result(r.transferNo(), r.sourceWarehouseId(), r.targetWarehouseId(), r.sku(), r.requestedQty(), r.outboundQty(), r.receivedQty(), r.status(), r.version(), duplicate);
    }

    /**
     * Create。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Create(String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal qty) {
    }

    /**
     * Result。
     *
     * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record Result(String transferNo, long sourceWarehouseId, long targetWarehouseId, String sku, BigDecimal requestedQty, BigDecimal outboundQty, BigDecimal receivedQty, int status, int version, boolean duplicated) {
    }
}
