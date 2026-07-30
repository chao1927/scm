package com.chaobo.scm.supplier.domain.asn;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.supplier.domain.shared.DomainEvent;
import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AsnAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class AsnAggregate {

    /**
     * asnId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long asnId;

    /**
     * asnNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String asnNo;

    /**
     * purchaseOrderId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrderId;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * warehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long warehouseId;

    /**
     * lines（类型：{@code List<AsnLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<AsnLine> lines;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * estimatedArrivalAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime estimatedArrivalAt;

    /**
     * shipmentInfo（类型：{@code ShipmentInfo}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private ShipmentInfo shipmentInfo;

    /**
     * status（类型：{@code AsnStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private AsnStatus status;

    /**
     * cancelReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String cancelReason;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 AsnAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrderId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param estimatedArrivalAt 业务时间，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<AsnLine>}
     * @param status 生命周期状态，类型为 {@code AsnStatus}
     * @param shipmentInfo 业务处理参数或成员，类型为 {@code ShipmentInfo}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private AsnAggregate(long asnId, String asnNo, long purchaseOrderId, long supplierId, long warehouseId, OffsetDateTime estimatedArrivalAt, List<AsnLine> lines, AsnStatus status, ShipmentInfo shipmentInfo, String cancelReason, int version) {
        this.asnId = asnId;
        this.asnNo = asnNo;
        this.purchaseOrderId = purchaseOrderId;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.estimatedArrivalAt = estimatedArrivalAt;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.shipmentInfo = shipmentInfo;
        this.cancelReason = cancelReason;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseOrderId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param estimatedArrivalAt 业务时间，类型为 {@code OffsetDateTime}
     * @param newLines 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code AsnAggregate}
     */
    public static AsnAggregate create(long purchaseOrderId, long supplierId, long warehouseId, OffsetDateTime estimatedArrivalAt, List<NewLine> newLines, long operatorId, IdentifierGenerator generator) {
        if (purchaseOrderId <= 0 || supplierId <= 0 || warehouseId <= 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购订单、供应商和目的仓不能为空");
        }
        if (estimatedArrivalAt == null || !estimatedArrivalAt.isAfter(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "预计到仓时间必须晚于当前时间");
        }
        if (newLines == null || newLines.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "ASN 至少包含一行商品");
        }
        long id = generator.nextId();
        List<AsnLine> lines = newLines.stream().map(line -> new AsnLine(generator.nextId(), line.skuCode(), line.plannedQuantity(), BigDecimal.ZERO, line.batchNo(), line.productionDate(), line.expireDate())).toList();
        AsnAggregate aggregate = new AsnAggregate(id, generator.nextBusinessNo("ASN"), purchaseOrderId, supplierId, warehouseId, estimatedArrivalAt, lines, AsnStatus.DRAFT, null, null, 0);
        aggregate.raise(generator, "SupplierAsnCreated", "ASN已创建", operatorId, Map.of("purchaseOrderId", purchaseOrderId, "supplierId", supplierId, "warehouseId", warehouseId, "plannedQuantity", aggregate.totalPlannedQuantity()));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param asnId 业务或技术标识，类型为 {@code long}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrderId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param estimatedArrivalAt 业务时间，类型为 {@code OffsetDateTime}
     * @param lines 业务处理参数或成员，类型为 {@code List<AsnLine>}
     * @param status 生命周期状态，类型为 {@code AsnStatus}
     * @param shipmentInfo 业务处理参数或成员，类型为 {@code ShipmentInfo}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AsnAggregate}
     */
    public static AsnAggregate rehydrate(long asnId, String asnNo, long purchaseOrderId, long supplierId, long warehouseId, OffsetDateTime estimatedArrivalAt, List<AsnLine> lines, AsnStatus status, ShipmentInfo shipmentInfo, String cancelReason, int version) {
        return new AsnAggregate(asnId, asnNo, purchaseOrderId, supplierId, warehouseId, estimatedArrivalAt, lines, status, shipmentInfo, cancelReason, version);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void submit(long operatorId, IdentifierGenerator generator) {
        requireStatus(AsnStatus.DRAFT);
        status = AsnStatus.SUBMITTED;
        version++;
        raise(generator, "SupplierAsnSubmitted", "ASN已提交", operatorId, Map.of("supplierId", supplierId, "warehouseId", warehouseId, "estimatedArrivalAt", estimatedArrivalAt.toString(), "plannedQuantity", totalPlannedQuantity()));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordAppointment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appointmentNo 可追踪业务编码，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void recordAppointment(String appointmentNo, long operatorId, IdentifierGenerator generator) {
        requireStatus(AsnStatus.SUBMITTED);
        if (appointmentNo == null || appointmentNo.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "预约单号不能为空");
        }
        status = AsnStatus.APPOINTED;
        version++;
        raise(generator, "SupplierAsnAppointmentConfirmed", "ASN预约已确认", operatorId, Map.of("appointmentNo", appointmentNo, "supplierId", supplierId, "warehouseId", warehouseId));
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void cancel(String reason, long operatorId, IdentifierGenerator generator) {
        if (!List.of(AsnStatus.DRAFT, AsnStatus.SUBMITTED, AsnStatus.APPOINTED, AsnStatus.SHIPPED).contains(status)) {
            throw stateConflict("当前状态不允许取消 ASN");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "取消原因不能为空");
        }
        status = AsnStatus.CANCELLED;
        cancelReason = reason.trim();
        version++;
        raise(generator, "SupplierAsnCancelled", "ASN已取消", operatorId, Map.of("cancelReason", cancelReason, "previousTransportStarted", shipmentInfo != null));
    }

    /**
     * 执行命令 {@code confirmShipment}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param shipment 业务处理参数或成员，类型为 {@code ShipmentInfo}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void confirmShipment(ShipmentInfo shipment, long operatorId, IdentifierGenerator generator) {
        if (!List.of(AsnStatus.SUBMITTED, AsnStatus.APPOINTED).contains(status)) {
            throw stateConflict("只有已提交或已预约 ASN 可以确认发货");
        }
        shipmentInfo = shipment;
        status = AsnStatus.SHIPPED;
        version++;
        raise(generator, "SupplierAsnShipped", "ASN已发货", operatorId, Map.of("supplierId", supplierId, "warehouseId", warehouseId, "carrierName", shipment.carrierName(), "trackingNo", shipment.trackingNo() == null ? "" : shipment.trackingNo(), "shippedAt", shipment.shippedAt().toString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordArrival}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param arrivedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void recordArrival(OffsetDateTime arrivedAt, long operatorId, IdentifierGenerator generator) {
        if (status != AsnStatus.SHIPPED) {
            throw stateConflict("只有已发货 ASN 可以登记到仓");
        }
        if (arrivedAt == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "到仓时间不能为空");
        }
        status = AsnStatus.ARRIVED;
        version++;
        raise(generator, "SupplierAsnArrived", "ASN已到仓", operatorId, Map.of("arrivedAt", arrivedAt.toString(), "supplierId", supplierId));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordReceipt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param receivedQuantity 数量值，类型为 {@code java.math.BigDecimal}
     * @param rejectedQuantity 数量值，类型为 {@code java.math.BigDecimal}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     */
    public void recordReceipt(java.math.BigDecimal receivedQuantity, java.math.BigDecimal rejectedQuantity, long operatorId, IdentifierGenerator generator) {
        if (status != AsnStatus.ARRIVED) {
            throw stateConflict("只有已到仓 ASN 可以登记收货");
        }
        if (receivedQuantity == null || receivedQuantity.signum() < 0 || rejectedQuantity == null || rejectedQuantity.signum() < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "收货数量不合法");
        }
        if (receivedQuantity.add(rejectedQuantity).compareTo(totalPlannedQuantity()) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "收货和拒收数量不能超过通知数量");
        }
        status = AsnStatus.RECEIVED;
        version++;
        raise(generator, "SupplierAsnReceived", "ASN已收货", operatorId, Map.of("receivedQuantity", receivedQuantity, "rejectedQuantity", rejectedQuantity, "supplierId", supplierId));
    }

    /**
     * 查询并返回 {@code requireStatus}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code AsnStatus}
     */
    private void requireStatus(AsnStatus expected) {
        if (status != expected) {
            throw stateConflict("ASN 状态必须为" + expected.label() + "，当前为" + status.label());
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code stateConflict}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param message 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private BusinessException stateConflict(String message) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, message);
    }

    /**
     * 转换数据模型 {@code totalPlannedQuantity}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 转换数据模型的结果，类型为 {@code BigDecimal}
     */
    private BigDecimal totalPlannedQuantity() {
        return lines.stream().map(AsnLine::plannedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param generator 业务处理参数或成员，类型为 {@code IdentifierGenerator}
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param eventName 业务处理参数或成员，类型为 {@code String}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     * @param payload 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(IdentifierGenerator generator, String eventType, String eventName, long operatorId, Map<String, Object> payload) {
        long eventId = generator.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, eventType, eventName, "ASN", asnId, asnNo, version, operatorId, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code asnId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long asnId() {
        return asnId;
    }

    /**
     * 处理当前类型职责中的操作 {@code asnNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String asnNo() {
        return asnNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseOrderId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long purchaseOrderId() {
        return purchaseOrderId;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long supplierId() {
        return supplierId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long warehouseId() {
        return warehouseId;
    }

    /**
     * 处理当前类型职责中的操作 {@code estimatedArrivalAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime estimatedArrivalAt() {
        return estimatedArrivalAt;
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<AsnLine>}
     */
    public List<AsnLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * 处理当前类型职责中的操作 {@code shipmentInfo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ShipmentInfo}
     */
    public ShipmentInfo shipmentInfo() {
        return shipmentInfo;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AsnStatus}
     */
    public AsnStatus status() {
        return status;
    }

    /**
     * 执行命令 {@code cancelReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String cancelReason() {
        return cancelReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }

    /**
     * NewLine。
     *
     * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record NewLine(String skuCode, BigDecimal plannedQuantity, String batchNo, java.time.LocalDate productionDate, java.time.LocalDate expireDate) {
    }
}
