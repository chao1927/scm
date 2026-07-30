package com.chaobo.scm.purchase.domain.order;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * PurchaseOrderLine。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class PurchaseOrderLine {

    /**
     * lineId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long lineId;

    /**
     * skuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode;

    /**
     * skuName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String skuName;

    /**
     * orderQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal orderQty;

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
     * requiredDeliveryDate（类型：{@code LocalDate}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private final LocalDate requiredDeliveryDate;

    /**
     * receivedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal receivedQty;

    /**
     * 创建 PurchaseOrderLine。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param lineId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param skuName 业务处理参数或成员，类型为 {@code String}
     * @param orderQty 数量值，类型为 {@code BigDecimal}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param taxIncludedPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param requiredDeliveryDate 业务时间，类型为 {@code LocalDate}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     */
    public PurchaseOrderLine(long lineId, String skuCode, String skuName, BigDecimal orderQty, BigDecimal unitPrice, BigDecimal taxRate, BigDecimal taxIncludedPrice, LocalDate requiredDeliveryDate, BigDecimal receivedQty) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购订单SKU不能为空");
        }
        if (orderQty == null || orderQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购数量必须大于0");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "采购单价不能小于0");
        }
        this.lineId = lineId;
        this.skuCode = skuCode;
        this.skuName = skuName;
        this.orderQty = orderQty;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        this.taxIncludedPrice = taxIncludedPrice == null ? taxIncluded(unitPrice, this.taxRate) : taxIncludedPrice;
        this.requiredDeliveryDate = requiredDeliveryDate;
        this.receivedQty = receivedQty == null ? BigDecimal.ZERO : receivedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code changeQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param newQty 数量值，类型为 {@code BigDecimal}
     */
    public void changeQty(BigDecimal newQty) {
        if (newQty == null || newQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "变更后采购数量必须大于0");
        }
        if (newQty.compareTo(receivedQty) < 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "采购数量不能小于已收货数量");
        }
        this.orderQty = newQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code amount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal amount() {
        return orderQty.multiply(unitPrice).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 处理当前类型职责中的操作 {@code taxAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal taxAmount() {
        return amount().multiply(taxRate).setScale(6, RoundingMode.HALF_UP);
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
     * 处理当前类型职责中的操作 {@code lineId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long lineId() {
        return lineId;
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
     * 处理当前类型职责中的操作 {@code skuName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String skuName() {
        return skuName;
    }

    /**
     * 处理当前类型职责中的操作 {@code orderQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal orderQty() {
        return orderQty;
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
     * 查询并返回 {@code requiredDeliveryDate}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @return 查询并返回的结果，类型为 {@code LocalDate}
     */
    public LocalDate requiredDeliveryDate() {
        return requiredDeliveryDate;
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
}
