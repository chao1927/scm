package com.chaobo.scm.supplier.domain.returning;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * SupplierReturnAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierReturnAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * no（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String no;

    /**
     * supplierId、warehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId, warehouseId;

    /**
     * qualityIssueId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long qualityIssueId;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String reason;

    /**
     * lines（类型：{@code List<SupplierReturnLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<SupplierReturnLine> lines;

    /**
     * status（类型：{@code SupplierReturnStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private SupplierReturnStatus status;

    /**
     * inventoryLockNo、outboundNo、shipmentId、waybillNo、carrierCode、settlementRef、exceptionReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private String inventoryLockNo, outboundNo, shipmentId, waybillNo, carrierCode, settlementRef, exceptionReason;

    /**
     * supplierConfirmedAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime supplierConfirmedAt;

    /**
     * settlementCompleted（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private boolean settlementCompleted;

    /**
     * offsetAmount、claimAmount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal offsetAmount, claimAmount;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 SupplierReturnAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param qualityIssueId 业务或技术标识，类型为 {@code Long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<SupplierReturnLine>}
     * @param status 生命周期状态，类型为 {@code SupplierReturnStatus}
     * @param inventoryLockNo 可追踪业务编码，类型为 {@code String}
     * @param supplierConfirmedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param shipmentId 业务或技术标识，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param settlementCompleted 业务处理参数或成员，类型为 {@code boolean}
     * @param settlementRef 业务处理参数或成员，类型为 {@code String}
     * @param offsetAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param claimAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param exceptionReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierReturnAggregate(long id, String no, long supplierId, long warehouseId, Long qualityIssueId, String reason, List<SupplierReturnLine> lines, SupplierReturnStatus status, String inventoryLockNo, OffsetDateTime supplierConfirmedAt, String outboundNo, String shipmentId, String waybillNo, String carrierCode, boolean settlementCompleted, String settlementRef, BigDecimal offsetAmount, BigDecimal claimAmount, String exceptionReason, int version) {
        this.id = id;
        this.no = no;
        this.supplierId = supplierId;
        this.warehouseId = warehouseId;
        this.qualityIssueId = qualityIssueId;
        this.reason = reason;
        this.lines = new ArrayList<>(lines);
        this.status = status;
        this.inventoryLockNo = inventoryLockNo;
        this.supplierConfirmedAt = supplierConfirmedAt;
        this.outboundNo = outboundNo;
        this.shipmentId = shipmentId;
        this.waybillNo = waybillNo;
        this.carrierCode = carrierCode;
        this.settlementCompleted = settlementCompleted;
        this.settlementRef = settlementRef;
        this.offsetAmount = nvl(offsetAmount);
        this.claimAmount = nvl(claimAmount);
        this.exceptionReason = exceptionReason;
        this.version = version;
        validate();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param qualityIssueId 业务或技术标识，类型为 {@code Long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<NewLine>}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierReturnAggregate}
     */
    public static SupplierReturnAggregate create(long supplierId, long warehouseId, Long qualityIssueId, String reason, List<NewLine> lines, long operator, IdentifierGenerator ids) {
        if (lines == null || lines.isEmpty()) {
            throw rule("退供明细不能为空");
        }
        var entities = lines.stream().map(l -> new SupplierReturnLine(ids.nextId(), l.skuCode(), l.batchNo(), l.inventoryStatus(), l.requestedQty(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)).toList();
        var a = new SupplierReturnAggregate(ids.nextId(), ids.nextBusinessNo("SR"), supplierId, warehouseId, qualityIssueId, reason, entities, SupplierReturnStatus.DRAFT, null, null, null, null, null, null, false, null, BigDecimal.ZERO, BigDecimal.ZERO, null, 0);
        a.raise(ids, "SupplierReturnCreated", "退供单已创建", operator);
        return a;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param warehouseId 业务或技术标识，类型为 {@code long}
     * @param qualityIssueId 业务或技术标识，类型为 {@code Long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<SupplierReturnLine>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param inventoryLockNo 可追踪业务编码，类型为 {@code String}
     * @param supplierConfirmedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param shipmentId 业务或技术标识，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param settlementCompleted 业务处理参数或成员，类型为 {@code boolean}
     * @param settlementRef 业务处理参数或成员，类型为 {@code String}
     * @param offsetAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param claimAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param exceptionReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnAggregate}
     */
    public static SupplierReturnAggregate rehydrate(long id, String no, long supplierId, long warehouseId, Long qualityIssueId, String reason, List<SupplierReturnLine> lines, int status, String inventoryLockNo, OffsetDateTime supplierConfirmedAt, String outboundNo, String shipmentId, String waybillNo, String carrierCode, boolean settlementCompleted, String settlementRef, BigDecimal offsetAmount, BigDecimal claimAmount, String exceptionReason, int version) {
        return new SupplierReturnAggregate(id, no, supplierId, warehouseId, qualityIssueId, reason, lines, SupplierReturnStatus.fromCode(status), inventoryLockNo, supplierConfirmedAt, outboundNo, shipmentId, waybillNo, carrierCode, settlementCompleted, settlementRef, offsetAmount, claimAmount, exceptionReason, version);
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.DRAFT);
        move(SupplierReturnStatus.PENDING_REVIEW, ids, "SupplierReturnSubmitted", "退供单已提交", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code review}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approved 业务处理参数或成员，类型为 {@code boolean}
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void review(boolean approved, String comment, long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.PENDING_REVIEW);
        if (!approved) {
            exceptionReason = required(comment, "驳回原因");
            move(SupplierReturnStatus.DRAFT, ids, "SupplierReturnRejected", "退供单已驳回", operator);
            return;
        }
        move(SupplierReturnStatus.APPROVED, ids, "SupplierReturnApproved", "退供单已审核", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code requestInventoryLock}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void requestInventoryLock(long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.APPROVED);
        move(SupplierReturnStatus.INVENTORY_LOCKING, ids, "SupplierReturnInventoryLockRequested", "退供库存锁定已请求", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordInventoryLock}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param success 业务处理参数或成员，类型为 {@code boolean}
     * @param lockNo 可追踪业务编码，类型为 {@code String}
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param failureReason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordInventoryLock(boolean success, String lockNo, Map<Long, BigDecimal> quantities, String failureReason, long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.INVENTORY_LOCKING);
        if (!success) {
            exceptionReason = required(failureReason, "锁定失败原因");
            move(SupplierReturnStatus.APPROVED, ids, "SupplierReturnInventoryLockFailed", "退供库存锁定失败", operator);
            return;
        }
        inventoryLockNo = required(lockNo, "库存锁定号");
        for (var line : lines) {
            line.lock(requiredQty(quantities, line.id(), "锁定"));
        }
        exceptionReason = null;
        move(SupplierReturnStatus.PENDING_SUPPLIER_CONFIRMATION, ids, "SupplierReturnInventoryLocked", "退供库存已锁定", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierConfirm}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param hasDifference 业务处理参数或成员，类型为 {@code boolean}
     * @param differenceReason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void supplierConfirm(boolean hasDifference, String differenceReason, long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.PENDING_SUPPLIER_CONFIRMATION);
        supplierConfirmedAt = OffsetDateTime.now();
        if (hasDifference) {
            exceptionReason = required(differenceReason, "差异原因");
            move(SupplierReturnStatus.SUPPLIER_DIFFERENCE, ids, "SupplierReturnDifferenceReported", "供应商已反馈退供差异", operator);
        } else {
            exceptionReason = null;
            move(SupplierReturnStatus.PENDING_OUTBOUND, ids, "SupplierReturnConfirmed", "供应商已确认退供", operator);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code resolveDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void resolveDifference(long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.SUPPLIER_DIFFERENCE);
        exceptionReason = null;
        move(SupplierReturnStatus.PENDING_OUTBOUND, ids, "SupplierReturnDifferenceResolved", "退供差异已解决", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordOutbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param outboundNo 可追踪业务编码，类型为 {@code String}
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordOutbound(String outboundNo, Map<Long, BigDecimal> quantities, long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.PENDING_OUTBOUND);
        this.outboundNo = required(outboundNo, "出库单号");
        for (var line : lines) {
            line.outbound(requiredQty(quantities, line.id(), "出库"));
        }
        move(SupplierReturnStatus.OUTBOUNDED, ids, "SupplierReturnOutboundCompleted", "退供已出库", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param shipmentId 业务或技术标识，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordWaybill(String shipmentId, String waybillNo, String carrierCode, long operator, IdentifierGenerator ids) {
        if (status != SupplierReturnStatus.OUTBOUNDED && status != SupplierReturnStatus.IN_TRANSIT) {
            throw state("当前状态不能记录运单");
        }
        this.shipmentId = required(shipmentId, "运输任务号");
        this.waybillNo = required(waybillNo, "运单号");
        this.carrierCode = required(carrierCode, "物流商编码");
        move(SupplierReturnStatus.IN_TRANSIT, ids, "SupplierReturnTransportRecorded", "退供运单已记录", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordSigned}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param quantities 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param differenceReason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordSigned(Map<Long, BigDecimal> quantities, String differenceReason, long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.IN_TRANSIT);
        boolean different = false;
        for (var line : lines) {
            var qty = requiredQty(quantities, line.id(), "签收");
            line.sign(qty);
            different |= qty.compareTo(line.outboundQty()) != 0;
        }
        if (different) {
            exceptionReason = required(differenceReason, "签收差异原因");
            move(SupplierReturnStatus.SUPPLIER_DIFFERENCE, ids, "SupplierReturnReceiptDifferenceReported", "退供签收存在差异", operator);
        } else {
            move(SupplierReturnStatus.SIGNED, ids, "SupplierReturnSigned", "退供已签收", operator);
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code recordTransportException}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordTransportException(String reason, long operator, IdentifierGenerator ids) {
        if (status != SupplierReturnStatus.OUTBOUNDED && status != SupplierReturnStatus.IN_TRANSIT) {
            throw state("当前状态不能记录运输异常");
        }
        exceptionReason = required(reason, "运输异常原因");
        move(SupplierReturnStatus.SUPPLIER_DIFFERENCE, ids, "SupplierReturnTransportExceptionRecorded", "退供运输异常已记录", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code recordSettlement}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ref 业务处理参数或成员，类型为 {@code String}
     * @param offset 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param claim 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordSettlement(String ref, BigDecimal offset, BigDecimal claim, long operator, IdentifierGenerator ids) {
        if (status != SupplierReturnStatus.SIGNED && status != SupplierReturnStatus.SUPPLIER_DIFFERENCE) {
            throw state("只有签收后才能记录结算");
        }
        settlementRef = required(ref, "结算引用号");
        offsetAmount = nonNegative(offset);
        claimAmount = nonNegative(claim);
        settlementCompleted = true;
        version++;
        raise(ids, "SupplierReturnSettlementCompleted", "退供结算已完成", operator);
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void close(long operator, IdentifierGenerator ids) {
        require(SupplierReturnStatus.SIGNED);
        if (!settlementCompleted) {
            throw rule("退供结算未完成，不能关闭");
        }
        move(SupplierReturnStatus.CLOSED, ids, "SupplierReturnClosed", "退供单已关闭", operator);
    }

    /**
     * 处理当前类型职责中的操作 {@code move}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param target 业务处理参数或成员，类型为 {@code SupplierReturnStatus}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    private void move(SupplierReturnStatus target, IdentifierGenerator ids, String type, String name, long operator) {
        status = target;
        version++;
        raise(ids, type, name, operator);
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code SupplierReturnStatus}
     */
    private void require(SupplierReturnStatus expected) {
        if (status != expected) {
            throw state("当前状态不允许该操作");
        }
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validate() {
        if (id <= 0 || supplierId <= 0 || warehouseId <= 0 || reason == null || reason.isBlank() || lines.isEmpty()) {
            throw rule("退供单信息不完整");
        }
        if (lines.stream().map(SupplierReturnLine::id).distinct().count() != lines.size()) {
            throw rule("退供明细重复");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     */
    private void raise(IdentifierGenerator ids, String type, String name, long operator) {
        long eventId = ids.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, type, name, "SUPPLIER_RETURN", id, no, version, operator, OffsetDateTime.now(), Map.of("supplierId", supplierId, "warehouseId", warehouseId, "status", status.code())));
    }

    /**
     * 查询并返回 {@code required}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param v 业务处理参数或成员，类型为 {@code String}
     * @param n 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code String}
     */
    private static String required(String v, String n) {
        if (v == null || v.isBlank()) {
            throw rule(n + "不能为空");
        }
        return v.trim();
    }

    /**
     * 查询并返回 {@code requiredQty}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param values 业务处理参数或成员，类型为 {@code Map<Long,BigDecimal>}
     * @param id 业务或技术标识，类型为 {@code long}
     * @param n 业务处理参数或成员，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal requiredQty(Map<Long, BigDecimal> values, long id, String n) {
        if (values == null || !values.containsKey(id)) {
            throw rule(n + "数量缺少明细: " + id);
        }
        return values.get(id);
    }

    /**
     * 处理当前类型职责中的操作 {@code nvl}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 处理当前类型职责中的操作 {@code nonNegative}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal nonNegative(BigDecimal v) {
        v = nvl(v);
        if (v.signum() < 0) {
            throw rule("金额不能小于0");
        }
        return v;
    }

    /**
     * 处理当前类型职责中的操作 {@code rule}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param m 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException rule(String m) {
        return new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, m);
    }

    /**
     * 处理当前类型职责中的操作 {@code state}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param m 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BusinessException}
     */
    private static BusinessException state(String m) {
        return new BusinessException(ErrorCode.STATE_CONFLICT, m);
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code no}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String no() {
        return no;
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
     * 处理当前类型职责中的操作 {@code qualityIssueId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long qualityIssueId() {
        return qualityIssueId;
    }

    /**
     * 处理当前类型职责中的操作 {@code reason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reason() {
        return reason;
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<SupplierReturnLine>}
     */
    public List<SupplierReturnLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierReturnStatus}
     */
    public SupplierReturnStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code inventoryLockNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String inventoryLockNo() {
        return inventoryLockNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierConfirmedAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime supplierConfirmedAt() {
        return supplierConfirmedAt;
    }

    /**
     * 处理当前类型职责中的操作 {@code outboundNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String outboundNo() {
        return outboundNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code shipmentId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String shipmentId() {
        return shipmentId;
    }

    /**
     * 处理当前类型职责中的操作 {@code waybillNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String waybillNo() {
        return waybillNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code carrierCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierCode() {
        return carrierCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code settlementCompleted}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean settlementCompleted() {
        return settlementCompleted;
    }

    /**
     * 处理当前类型职责中的操作 {@code settlementRef}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String settlementRef() {
        return settlementRef;
    }

    /**
     * 处理当前类型职责中的操作 {@code offsetAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal offsetAmount() {
        return offsetAmount;
    }

    /**
     * 处理当前类型职责中的操作 {@code claimAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal claimAmount() {
        return claimAmount;
    }

    /**
     * 处理当前类型职责中的操作 {@code exceptionReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String exceptionReason() {
        return exceptionReason;
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
    public record NewLine(String skuCode, String batchNo, String inventoryStatus, BigDecimal requestedQty) {
    }
}
