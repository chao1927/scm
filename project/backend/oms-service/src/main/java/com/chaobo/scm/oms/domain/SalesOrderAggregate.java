package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SalesOrderAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class SalesOrderAggregate {

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
     * INTERCEPTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int INTERCEPTED = 3;

    /**
     * orderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String orderNo;

    /**
     * channelCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String channelCode;

    /**
     * channelOrderNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String channelOrderNo;

    /**
     * customerId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final Long customerId;

    /**
     * receiverAddress（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String receiverAddress;

    /**
     * lines（类型：{@code List<OrderLine>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<OrderLine> lines;

    /**
     * totalAmount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal totalAmount;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * reviewRemark（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String reviewRemark;

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
     * 创建 SalesOrderAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param channelOrderNo 可追踪业务编码，类型为 {@code String}
     * @param customerId 业务或技术标识，类型为 {@code Long}
     * @param receiverAddress 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<OrderLine>}
     * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reviewRemark 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private SalesOrderAggregate(String orderNo, String channelCode, String channelOrderNo, Long customerId, String receiverAddress, List<OrderLine> lines, BigDecimal totalAmount, int status, String reviewRemark, long version) {
        if (blank(orderNo) || blank(channelCode) || blank(channelOrderNo) || customerId == null || blank(receiverAddress)) {
            throw new IllegalArgumentException("orderNo, channel, customer and address are required");
        }
        validateLines(lines);
        this.orderNo = orderNo;
        this.channelCode = channelCode;
        this.channelOrderNo = channelOrderNo;
        this.customerId = customerId;
        this.receiverAddress = receiverAddress;
        this.lines = new ArrayList<>(lines);
        this.totalAmount = totalAmount == null ? amountOf(lines) : totalAmount;
        if (this.totalAmount.signum() < 0) {
            throw new IllegalArgumentException("total amount cannot be negative");
        }
        this.status = status;
        this.reviewRemark = reviewRemark;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param channelOrderNo 可追踪业务编码，类型为 {@code String}
     * @param customerId 业务或技术标识，类型为 {@code Long}
     * @param receiverAddress 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<OrderLine>}
     * @return 执行命令的结果，类型为 {@code SalesOrderAggregate}
     */
    public static SalesOrderAggregate create(String orderNo, String channelCode, String channelOrderNo, Long customerId, String receiverAddress, List<OrderLine> lines) {
        SalesOrderAggregate aggregate = new SalesOrderAggregate(orderNo, channelCode, channelOrderNo, customerId, receiverAddress, lines, amountOf(lines), PENDING_REVIEW, null, 1);
        aggregate.events.add(OmsEvent.of("ChannelOrderReceived", orderNo, channelCode + ":" + channelOrderNo));
        aggregate.events.add(OmsEvent.of("SalesOrderCreated", orderNo, aggregate.totalAmount.toPlainString()));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param channelCode 可追踪业务编码，类型为 {@code String}
     * @param channelOrderNo 可追踪业务编码，类型为 {@code String}
     * @param customerId 业务或技术标识，类型为 {@code Long}
     * @param receiverAddress 业务处理参数或成员，类型为 {@code String}
     * @param lines 业务处理参数或成员，类型为 {@code List<OrderLine>}
     * @param totalAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param reviewRemark 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code SalesOrderAggregate}
     */
    public static SalesOrderAggregate restore(String orderNo, String channelCode, String channelOrderNo, Long customerId, String receiverAddress, List<OrderLine> lines, BigDecimal totalAmount, int status, String reviewRemark, long version) {
        return new SalesOrderAggregate(orderNo, channelCode, channelOrderNo, customerId, receiverAddress, lines, totalAmount, status, reviewRemark, version);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param remark 业务处理参数或成员，类型为 {@code String}
     */
    public void approve(String remark) {
        ensurePendingReview();
        status = APPROVED;
        reviewRemark = remark;
        version++;
        events.add(OmsEvent.of("SalesOrderReviewed", orderNo, "APPROVED"));
    }

    /**
     * 处理当前类型职责中的操作 {@code intercept}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void intercept(String reason) {
        ensurePendingReview();
        if (blank(reason)) {
            throw new IllegalArgumentException("intercept reason is required");
        }
        status = INTERCEPTED;
        reviewRemark = reason;
        version++;
        events.add(OmsEvent.of("SalesOrderIntercepted", orderNo, reason));
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
     * 处理当前类型职责中的操作 {@code orderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String orderNo() {
        return orderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code channelCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String channelCode() {
        return channelCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code channelOrderNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String channelOrderNo() {
        return channelOrderNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code customerId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long customerId() {
        return customerId;
    }

    /**
     * 处理当前类型职责中的操作 {@code receiverAddress}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String receiverAddress() {
        return receiverAddress;
    }

    /**
     * 处理当前类型职责中的操作 {@code lines}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OrderLine>}
     */
    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    /**
     * 转换数据模型 {@code totalAmount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 转换数据模型的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal totalAmount() {
        return totalAmount;
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
     * 处理当前类型职责中的操作 {@code reviewRemark}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String reviewRemark() {
        return reviewRemark;
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
     * 校验业务约束 {@code ensurePendingReview}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void ensurePendingReview() {
        if (status != PENDING_REVIEW) {
            throw new IllegalStateException("sales order is not pending review");
        }
    }

    /**
     * 校验业务约束 {@code validateLines}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<OrderLine>}
     */
    private static void validateLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("order lines are required");
        }
        for (OrderLine line : lines) {
            if (blank(line.skuCode()) || line.quantity() <= 0 || line.unitPrice() == null || line.unitPrice().signum() < 0) {
                throw new IllegalArgumentException("invalid order line");
            }
        }
    }

    /**
     * 处理当前类型职责中的操作 {@code amountOf}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param lines 业务处理参数或成员，类型为 {@code List<OrderLine>}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal amountOf(List<OrderLine> lines) {
        return lines.stream().map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
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

    /**
     * OrderLine。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record OrderLine(String skuCode, int quantity, BigDecimal unitPrice) {
    }
}
