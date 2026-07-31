package com.chaobo.scm.inventory.application;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.inventory.domain.StockTransferAggregate;
import com.chaobo.scm.inventory.infrastructure.persistence.StockTransferMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * StockTransferApplicationService。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。面向调用方提供应用用例，协调权限、聚合、资源库和事件发布。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class StockTransferApplicationService {

    /**
     * mapper（类型：{@code StockTransferMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final StockTransferMapper mapper;

    /**
     * stock（类型：{@code StockTransferPorts.StockReservation}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StockTransferPorts.StockReservation stock;

    /**
     * events（类型：{@code StockTransferPorts.EventPublisher}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final StockTransferPorts.EventPublisher events;

    /**
     * ids（类型：{@code AtomicLong}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final AtomicLong ids = new AtomicLong(System.currentTimeMillis());

    /**
     * 创建 StockTransferApplicationService。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param mapper 持久化访问依赖，类型为 {@code StockTransferMapper}
     * @param stock 业务处理参数或成员，类型为 {@code StockTransferPorts.StockReservation}
     * @param events 业务处理参数或成员，类型为 {@code StockTransferPorts.EventPublisher}
     */
    public StockTransferApplicationService(StockTransferMapper mapper, StockTransferPorts.StockReservation stock, StockTransferPorts.EventPublisher events) {
        this.mapper = mapper;
        this.stock = stock;
        this.events = events;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param command 用例输入命令，类型为 {@code CreateCommand}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult create(CreateCommand command, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "调拨创建缺少幂等键");
        }
        StockTransferMapper.Row existing = mapper.findByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return view(existing, true);
        }
        long id = ids.incrementAndGet();
        StockTransferAggregate aggregate = StockTransferAggregate.create(id, "TRF" + id, command.ownerId(), command.sourceWarehouseId(), command.targetWarehouseId(), command.sku(), command.batchNo(), command.qty());
        StockTransferMapper.Row row = row(aggregate, idempotencyKey);
        mapper.insert(row);
        publish("TransferCreated", aggregate);
        return view(row, false);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult submit(String transferNo, int version) {
        return change(transferNo, aggregate -> aggregate.submit(version), "TransferSubmitted");
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult approve(String transferNo, int version) {
        return change(transferNo, aggregate -> aggregate.approve(version), "TransferApproved");
    }

    /**
     * 执行命令 {@code reserve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult reserve(String transferNo, int version) {
        StockTransferAggregate aggregate = load(transferNo);
        String reservationNo = stock.reserve(aggregate.ownerId(), aggregate.sourceWarehouseId(), aggregate.sku(), aggregate.batchNo(), aggregate.requestedQty(), aggregate.transferNo());
        int oldVersion = aggregate.version();
        aggregate.reserve(aggregate.requestedQty(), version);
        save(aggregate, oldVersion);
        publish("TransferStockReserved", aggregate, "\"reservationNo\":\"" + reservationNo + "\"");
        return view(row(aggregate, mapper.find(transferNo).idempotencyKey()), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult recordOutbound(String transferNo, BigDecimal qty, int version) {
        StockTransferMapper.Row current = required(transferNo);
        StockTransferAggregate aggregate = aggregate(current);
        int oldVersion = aggregate.version();
        aggregate.recordOutbound(qty, version);
        stock.outboundForTransfer(transferNo, qty);
        save(aggregate, oldVersion);
        publish("TransferOutboundCompleted", aggregate);
        return view(row(aggregate, current.idempotencyKey()), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code markInTransit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult markInTransit(String transferNo, int version) {
        return change(transferNo, aggregate -> aggregate.markInTransit(version), "TransferInTransit");
    }

    /**
     * 处理当前类型职责中的操作 {@code receive}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param qty 数量值，类型为 {@code BigDecimal}
     * @param finalReceipt 业务处理参数或成员，类型为 {@code boolean}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult receive(String transferNo, BigDecimal qty, boolean finalReceipt, int version) {
        StockTransferMapper.Row current = required(transferNo);
        StockTransferAggregate aggregate = aggregate(current);
        int oldVersion = aggregate.version();
        aggregate.receive(qty, finalReceipt, version);
        stock.inboundForTransfer(aggregate.ownerId(), aggregate.targetWarehouseId(), aggregate.sku(), aggregate.batchNo(), qty, transferNo);
        save(aggregate, oldVersion);
        publish(aggregate.status() == StockTransferAggregate.COMPLETED ? "TransferCompleted" : aggregate.status() == StockTransferAggregate.DIFFERENCE ? "TransferDifferenceRaised" : "TransferPartiallyReceived", aggregate);
        return view(row(aggregate, current.idempotencyKey()), false);
    }

    /** 校验 WMS 收货事实；库存入账延后到上架完成事实。 */
    public TransferResult validateReceiptFact(String transferNo, BigDecimal qty) {
        StockTransferMapper.Row current = required(transferNo);
        StockTransferAggregate aggregate = aggregate(current);
        aggregate.validateReceipt(qty);
        return view(current, false);
    }

    /**
     * 执行命令 {@code confirmDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult confirmDifference(String transferNo, String reason, String responsibleParty, String evidenceRef, int version) {
        return change(transferNo, aggregate -> aggregate.confirmDifference(reason, responsibleParty, evidenceRef, version), "TransferDifferenceConfirmed");
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code TransferResult}
     */
    @Transactional(rollbackFor = Exception.class)
    public TransferResult cancel(String transferNo, int version) {
        StockTransferAggregate aggregate = load(transferNo);
        int previousStatus = aggregate.status();
        int oldVersion = aggregate.version();
        aggregate.cancel(version);
        if (previousStatus == StockTransferAggregate.RESERVED) {
            stock.releaseForTransfer(transferNo);
        }
        save(aggregate, oldVersion);
        publish("TransferCancelled", aggregate);
        return view(row(aggregate, mapper.find(transferNo).idempotencyKey()), false);
    }

    /**
     * 处理当前类型职责中的操作 {@code detail}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferResult}
     */
    public TransferResult detail(String transferNo) {
        return view(required(transferNo), false);
    }

    /**
     * 查询并返回 {@code list}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param limit 业务处理参数或成员，类型为 {@code int}
     * @return 查询并返回的结果，类型为 {@code List<TransferResult>}
     */
    public List<TransferResult> list(int limit) {
        return mapper.list(limit <= 0 ? 50 : Math.min(limit, 200)).stream().map(row -> view(row, false)).toList();
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @param action 业务处理参数或成员，类型为 {@code java.util.function.Consumer<StockTransferAggregate>}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferResult}
     */
    private TransferResult change(String transferNo, java.util.function.Consumer<StockTransferAggregate> action, String eventType) {
        StockTransferMapper.Row current = required(transferNo);
        StockTransferAggregate aggregate = aggregate(current);
        int oldVersion = aggregate.version();
        action.accept(aggregate);
        save(aggregate, oldVersion);
        publish(eventType, aggregate);
        return view(row(aggregate, current.idempotencyKey()), false);
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code StockTransferAggregate}
     * @param oldVersion 乐观锁或契约版本，类型为 {@code int}
     */
    private void save(StockTransferAggregate aggregate, int oldVersion) {
        if (mapper.update(aggregate.id(), aggregate.reservedQty(), aggregate.outboundQty(), aggregate.receivedQty(), aggregate.differenceQty(), aggregate.differenceReason(), aggregate.responsibleParty(), aggregate.evidenceRef(), aggregate.status(), aggregate.version(), oldVersion) != 1) {
            throw new BusinessException(ErrorCode.VERSION_CONFLICT, "调拨单持久化版本冲突");
        }
    }

    /**
     * 查询并返回 {@code load}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code StockTransferAggregate}
     */
    private StockTransferAggregate load(String transferNo) {
        return aggregate(required(transferNo));
    }

    /**
     * 查询并返回 {@code required}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param transferNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code StockTransferMapper.Row}
     */
    private StockTransferMapper.Row required(String transferNo) {
        StockTransferMapper.Row row = mapper.find(transferNo);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "调拨单不存在");
        }
        return row;
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregate 业务处理参数或成员，类型为 {@code StockTransferAggregate}
     */
    private void publish(String type, StockTransferAggregate aggregate) {
        publish(type, aggregate, "");
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregate 业务处理参数或成员，类型为 {@code StockTransferAggregate}
     * @param extra 业务处理参数或成员，类型为 {@code String}
     */
    private void publish(String type, StockTransferAggregate aggregate, String extra) {
        String suffix = extra.isBlank() ? "" : "," + extra;
        events.publish(type, aggregate.transferNo(), "{\"transferNo\":\"" + aggregate.transferNo() + "\",\"ownerId\":" + aggregate.ownerId() + ",\"sourceWarehouseId\":" + aggregate.sourceWarehouseId() + ",\"targetWarehouseId\":" + aggregate.targetWarehouseId() + ",\"sku\":\"" + aggregate.sku() + "\"" + ",\"batchNo\":" + jsonString(aggregate.batchNo()) + ",\"requestedQty\":" + aggregate.requestedQty() + ",\"outboundQty\":" + aggregate.outboundQty() + ",\"receivedQty\":" + aggregate.receivedQty() + ",\"differenceQty\":" + aggregate.differenceQty() + ",\"version\":" + aggregate.version() + ",\"status\":" + aggregate.status() + suffix + "}");
    }

    /**
     * 处理当前类型职责中的操作 {@code jsonString}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private static String jsonString(String value) {
        return value == null ? "null" : "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /**
     * 处理当前类型职责中的操作 {@code aggregate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code StockTransferMapper.Row}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StockTransferAggregate}
     */
    private static StockTransferAggregate aggregate(StockTransferMapper.Row row) {
        return StockTransferAggregate.restore(row.id(), row.transferNo(), row.ownerId(), row.sourceWarehouseId(), row.targetWarehouseId(), row.sku(), row.batchNo(), row.requestedQty(), row.reservedQty(), row.outboundQty(), row.receivedQty(), row.differenceQty(), row.differenceReason(), row.responsibleParty(), row.evidenceRef(), row.status(), row.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code row}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param aggregate 业务处理参数或成员，类型为 {@code StockTransferAggregate}
     * @param idempotencyKey 业务或技术标识，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code StockTransferMapper.Row}
     */
    private static StockTransferMapper.Row row(StockTransferAggregate aggregate, String idempotencyKey) {
        return new StockTransferMapper.Row(aggregate.id(), aggregate.transferNo(), idempotencyKey, aggregate.ownerId(), aggregate.sourceWarehouseId(), aggregate.targetWarehouseId(), aggregate.sku(), aggregate.batchNo(), aggregate.requestedQty(), aggregate.reservedQty(), aggregate.outboundQty(), aggregate.receivedQty(), aggregate.differenceQty(), aggregate.differenceReason(), aggregate.responsibleParty(), aggregate.evidenceRef(), aggregate.status(), aggregate.version());
    }

    /**
     * 处理当前类型职责中的操作 {@code view}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param row 业务处理参数或成员，类型为 {@code StockTransferMapper.Row}
     * @param duplicated 业务处理参数或成员，类型为 {@code boolean}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code TransferResult}
     */
    private static TransferResult view(StockTransferMapper.Row row, boolean duplicated) {
        return new TransferResult(row.transferNo(), row.ownerId(), row.sourceWarehouseId(), row.targetWarehouseId(), row.sku(), row.batchNo(), row.requestedQty(), row.reservedQty(), row.outboundQty(), row.receivedQty(), row.differenceQty(), row.differenceReason(), row.responsibleParty(), row.evidenceRef(), row.status(), row.version(), duplicated);
    }

    /**
     * CreateCommand。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record CreateCommand(long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal qty) {
    }

    /**
     * TransferResult。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record TransferResult(String transferNo, long ownerId, long sourceWarehouseId, long targetWarehouseId, String sku, String batchNo, BigDecimal requestedQty, BigDecimal reservedQty, BigDecimal outboundQty, BigDecimal receivedQty, BigDecimal differenceQty, String differenceReason, String responsibleParty, String evidenceRef, int status, int version, boolean duplicated) {
    }
}
