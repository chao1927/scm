package com.chaobo.scm.supplier.domain.returning;

import com.chaobo.scm.common.error.*;
import java.math.BigDecimal;

/**
 * SupplierReturnLine。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class SupplierReturnLine {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * skuCode、batchNo、inventoryStatus（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode, batchNo, inventoryStatus;

    /**
     * requestedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal requestedQty;

    /**
     * lockedQty、outboundQty、signedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal lockedQty, outboundQty, signedQty;

    /**
     * 创建 SupplierReturnLine。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param batchNo 可追踪业务编码，类型为 {@code String}
     * @param inventoryStatus 生命周期状态，类型为 {@code String}
     * @param requestedQty 数量值，类型为 {@code BigDecimal}
     * @param lockedQty 数量值，类型为 {@code BigDecimal}
     * @param outboundQty 数量值，类型为 {@code BigDecimal}
     * @param signedQty 数量值，类型为 {@code BigDecimal}
     */
    public SupplierReturnLine(long id, String skuCode, String batchNo, String inventoryStatus, BigDecimal requestedQty, BigDecimal lockedQty, BigDecimal outboundQty, BigDecimal signedQty) {
        this.id = id;
        this.skuCode = skuCode;
        this.batchNo = batchNo;
        this.inventoryStatus = inventoryStatus;
        this.requestedQty = requestedQty;
        this.lockedQty = nvl(lockedQty);
        this.outboundQty = nvl(outboundQty);
        this.signedQty = nvl(signedQty);
        validate();
    }

    /**
     * 处理当前类型职责中的操作 {@code lock}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void lock(BigDecimal qty) {
        qty = positive(qty, "锁定数量");
        if (qty.compareTo(requestedQty) > 0) {
            throw rule("锁定数量不能超过申请数量");
        }
        lockedQty = qty;
    }

    /**
     * 处理当前类型职责中的操作 {@code outbound}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void outbound(BigDecimal qty) {
        qty = positive(qty, "出库数量");
        if (qty.compareTo(lockedQty) > 0) {
            throw rule("出库数量不能超过锁定数量");
        }
        outboundQty = qty;
    }

    /**
     * 处理当前类型职责中的操作 {@code sign}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param qty 数量值，类型为 {@code BigDecimal}
     */
    public void sign(BigDecimal qty) {
        qty = nonNegative(qty, "签收数量");
        if (qty.compareTo(outboundQty) > 0) {
            throw rule("签收数量不能超过出库数量");
        }
        signedQty = qty;
    }

    /**
     * 校验业务约束 {@code validate}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validate() {
        if (id <= 0 || skuCode == null || skuCode.isBlank() || inventoryStatus == null || inventoryStatus.isBlank()) {
            throw rule("退供明细信息不完整");
        }
        positive(requestedQty, "申请数量");
        if (lockedQty.compareTo(requestedQty) > 0 || outboundQty.compareTo(lockedQty) > 0 || signedQty.compareTo(outboundQty) > 0) {
            throw rule("退供数量链不合法");
        }
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
     * 处理当前类型职责中的操作 {@code positive}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param n 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal positive(BigDecimal v, String n) {
        if (v == null || v.signum() <= 0) {
            throw rule(n + "必须大于0");
        }
        return v;
    }

    /**
     * 处理当前类型职责中的操作 {@code nonNegative}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param n 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal nonNegative(BigDecimal v, String n) {
        if (v == null || v.signum() < 0) {
            throw rule(n + "不能小于0");
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
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
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
     * 处理当前类型职责中的操作 {@code batchNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String batchNo() {
        return batchNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code inventoryStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String inventoryStatus() {
        return inventoryStatus;
    }

    /**
     * 处理当前类型职责中的操作 {@code requestedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal requestedQty() {
        return requestedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code lockedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal lockedQty() {
        return lockedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code outboundQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal outboundQty() {
        return outboundQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code signedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal signedQty() {
        return signedQty;
    }
}
