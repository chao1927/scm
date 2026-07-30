package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * AfterSaleAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class AfterSaleAggregate {

    /**
     * PENDING_REVIEW（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_REVIEW = 1;

    /**
     * APPROVED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int APPROVED = 2;

    /**
     * REFUND_REQUESTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REFUND_REQUESTED = 3;

    /**
     * REFUNDED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REFUNDED = 4;

    /**
     * COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int COMPLETED = 5;

    /**
     * REJECTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REJECTED = 6;

    /**
     * afterSaleNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String afterSaleNo;

    /**
     * salesOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String salesOrderNo;

    /**
     * fulfillmentNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String fulfillmentNo;

    /**
     * refundAmount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal refundAmount;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String reason;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * refundedAmount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal refundedAmount;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<OmsEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<OmsEvent> events = new ArrayList<>();

    /**
     * 创建 AfterSaleAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param refundedAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private AfterSaleAggregate(String afterSaleNo, String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount, String reason, int status, BigDecimal refundedAmount, long version) {
        if (blank(afterSaleNo) || blank(salesOrderNo) || blank(fulfillmentNo) || refundAmount == null || refundAmount.signum() <= 0 || blank(reason)) {
            throw new IllegalArgumentException("after-sale references, amount and reason are required");
        }
        this.afterSaleNo = afterSaleNo;
        this.salesOrderNo = salesOrderNo;
        this.fulfillmentNo = fulfillmentNo;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = status;
        this.refundedAmount = refundedAmount == null ? BigDecimal.ZERO : refundedAmount;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code AfterSaleAggregate}
     */
    public static AfterSaleAggregate create(String afterSaleNo, String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount, String reason) {
        AfterSaleAggregate aggregate = new AfterSaleAggregate(afterSaleNo, salesOrderNo, fulfillmentNo, refundAmount, reason, PENDING_REVIEW, BigDecimal.ZERO, 1);
        aggregate.events.add(OmsEvent.of("AfterSaleCreated", afterSaleNo, salesOrderNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param refundedAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code AfterSaleAggregate}
     */
    public static AfterSaleAggregate restore(String afterSaleNo, String salesOrderNo, String fulfillmentNo, BigDecimal refundAmount, String reason, int status, BigDecimal refundedAmount, long version) {
        return new AfterSaleAggregate(afterSaleNo, salesOrderNo, fulfillmentNo, refundAmount, reason, status, refundedAmount, version);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param remark 业务处理参数或成员，类型为 {@code String}
     */
    public void approve(String remark) {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("after-sale is not pending review");
        }
        if (blank(remark)) {
            throw new IllegalArgumentException("approval remark is required");
        }
        status = APPROVED;
        version++;
        events.add(OmsEvent.of("AfterSaleApproved", afterSaleNo, remark));
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void requestRefund() {
        if (status != APPROVED) {
            throw new IllegalStateException("after-sale is not approved");
        }
        status = REFUND_REQUESTED;
        version++;
        events.add(OmsEvent.of("RefundRequested", afterSaleNo, refundAmount.toPlainString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code markRefunded}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     */
    public void markRefunded(BigDecimal amount) {
        if (status != REFUND_REQUESTED) {
            throw new IllegalStateException("refund is not requested");
        }
        if (amount == null || amount.signum() <= 0 || amount.compareTo(refundAmount) > 0) {
            throw new IllegalArgumentException("refund amount exceeds request");
        }
        refundedAmount = amount;
        status = REFUNDED;
        version++;
        events.add(OmsEvent.of("RefundCompleted", afterSaleNo, amount.toPlainString()));
    }

    /**
     * 执行命令 {@code complete}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void complete() {
        if (status != REFUNDED) {
            throw new IllegalStateException("after-sale is not refunded");
        }
        status = COMPLETED;
        version++;
        events.add(OmsEvent.of("AfterSaleCompleted", afterSaleNo, salesOrderNo));
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OmsEvent>}
     */
    public List<OmsEvent> pullEvents() {
        List<OmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code afterSaleNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String afterSaleNo() {
        return afterSaleNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code salesOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String salesOrderNo() {
        return salesOrderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code fulfillmentNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String fulfillmentNo() {
        return fulfillmentNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code refundAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal refundAmount() {
        return refundAmount;
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
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int status() {
        return status;
    }

    /**
     * 处理当前类型职责中的操作 {@code refundedAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal refundedAmount() {
        return refundedAmount;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long version() {
        return version;
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
