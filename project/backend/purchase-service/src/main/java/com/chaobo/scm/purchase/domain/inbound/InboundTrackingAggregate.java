package com.chaobo.scm.purchase.domain.inbound;

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
 * InboundTrackingAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class InboundTrackingAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * inboundNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String inboundNo;

    /**
     * orderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String orderNo;

    /**
     * asnNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String asnNo;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

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
     * skuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode;

    /**
     * notifiedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal notifiedQty;

    /**
     * receivedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal receivedQty;

    /**
     * qualifiedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal qualifiedQty;

    /**
     * unqualifiedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal unqualifiedQty;

    /**
     * putawayQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal putawayQty;

    /**
     * status（类型：{@code InboundStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private InboundStatus status;

    /**
     * exceptionReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String exceptionReason;

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
     * 创建 InboundTrackingAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param inboundNo 可追踪业务编码，类型为 {@code String}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param notifiedQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param qualifiedQty 数量值，类型为 {@code BigDecimal}
     * @param unqualifiedQty 数量值，类型为 {@code BigDecimal}
     * @param putawayQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code InboundStatus}
     * @param exceptionReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public InboundTrackingAggregate(long id, String inboundNo, String orderNo, String asnNo, long supplierId, long purchaseOrgId, String warehouseCode, String skuCode, BigDecimal notifiedQty, BigDecimal receivedQty, BigDecimal qualifiedQty, BigDecimal unqualifiedQty, BigDecimal putawayQty, InboundStatus status, String exceptionReason, int version) {
        if (orderNo == null || orderNo.isBlank() || asnNo == null || asnNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "PO和ASN不能为空");
        }
        if (notifiedQty == null || notifiedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "通知数量必须大于0");
        }
        this.id = id;
        this.inboundNo = inboundNo;
        this.orderNo = orderNo;
        this.asnNo = asnNo;
        this.supplierId = supplierId;
        this.purchaseOrgId = purchaseOrgId;
        this.warehouseCode = warehouseCode;
        this.skuCode = skuCode;
        this.notifiedQty = notifiedQty;
        this.receivedQty = zero(receivedQty);
        this.qualifiedQty = zero(qualifiedQty);
        this.unqualifiedQty = zero(unqualifiedQty);
        this.putawayQty = zero(putawayQty);
        this.status = status;
        this.exceptionReason = exceptionReason;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code recordAsn}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param asnNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param warehouseCode 可追踪业务编码，类型为 {@code String}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param notifiedQty 数量值，类型为 {@code BigDecimal}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundTrackingAggregate}
     */
    public static InboundTrackingAggregate recordAsn(String orderNo, String asnNo, long supplierId, long purchaseOrgId, String warehouseCode, String skuCode, BigDecimal notifiedQty, IdentifierGenerator ids) {
        var aggregate = new InboundTrackingAggregate(ids.nextId(), ids.nextCode("INB"), orderNo, asnNo, supplierId, purchaseOrgId, warehouseCode, skuCode, notifiedQty, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, InboundStatus.ASN_RECORDED, null, 0);
        aggregate.raise("PurchaseAsnRecorded", Map.of());
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code syncWms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param qualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param unqualified 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param putaway 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void syncWms(BigDecimal received, BigDecimal qualified, BigDecimal unqualified, BigDecimal putaway, String reason, IdentifierGenerator ids) {
        requireNonNegative(received, "收货数量");
        requireNonNegative(qualified, "合格数量");
        requireNonNegative(unqualified, "不合格数量");
        requireNonNegative(putaway, "上架数量");
        if (received.compareTo(notifiedQty) > 0) {
            status = InboundStatus.EXCEPTION;
            exceptionReason = "WMS数量超过ASN通知数量";
            raise("PurchaseInboundExceptionRaised", Map.of("reason", exceptionReason));
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, exceptionReason);
        }
        version++;
        this.receivedQty = received;
        this.qualifiedQty = qualified;
        this.unqualifiedQty = unqualified;
        this.putawayQty = putaway;
        if (putaway.signum() > 0) {
            status = InboundStatus.PUTAWAY;
            raise("PurchaseGoodsPutawayCompleted", Map.of("syncReason", Objects.requireNonNullElse(reason, "")));
        } else if (qualified.add(unqualified).signum() > 0) {
            status = InboundStatus.INSPECTED;
            raise("PurchaseInspectionCompleted", Map.of("syncReason", Objects.requireNonNullElse(reason, "")));
        } else if (received.signum() > 0) {
            status = InboundStatus.RECEIVED;
            raise("PurchaseGoodsReceived", Map.of("syncReason", Objects.requireNonNullElse(reason, "")));
        }
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
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param eventType 业务处理参数或成员，类型为 {@code String}
     * @param extra 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(String eventType, Map<String, Object> extra) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("inboundId", id);
        payload.put("inboundNo", inboundNo);
        payload.put("orderNo", orderNo);
        payload.put("asnNo", asnNo);
        payload.put("supplierId", supplierId);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("warehouseCode", Objects.requireNonNullElse(warehouseCode, ""));
        payload.put("skuCode", Objects.requireNonNullElse(skuCode, ""));
        payload.put("status", status.code());
        payload.put("version", version);
        payload.putAll(extra);
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "INBOUND_TRACKING", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code zero}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 查询并返回 {@code requireNonNegative}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param name 业务处理参数或成员，类型为 {@code String}
     */
    private static void requireNonNegative(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, name + "不能小于0");
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
     * 处理当前类型职责中的操作 {@code inboundNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String inboundNo() {
        return inboundNo;
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
     * 处理当前类型职责中的操作 {@code asnNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String asnNo() {
        return asnNo;
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
     * 处理当前类型职责中的操作 {@code skuCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String skuCode() {
        return skuCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code notifiedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal notifiedQty() {
        return notifiedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code receivedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal receivedQty() {
        return receivedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code qualifiedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal qualifiedQty() {
        return qualifiedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code unqualifiedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal unqualifiedQty() {
        return unqualifiedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code putawayQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal putawayQty() {
        return putawayQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code InboundStatus}
     */
    public InboundStatus status() {
        return status;
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
}
