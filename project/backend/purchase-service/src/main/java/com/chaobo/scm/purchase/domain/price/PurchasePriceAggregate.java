package com.chaobo.scm.purchase.domain.price;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import com.chaobo.scm.purchase.domain.shared.DomainEvent;
import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PurchasePriceAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PurchasePriceAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * priceNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String priceNo;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * skuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode;

    /**
     * purchaseOrgId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long purchaseOrgId;

    /**
     * priceType（类型：{@code int}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private final int priceType;

    /**
     * currency（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String currency;

    /**
     * unitPrice（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal unitPrice;

    /**
     * taxRate（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal taxRate;

    /**
     * taxIncludedPrice（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal taxIncludedPrice;

    /**
     * effectiveFrom（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate effectiveFrom;

    /**
     * effectiveTo（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate effectiveTo;

    /**
     * sourceType（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sourceType;

    /**
     * sourceNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String sourceNo;

    /**
     * status（类型：{@code PurchasePriceStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private PurchasePriceStatus status;

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
     * 创建 PurchasePriceAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param priceNo 可追踪业务编码，类型为 {@code String}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param priceType 金额或计费值，类型为 {@code int}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param taxIncludedPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code PurchasePriceStatus}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public PurchasePriceAggregate(long id, String priceNo, long supplierId, String skuCode, long purchaseOrgId, int priceType, String currency, BigDecimal unitPrice, BigDecimal taxRate, BigDecimal taxIncludedPrice, LocalDate effectiveFrom, LocalDate effectiveTo, String sourceType, String sourceNo, PurchasePriceStatus status, int version) {
        validate(supplierId, skuCode, purchaseOrgId, priceType, currency, unitPrice, taxRate, effectiveFrom, effectiveTo);
        this.id = id;
        this.priceNo = priceNo;
        this.supplierId = supplierId;
        this.skuCode = skuCode;
        this.purchaseOrgId = purchaseOrgId;
        this.priceType = priceType;
        this.currency = currency;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate;
        this.taxIncludedPrice = taxIncludedPrice == null ? taxIncluded(unitPrice, taxRate) : taxIncludedPrice;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.sourceType = sourceType;
        this.sourceNo = sourceNo;
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param priceType 金额或计费值，类型为 {@code int}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     * @param sourceType 业务处理参数或成员，类型为 {@code String}
     * @param sourceNo 可追踪业务编码，类型为 {@code String}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code PurchasePriceAggregate}
     */
    public static PurchasePriceAggregate create(long supplierId, String skuCode, long purchaseOrgId, int priceType, String currency, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo, String sourceType, String sourceNo, IdentifierGenerator ids) {
        var aggregate = new PurchasePriceAggregate(ids.nextId(), ids.nextCode("PRICE"), supplierId, skuCode, purchaseOrgId, priceType, currency, unitPrice, taxRate, null, effectiveFrom, effectiveTo, sourceType, sourceNo, PurchasePriceStatus.ACTIVE, 0);
        aggregate.raise("PurchasePriceActivated");
        return aggregate;
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void disable(IdentifierGenerator ids) {
        if (status == PurchasePriceStatus.DISABLED) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "采购价格已停用");
        }
        touch();
        this.status = PurchasePriceStatus.DISABLED;
        raise("PurchasePriceDisabled");
    }

    /**
     * 处理当前类型职责中的操作 {@code overlaps}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param from 业务处理参数或成员，类型为 {@code LocalDate}
     * @param to 业务处理参数或成员，类型为 {@code LocalDate}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean overlaps(LocalDate from, LocalDate to) {
        var thisTo = effectiveTo == null ? LocalDate.MAX : effectiveTo;
        var targetTo = to == null ? LocalDate.MAX : to;
        return !thisTo.isBefore(from) && !targetTo.isBefore(effectiveFrom);
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
     */
    private void raise(String eventType) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("priceId", id);
        payload.put("priceNo", priceNo);
        payload.put("supplierId", supplierId);
        payload.put("skuCode", skuCode);
        payload.put("purchaseOrgId", purchaseOrgId);
        payload.put("currency", currency);
        payload.put("unitPrice", unitPrice.toPlainString());
        payload.put("taxRate", taxRate.toPlainString());
        payload.put("status", status.code());
        payload.put("version", version);
        payload.put("sourceType", Objects.requireNonNullElse(sourceType, ""));
        payload.put("sourceNo", Objects.requireNonNullElse(sourceNo, ""));
        events.add(new DomainEvent(0, "PUR-" + eventType + "-" + id + "-" + version, eventType, "PURCHASE_PRICE", Long.toString(id), version, OffsetDateTime.now(), payload));
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param purchaseOrgId 业务或技术标识，类型为 {@code long}
     * @param priceType 金额或计费值，类型为 {@code int}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param effectiveFrom 业务处理参数或成员，类型为 {@code LocalDate}
     * @param effectiveTo 业务处理参数或成员，类型为 {@code LocalDate}
     */
    private static void validate(long supplierId, String skuCode, long purchaseOrgId, int priceType, String currency, BigDecimal unitPrice, BigDecimal taxRate, LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (supplierId <= 0 || purchaseOrgId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "供应商和采购组织不能为空");
        }
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "SKU不能为空");
        }
        if (priceType < 1 || priceType > VALIDATE_VALUE_3) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "价格类型不合法");
        }
        if (currency == null || currency.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "币种不能为空");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "未税单价不能小于0");
        }
        if (taxRate == null || taxRate.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "税率不能小于0");
        }
        boolean effectivePeriodInvalid = effectiveFrom == null || (effectiveTo != null && effectiveTo.isBefore(effectiveFrom));
        if (effectivePeriodInvalid) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "价格有效期不合法");
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code taxIncluded}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal taxIncluded(BigDecimal unitPrice, BigDecimal taxRate) {
        return unitPrice.multiply(BigDecimal.ONE.add(taxRate)).setScale(6, RoundingMode.HALF_UP);
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
     * 处理当前类型职责中的操作 {@code priceNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String priceNo() {
        return priceNo;
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
     * 处理当前类型职责中的操作 {@code skuCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String skuCode() {
        return skuCode;
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
     * 处理当前类型职责中的操作 {@code priceType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int priceType() {
        return priceType;
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
     * 处理当前类型职责中的操作 {@code unitPrice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal unitPrice() {
        return unitPrice;
    }

    /**
     * 处理当前类型职责中的操作 {@code taxRate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal taxRate() {
        return taxRate;
    }

    /**
     * 处理当前类型职责中的操作 {@code taxIncludedPrice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal taxIncludedPrice() {
        return taxIncludedPrice;
    }

    /**
     * 处理当前类型职责中的操作 {@code effectiveFrom}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
     */
    public LocalDate effectiveFrom() {
        return effectiveFrom;
    }

    /**
     * 处理当前类型职责中的操作 {@code effectiveTo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDate}
     */
    public LocalDate effectiveTo() {
        return effectiveTo;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceType() {
        return sourceType;
    }

    /**
     * 处理当前类型职责中的操作 {@code sourceNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sourceNo() {
        return sourceNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code PurchasePriceStatus}
     */
    public PurchasePriceStatus status() {
        return status;
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
     * 业务常量 {@code VALIDATE_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int VALIDATE_VALUE_3 = 3;
}
