package com.chaobo.scm.supplier.domain.item;

import com.chaobo.scm.common.error.*;
import com.chaobo.scm.supplier.domain.shared.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * SupplierItemAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierItemAggregate {

    /**
     * itemId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long itemId;

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
     * supplierSkuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String supplierSkuCode;

    /**
     * condition（类型：{@code SupplyCondition}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private SupplyCondition condition;

    /**
     * status（类型：{@code SupplyStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private SupplyStatus status;

    /**
     * pauseReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String pauseReason;

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
     * 创建 SupplierItemAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param supplierSku 业务处理参数或成员，类型为 {@code String}
     * @param condition 业务处理参数或成员，类型为 {@code SupplyCondition}
     * @param status 生命周期状态，类型为 {@code SupplyStatus}
     * @param pauseReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    private SupplierItemAggregate(long id, long supplierId, String sku, String supplierSku, SupplyCondition condition, SupplyStatus status, String pauseReason, int version) {
        this.itemId = id;
        this.supplierId = supplierId;
        this.skuCode = sku;
        this.supplierSkuCode = supplierSku;
        this.condition = condition;
        this.status = status;
        this.pauseReason = pauseReason;
        this.version = version;
    }

    /**
     * 执行命令 {@code enable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param supplierSku 业务处理参数或成员，类型为 {@code String}
     * @param condition 业务处理参数或成员，类型为 {@code SupplyCondition}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @return 执行命令的结果，类型为 {@code SupplierItemAggregate}
     */
    public static SupplierItemAggregate enable(long supplierId, String sku, String supplierSku, SupplyCondition condition, long operator, IdentifierGenerator ids) {
        if (supplierId <= 0 || sku == null || sku.isBlank()) {
            throw rule("供应商和SKU不能为空");
        }
        long id = ids.nextId();
        var a = new SupplierItemAggregate(id, supplierId, sku, supplierSku, condition, SupplyStatus.AVAILABLE, null, 0);
        a.raise(ids, "SupplierItemEnabled", "供应商商品已启用", operator, Map.of("supplierId", supplierId, "skuCode", sku));
        return a;
    }

    /**
     * 处理当前类型职责中的操作 {@code rehydrate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param supplierSku 业务处理参数或成员，类型为 {@code String}
     * @param condition 业务处理参数或成员，类型为 {@code SupplyCondition}
     * @param status 生命周期状态，类型为 {@code SupplyStatus}
     * @param pauseReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplierItemAggregate}
     */
    public static SupplierItemAggregate rehydrate(long id, long supplierId, String sku, String supplierSku, SupplyCondition condition, SupplyStatus status, String pauseReason, int version) {
        return new SupplierItemAggregate(id, supplierId, sku, supplierSku, condition, status, pauseReason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code changeCondition}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param supplierSku 业务处理参数或成员，类型为 {@code String}
     * @param condition 业务处理参数或成员，类型为 {@code SupplyCondition}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void changeCondition(String supplierSku, SupplyCondition condition, long operator, IdentifierGenerator ids) {
        if (status == SupplyStatus.DISCONTINUED) {
            throw state("停供商品不能修改供货条件");
        }
        this.supplierSkuCode = supplierSku;
        this.condition = condition;
        version++;
        raise(ids, "SupplierItemSupplyConditionChanged", "供应商商品供货条件已变更", operator, Map.of("supplierId", supplierId, "skuCode", skuCode));
    }

    /**
     * 处理当前类型职责中的操作 {@code pause}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void pause(String reason, long operator, IdentifierGenerator ids) {
        if (status != SupplyStatus.AVAILABLE) {
            throw state("只有可供商品可以暂停");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("暂停原因不能为空");
        }
        status = SupplyStatus.PAUSED;
        pauseReason = reason.trim();
        version++;
        raise(ids, "SupplierItemPaused", "供应商商品已暂停", operator, Map.of("reason", pauseReason));
    }

    /**
     * 处理当前类型职责中的操作 {@code resume}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void resume(long operator, IdentifierGenerator ids) {
        if (status != SupplyStatus.PAUSED) {
            throw state("只有暂停商品可以恢复");
        }
        status = SupplyStatus.AVAILABLE;
        pauseReason = null;
        version++;
        raise(ids, "SupplierItemResumed", "供应商商品已恢复", operator, Map.of("supplierId", supplierId, "skuCode", skuCode));
    }

    /**
     * 处理当前类型职责中的操作 {@code discontinue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     */
    public void discontinue(String reason, long operator, IdentifierGenerator ids) {
        if (status == SupplyStatus.DISCONTINUED) {
            throw state("当前商品已停供");
        }
        if (reason == null || reason.isBlank()) {
            throw rule("停供原因不能为空");
        }
        status = SupplyStatus.DISCONTINUED;
        pauseReason = reason.trim();
        version++;
        raise(ids, "SupplierItemDiscontinued", "供应商商品已停供", operator, Map.of("reason", pauseReason, "supplierId", supplierId, "skuCode", skuCode));
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param name 业务处理参数或成员，类型为 {@code String}
     * @param operator 业务处理参数或成员，类型为 {@code long}
     * @param payload 业务处理参数或成员，类型为 {@code Map<String,Object>}
     */
    private void raise(IdentifierGenerator ids, String type, String name, long operator, Map<String, Object> payload) {
        long eventId = ids.nextId();
        events.add(new DomainEvent(eventId, "SUP-" + eventId, type, name, "SUPPLIER_ITEM", itemId, Long.toString(itemId), version, operator, OffsetDateTime.now(), payload));
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
        var c = List.copyOf(events);
        events.clear();
        return c;
    }

    /**
     * 处理当前类型职责中的操作 {@code itemId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long itemId() {
        return itemId;
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
     * 处理当前类型职责中的操作 {@code supplierSkuCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String supplierSkuCode() {
        return supplierSkuCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code condition}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplyCondition}
     */
    public SupplyCondition condition() {
        return condition;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SupplyStatus}
     */
    public SupplyStatus status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code pauseReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String pauseReason() {
        return pauseReason;
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
