package com.chaobo.scm.purchase.domain.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PurchaseOrderAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PurchaseOrderAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * orderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String orderNo;

    /**
     * purchaseType（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int purchaseType;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * supplierCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String supplierCode;

    /**
     * supplierName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String supplierName;

    /**
     * purchaseOrgId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrgId;

    /**
     * warehouseCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String warehouseCode;

    /**
     * currency（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String currency;

    /**
     * status（类型：{@code PurchaseOrderStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private PurchaseOrderStatus status;

    /**
     * versionNo（类型：{@code int}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private int versionNo;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * releasedAt（类型：{@code OffsetDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private OffsetDateTime releasedAt;

    /**
     * cancelReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String cancelReason;

    /**
     * lines（类型：{@code List<PurchaseOrderLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<PurchaseOrderLine> lines;

    /**
     * events（类型：{@code List<DomainEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * 创建 PurchaseOrderAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param purchaseType 业务处理参数或成员，类型为 {@code int}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param supplierCode 可追踪业务编码，类型为 {@code String}
     * @param supplierName 业务处理参数或成员，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code PurchaseOrderStatus}
     * @param versionNo 可追踪业务编码，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param releasedAt 业务时间，类型为 {@code OffsetDateTime}
     * @param cancelReason 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<PurchaseOrderLine>}
     */
    public PurchaseOrderAggregate(long id, String orderNo, int purchaseType, long supplierId, String supplierCode, String supplierName, long purchaseOrgId, String warehouseCode, String currency, PurchaseOrderStatus status, int versionNo, int version, OffsetDateTime releasedAt, String cancelReason, List<PurchaseOrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单必须至少包含一行商品");
        }
        this.id = id;
        this.orderNo = orderNo;
        this.purchaseType = purchaseType;
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.purchaseOrgId = purchaseOrgId;
        this.warehouseCode = warehouseCode;
        this.currency = currency;
        this.status = status;
        this.versionNo = versionNo;
        this.version = version;
        this.releasedAt = releasedAt;
        this.cancelReason = cancelReason;
        this.lines = new ArrayList<>(lines);
        assertNoDuplicateSku();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param purchaseType 业务处理参数或成员，类型为 {@code int}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param supplierCode 可追踪业务编码，类型为 {@code String}
     * @param supplierName 业务处理参数或成员，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<PurchaseOrderLine>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code PurchaseOrderAggregate}
     */
    public static PurchaseOrderAggregate create(int purchaseType, long supplierId, String supplierCode, String supplierName, long purchaseOrgId, String warehouseCode, String currency, List<PurchaseOrderLine> lines, IdentifierGenerator ids) {
        var aggregate = new PurchaseOrderAggregate(ids.nextId(), ids.nextCode("PO"), purchaseType, supplierId, supplierCode, supplierName, purchaseOrgId, warehouseCode, currency, PurchaseOrderStatus.DRAFT, 1, 0, null, null, lines);
        aggregate.raise("PurchaseOrderCreated", Map.of());
        return aggregate;
    }

    /**
     * 执行命令 {@code submit}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void submit(IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.REJECTED);
        touch();
        status = PurchaseOrderStatus.SUBMITTED;
        raise("PurchaseOrderSubmitted", Map.of());
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param approved 业务处理参数或成员，类型为 {@code boolean}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void approve(boolean approved, String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUBMITTED);
        touch();
        if (approved) {
            status = PurchaseOrderStatus.APPROVED;
            raise("PurchaseOrderApproved", Map.of());
        } else {
            if (reason == null || reason.isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "驳回原因不能为空");
            }
            status = PurchaseOrderStatus.REJECTED;
            raise("PurchaseOrderRejected", Map.of("reason", reason));
        }
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param publishMode 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void publish(String publishMode, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.APPROVED);
        touch();
        status = PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM;
        releasedAt = OffsetDateTime.now();
        raise("PurchaseOrderPublished", Map.of("publishMode", Objects.requireNonNullElse(publishMode, "EVENT")));
    }

    /**
     * 执行命令 {@code cancel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void cancel(String reason, IdentifierGenerator ids) {
        if (receivedQuantity().signum() > 0) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "采购订单已有入库执行，不能直接取消");
        }
        if (status == PurchaseOrderStatus.CANCELLED || status == PurchaseOrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前采购订单状态不能取消");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "取消原因不能为空");
        }
        touch();
        status = PurchaseOrderStatus.CANCELLED;
        cancelReason = reason;
        raise("PurchaseOrderCancelled", Map.of("reason", reason));
    }

    /**
     * 执行命令 {@code closeRemaining}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void closeRemaining(String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUPPLIER_CONFIRMED, PurchaseOrderStatus.PARTIALLY_INBOUNDED, PurchaseOrderStatus.SUPPLIER_DIFF, PurchaseOrderStatus.SUPPLIER_REJECTED);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "关闭剩余数量原因不能为空");
        }
        touch();
        versionNo++;
        status = PurchaseOrderStatus.CLOSED;
        raise("PurchaseOrderClosed", Map.of("reason", reason));
    }

    /**
     * 供应商确认是采购订单发布后的外部业务事实。事件先由 Inbox 幂等保护，
     * 再调用本方法推进订单，避免供应商门户直接改写采购订单数据。
     */
    public void recordSupplierConfirmation(String remark, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        touch();
        status = PurchaseOrderStatus.SUPPLIER_CONFIRMED;
        raise("SupplierOrderConfirmationRecorded", optional("remark", remark));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordSupplierRejection}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordSupplierRejection(String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商拒绝原因不能为空");
        }
        touch();
        status = PurchaseOrderStatus.SUPPLIER_REJECTED;
        raise("SupplierOrderRejectionRecorded", Map.of("reason", reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code recordSupplierDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void recordSupplierDifference(String reason, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM);
        touch();
        status = PurchaseOrderStatus.SUPPLIER_DIFF;
        raise("SupplierOrderDifferenceRecorded", optional("reason", reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code acceptSupplierDifference}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param comment 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void acceptSupplierDifference(String comment, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUPPLIER_DIFF);
        touch();
        status = PurchaseOrderStatus.SUPPLIER_CONFIRMED;
        raise("SupplierOrderDifferenceAccepted", optional("comment", comment));
    }

    /**
     * 处理当前类型职责中的操作 {@code restartSupplierNegotiation}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param requirement 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void restartSupplierNegotiation(String requirement, IdentifierGenerator ids) {
        ensureStatus(PurchaseOrderStatus.SUPPLIER_DIFF, PurchaseOrderStatus.SUPPLIER_REJECTED);
        if (requirement == null || requirement.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "重新协商要求不能为空");
        }
        touch();
        status = PurchaseOrderStatus.PENDING_SUPPLIER_CONFIRM;
        raise("SupplierOrderRenegotiationRequested", Map.of("requirement", requirement));
    }

    /**
     * 执行命令 {@code applyLineQtyChanges}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param lineQtyChanges 数量值，类型为 {@code Map<Long,BigDecimal>}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void applyLineQtyChanges(Map<Long, BigDecimal> lineQtyChanges, IdentifierGenerator ids) {
        if (status == PurchaseOrderStatus.COMPLETED || status == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "已完成或已取消订单不能变更");
        }
        if (lineQtyChanges == null || lineQtyChanges.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "变更行不能为空");
        }
        for (Map.Entry<Long, BigDecimal> entry : lineQtyChanges.entrySet()) {
            line(entry.getKey()).changeQty(entry.getValue());
        }
        touch();
        versionNo++;
        raise("PurchaseOrderChangeEffective", Map.of("changeType", "QTY"));
    }

    /**
     * 转换数据模型 {@code totalAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal totalAmount() {
        return lines.stream().map(PurchaseOrderLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 处理当前类型职责中的操作 {@code taxAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal taxAmount() {
        return lines.stream().map(PurchaseOrderLine::taxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 处理当前类型职责中的操作 {@code taxIncludedAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal taxIncludedAmount() {
        return totalAmount().add(taxAmount());
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<DomainEvent>}
     */
    public List<DomainEvent> pullEvents() {
        var pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    /**
     * 处理当前类型职责中的操作 {@code line}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderLine}
     */
    private PurchaseOrderLine line(long lineId) {
        return lines.stream().filter(line -> line.lineId() == lineId).findFirst().orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "采购订单行不存在"));
    }

    /**
     * 处理当前类型职责中的操作 {@code receivedQuantity}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private BigDecimal receivedQuantity() {
        return lines.stream().map(PurchaseOrderLine::receivedQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 校验业务约束 {@code ensureStatus}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param allowed 业务处理参数或成员，类型为 {@code PurchaseOrderStatus}
     */
    private void ensureStatus(PurchaseOrderStatus... allowed) {
        for (PurchaseOrderStatus candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw new BusinessException(ErrorCode.STATE_CONFLICT, "当前采购订单状态不允许执行该操作");
    }

    /**
     * 转换数据模型 {@code touch}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void touch() {
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param extra 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("orderId", id);
        payload.put("orderNo", orderNo);
        payload.put("supplierId", supplierId);
        payload.put("supplierCode", supplierCode);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("warehouseCode", Objects.requireNonNullElse(warehouseCode, ""));
        payload.put("currency", currency);
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "PURCHASE_ORDER", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code optional}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param key 业务处理参数或成员，类型为 {@code String}
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Map<String,Object>}
     */
    private static Map<String, Object> optional(String key, String value) {
        return value == null || value.isBlank() ? Map.of() : Map.of(key, value);
    }

    /**
     * 处理当前类型职责中的操作 {@code assertNoDuplicateSku}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void assertNoDuplicateSku() {
        var keys = new java.util.HashSet<String>();
        for (PurchaseOrderLine line : lines) {
            if (!keys.add(line.skuCode())) {
                throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购订单行SKU不能重复");
            }
        }
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
     * 处理当前类型职责中的操作 {@code orderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String orderNo() {
        return orderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int purchaseType() {
        return purchaseType;
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
     * 处理当前类型职责中的操作 {@code supplierCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String supplierCode() {
        return supplierCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String supplierName() {
        return supplierName;
    }

    /**
     * 处理当前类型职责中的操作 {@code purchaseOrgId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long purchaseOrgId() {
        return purchaseOrgId;
    }

    /**
     * 处理当前类型职责中的操作 {@code warehouseCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String warehouseCode() {
        return warehouseCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code currency}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String currency() {
        return currency;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchaseOrderStatus}
     */
    public PurchaseOrderStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code versionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int versionNo() {
        return versionNo;
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
     * 执行命令 {@code releasedAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code OffsetDateTime}
     */
    public OffsetDateTime releasedAt() {
        return releasedAt;
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
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<PurchaseOrderLine>}
     */
    public List<PurchaseOrderLine> lines() {
        return List.copyOf(lines);
    }
}
