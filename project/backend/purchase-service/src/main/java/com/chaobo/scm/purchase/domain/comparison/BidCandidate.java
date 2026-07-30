package com.chaobo.scm.purchase.domain.comparison;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BidCandidate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class BidCandidate {

    /**
     * candidateId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long candidateId;

    /**
     * supplierId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long supplierId;

    /**
     * supplierName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String supplierName;

    /**
     * quoteNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String quoteNo;

    /**
     * skuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode;

    /**
     * quoteQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal quoteQty;

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
     * deliveryDays（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int deliveryDays;

    /**
     * supplierScore（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal supplierScore;

    /**
     * transportScore（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的应用或外部协作依赖；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal transportScore;

    /**
     * estimatedFreightCost（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal estimatedFreightCost;

    /**
     * totalCost（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal totalCost;

    /**
     * compositeScore（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal compositeScore;

    /**
     * awarded（类型：{@code boolean}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private boolean awarded;

    /**
     * 创建 BidCandidate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param candidateId 业务或技术标识，类型为 {@code long}
     * @param supplierId 业务或技术标识，类型为 {@code long}
     * @param supplierName 业务处理参数或成员，类型为 {@code String}
     * @param quoteNo 可追踪业务编码，类型为 {@code String}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param quoteQty 数量值，类型为 {@code BigDecimal}
     * @param unitPrice 金额或计费值，类型为 {@code BigDecimal}
     * @param taxRate 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param deliveryDays 业务处理参数或成员，类型为 {@code int}
     * @param supplierScore 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param transportScore 应用或外部协作依赖，类型为 {@code BigDecimal}
     * @param estimatedFreightCost 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param awarded 业务处理参数或成员，类型为 {@code boolean}
     */
    public BidCandidate(long candidateId, long supplierId, String supplierName, String quoteNo, String skuCode, BigDecimal quoteQty, BigDecimal unitPrice, BigDecimal taxRate, int deliveryDays, BigDecimal supplierScore, BigDecimal transportScore, BigDecimal estimatedFreightCost, boolean awarded) {
        if (supplierId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选供应商不能为空");
        }
        if (quoteNo == null || quoteNo.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选报价单号不能为空");
        }
        if (skuCode == null || skuCode.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选SKU不能为空");
        }
        if (quoteQty == null || quoteQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选报价数量必须大于0");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "候选报价单价不能小于0");
        }
        if (deliveryDays < 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "交期天数不能小于0");
        }
        this.candidateId = candidateId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.quoteNo = quoteNo;
        this.skuCode = skuCode;
        this.quoteQty = quoteQty;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        this.deliveryDays = deliveryDays;
        this.supplierScore = defaultScore(supplierScore);
        this.transportScore = defaultScore(transportScore);
        this.estimatedFreightCost = estimatedFreightCost == null ? BigDecimal.ZERO : estimatedFreightCost;
        this.totalCost = unitPrice.multiply(quoteQty).add(this.estimatedFreightCost).setScale(6, RoundingMode.HALF_UP);
        this.compositeScore = calculateScore();
        this.awarded = awarded;
    }

    /**
     * 处理当前类型职责中的操作 {@code award}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void award() {
        this.awarded = true;
    }

    /**
     * 处理当前类型职责中的操作 {@code clearAward}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void clearAward() {
        this.awarded = false;
    }

    /**
     * 处理当前类型职责中的操作 {@code calculateScore}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private BigDecimal calculateScore() {
        var priceScore = BigDecimal.valueOf(1000).divide(totalCost.add(BigDecimal.ONE), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        var deliveryScore = BigDecimal.valueOf(Math.max(0, 100 - deliveryDays));
        return priceScore.multiply(BigDecimal.valueOf(0.45)).add(supplierScore.multiply(BigDecimal.valueOf(0.25))).add(transportScore.multiply(BigDecimal.valueOf(0.2))).add(deliveryScore.multiply(BigDecimal.valueOf(0.1))).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 处理当前类型职责中的操作 {@code defaultScore}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal defaultScore(BigDecimal value) {
        return value == null ? BigDecimal.valueOf(60) : value;
    }

    /**
     * 处理当前类型职责中的操作 {@code candidateId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long candidateId() {
        return candidateId;
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
     * 处理当前类型职责中的操作 {@code supplierName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String supplierName() {
        return supplierName;
    }

    /**
     * 处理当前类型职责中的操作 {@code quoteNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String quoteNo() {
        return quoteNo;
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
     * 处理当前类型职责中的操作 {@code quoteQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal quoteQty() {
        return quoteQty;
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
     * 处理当前类型职责中的操作 {@code deliveryDays}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int deliveryDays() {
        return deliveryDays;
    }

    /**
     * 处理当前类型职责中的操作 {@code supplierScore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal supplierScore() {
        return supplierScore;
    }

    /**
     * 处理当前类型职责中的操作 {@code transportScore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal transportScore() {
        return transportScore;
    }

    /**
     * 处理当前类型职责中的操作 {@code estimatedFreightCost}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal estimatedFreightCost() {
        return estimatedFreightCost;
    }

    /**
     * 转换数据模型 {@code totalCost}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal totalCost() {
        return totalCost;
    }

    /**
     * 处理当前类型职责中的操作 {@code compositeScore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal compositeScore() {
        return compositeScore;
    }

    /**
     * 处理当前类型职责中的操作 {@code awarded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    public boolean awarded() {
        return awarded;
    }
}
