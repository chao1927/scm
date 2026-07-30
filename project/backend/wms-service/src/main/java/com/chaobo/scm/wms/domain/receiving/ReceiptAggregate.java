package com.chaobo.scm.wms.domain.receiving;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;
import java.math.BigDecimal;

/**
 * ReceiptAggregate。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ReceiptAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * receiptNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String receiptNo;

    /**
     * inboundId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long inboundId;

    /**
     * skuCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String skuCode;

    /**
     * expectedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal expectedQty;

    /**
     * receivedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal receivedQty;

    /**
     * rejectedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal rejectedQty;

    /**
     * status（类型：{@code ReceiptStatus}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private ReceiptStatus status;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 ReceiptAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @param inboundId 业务或技术标识，类型为 {@code long}
     * @param skuCode 可追踪业务编码，类型为 {@code String}
     * @param expectedQty 数量值，类型为 {@code BigDecimal}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param rejectedQty 数量值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code ReceiptStatus}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public ReceiptAggregate(long id, String receiptNo, long inboundId, String skuCode, BigDecimal expectedQty, BigDecimal receivedQty, BigDecimal rejectedQty, ReceiptStatus status, int version) {
        if (inboundId <= 0 || skuCode == null || skuCode.isBlank() || expectedQty == null || expectedQty.signum() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "收货单来源和通知数量不能为空");
        }
        this.id = id;
        this.receiptNo = receiptNo;
        this.inboundId = inboundId;
        this.skuCode = skuCode;
        this.expectedQty = expectedQty;
        this.receivedQty = zero(receivedQty);
        this.rejectedQty = zero(rejectedQty);
        this.status = status;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code scan}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rejected 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     */
    public void scan(BigDecimal received, BigDecimal rejected, String rejectReason) {
        ensureReceiving();
        if (received == null || rejected == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "扫码收货数量不合法");
        }
        boolean receivedQuantityInvalid = received.signum() < 0;
        boolean rejectedQuantityInvalid = rejected.signum() < 0;
        boolean totalQuantityEmpty = received.add(rejected).signum() <= 0;
        if (receivedQuantityInvalid || rejectedQuantityInvalid || totalQuantityEmpty) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "扫码收货数量不合法");
        }
        boolean rejectReasonMissing = rejectReason == null || rejectReason.isBlank();
        if (rejected.signum() > 0 && rejectReasonMissing) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "拒收必须填写原因");
        }
        if (receivedQty.add(rejectedQty).add(received).add(rejected).compareTo(expectedQty) > 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "收货数量不能超过通知数量");
        }
        receivedQty = receivedQty.add(received);
        rejectedQty = rejectedQty.add(rejected);
        version++;
    }

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void complete() {
        ensureReceiving();
        if (receivedQty.add(rejectedQty).compareTo(expectedQty) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_FAILED, "收货数量与通知数量不一致");
        }
        status = ReceiptStatus.COMPLETED;
        version++;
    }

    /**
     * 校验业务约束 {@code ensureReceiving}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void ensureReceiving() {
        if (status != ReceiptStatus.RECEIVING) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "收货单当前不可操作");
        }
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
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code receiptNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String receiptNo() {
        return receiptNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code inboundId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long inboundId() {
        return inboundId;
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
     * 处理当前类型职责中的操作 {@code expectedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal expectedQty() {
        return expectedQty;
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
     * 执行命令 {@code rejectedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal rejectedQty() {
        return rejectedQty;
    }

    /**
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReceiptStatus}
     */
    public ReceiptStatus status() {
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
}
