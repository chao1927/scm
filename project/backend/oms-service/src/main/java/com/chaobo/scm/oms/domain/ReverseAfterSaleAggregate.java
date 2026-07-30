package com.chaobo.scm.oms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ReverseAfterSaleAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ReverseAfterSaleAggregate {

    /**
     * Type。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public enum Type {

        // 业务枚举值：refund only
        REFUND_ONLY,
        // 业务枚举值：return refund
        RETURN_REFUND,
        // 业务枚举值：exchange
        EXCHANGE,
        // 业务枚举值：reship
        RESHIP
    }

    /**
     * PENDING_REVIEW（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_REVIEW = 1;

    /**
     * RETURN_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RETURN_PENDING = 2;

    /**
     * INSPECTION_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int INSPECTION_PENDING = 3;

    /**
     * REFUND_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REFUND_PENDING = 4;

    /**
     * REFUND_REQUESTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REFUND_REQUESTED = 5;

    /**
     * RESHIP_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RESHIP_PENDING = 6;

    /**
     * RESHIP_REQUESTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int RESHIP_REQUESTED = 7;

    /**
     * COMPLETED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int COMPLETED = 8;

    /**
     * REJECTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REJECTED = 9;

    /**
     * EXCEPTION_PENDING（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int EXCEPTION_PENDING = 10;

    /**
     * afterSaleNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String afterSaleNo;

    /**
     * type（类型：{@code Type}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final Type type;

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
     * ownerId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long ownerId;

    /**
     * sku（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String sku;

    /**
     * applyQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal applyQty;

    /**
     * refundAmount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private final BigDecimal refundAmount;

    /**
     * returnWarehouseId（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long returnWarehouseId;

    /**
     * reason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String reason;

    /**
     * rmaNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String rmaNo;

    /**
     * receivedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal receivedQty;

    /**
     * acceptedQty（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal acceptedQty;

    /**
     * refundedAmount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal refundedAmount;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

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
     * 创建 ReverseAfterSaleAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param afterSaleNo 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code Type}
     * @param salesOrderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param applyQty 数量值，类型为 {@code BigDecimal}
     * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param returnWarehouseId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param rmaNo 可追踪业务编码，类型为 {@code String}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param acceptedQty 数量值，类型为 {@code BigDecimal}
     * @param refundedAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private ReverseAfterSaleAggregate(String afterSaleNo, Type type, String salesOrderNo, String fulfillmentNo, long ownerId, String sku, BigDecimal applyQty, BigDecimal refundAmount, long returnWarehouseId, String reason, String rmaNo, BigDecimal receivedQty, BigDecimal acceptedQty, BigDecimal refundedAmount, int status, long version) {
        if (blank(afterSaleNo) || type == null || blank(salesOrderNo) || blank(fulfillmentNo) || ownerId <= 0 || blank(sku) || !positive(applyQty) || refundAmount == null || refundAmount.signum() < 0 || blank(reason)) {
            throw new IllegalArgumentException("reverse after-sale data is invalid");
        }
        if (requiresReturn(type) && returnWarehouseId <= 0) {
            throw new IllegalArgumentException("return warehouse is required");
        }
        this.afterSaleNo = afterSaleNo;
        this.type = type;
        this.salesOrderNo = salesOrderNo;
        this.fulfillmentNo = fulfillmentNo;
        this.ownerId = ownerId;
        this.sku = sku;
        this.applyQty = applyQty;
        this.refundAmount = refundAmount;
        this.returnWarehouseId = returnWarehouseId;
        this.reason = reason;
        this.rmaNo = rmaNo;
        this.receivedQty = zero(receivedQty);
        this.acceptedQty = zero(acceptedQty);
        this.refundedAmount = zero(refundedAmount);
        this.status = status;
        this.version = version;
        validateQuantities();
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code Type}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param applyQty 数量值，类型为 {@code BigDecimal}
     * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param returnWarehouseId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code ReverseAfterSaleAggregate}
     */
    public static ReverseAfterSaleAggregate create(String no, Type type, String orderNo, String fulfillmentNo, long ownerId, String sku, BigDecimal applyQty, BigDecimal refundAmount, long returnWarehouseId, String reason) {
        var aggregate = new ReverseAfterSaleAggregate(no, type, orderNo, fulfillmentNo, ownerId, sku, applyQty, refundAmount, returnWarehouseId, reason, null, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, PENDING_REVIEW, 0);
        aggregate.events.add(OmsEvent.of("ReverseAfterSaleCreated", no, orderNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param no 可追踪业务编码，类型为 {@code String}
     * @param type 业务处理参数或成员，类型为 {@code Type}
     * @param orderNo 可追踪业务编码，类型为 {@code String}
     * @param fulfillmentNo 可追踪业务编码，类型为 {@code String}
     * @param ownerId 业务或技术标识，类型为 {@code long}
     * @param sku 业务处理参数或成员，类型为 {@code String}
     * @param applyQty 数量值，类型为 {@code BigDecimal}
     * @param refundAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param returnWarehouseId 业务或技术标识，类型为 {@code long}
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param rmaNo 可追踪业务编码，类型为 {@code String}
     * @param receivedQty 数量值，类型为 {@code BigDecimal}
     * @param acceptedQty 数量值，类型为 {@code BigDecimal}
     * @param refundedAmount 金额或计费值，类型为 {@code BigDecimal}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ReverseAfterSaleAggregate}
     */
    public static ReverseAfterSaleAggregate restore(String no, Type type, String orderNo, String fulfillmentNo, long ownerId, String sku, BigDecimal applyQty, BigDecimal refundAmount, long returnWarehouseId, String reason, String rmaNo, BigDecimal receivedQty, BigDecimal acceptedQty, BigDecimal refundedAmount, int status, long version) {
        return new ReverseAfterSaleAggregate(no, type, orderNo, fulfillmentNo, ownerId, sku, applyQty, refundAmount, returnWarehouseId, reason, rmaNo, receivedQty, acceptedQty, refundedAmount, status, version);
    }

    /**
     * 执行命令 {@code approve}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param rmaNo 可追踪业务编码，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void approve(String rmaNo, long expectedVersion) {
        require(PENDING_REVIEW, expectedVersion, "after-sale is not pending review");
        if (requiresReturn(type)) {
            if (blank(rmaNo)) {
                throw new IllegalArgumentException("RMA is required");
            }
            this.rmaNo = rmaNo;
            status = RETURN_PENDING;
            events.add(OmsEvent.of("ReturnRequested", afterSaleNo, rmaNo));
        } else if (type == Type.REFUND_ONLY) {
            status = REFUND_PENDING;
        } else {
            status = RESHIP_PENDING;
        }
        version++;
        events.add(OmsEvent.of("ReverseAfterSaleApproved", afterSaleNo, type.name()));
    }

    /**
     * 处理当前类型职责中的操作 {@code markReturnReceived}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void markReturnReceived(long expectedVersion) {
        require(RETURN_PENDING, expectedVersion, "return is not pending");
        status = INSPECTION_PENDING;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code inspect}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param received 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param accepted 业务处理参数或成员，类型为 {@code BigDecimal}
     * @param unmatched 业务处理参数或成员，类型为 {@code boolean}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void inspect(BigDecimal received, BigDecimal accepted, boolean unmatched, long expectedVersion) {
        if (status != RETURN_PENDING && status != INSPECTION_PENDING) {
            throw new IllegalStateException("return is not awaiting inspection");
        }
        requireVersion(expectedVersion);
        if (!positive(received) || received.compareTo(applyQty) > 0 || accepted == null || accepted.signum() < 0 || accepted.compareTo(received) > 0) {
            throw new IllegalArgumentException("return inspection quantities are invalid");
        }
        receivedQty = received;
        acceptedQty = accepted;
        status = unmatched ? EXCEPTION_PENDING : type == Type.RETURN_REFUND ? REFUND_PENDING : RESHIP_PENDING;
        version++;
        validateQuantities();
        events.add(OmsEvent.of("ReturnInspectionAccepted", afterSaleNo, accepted.toPlainString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code requestRefund}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void requestRefund(long expectedVersion) {
        require(REFUND_PENDING, expectedVersion, "refund is not pending");
        if (refundAmount.signum() <= 0) {
            throw new IllegalStateException("refund amount is zero");
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
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void markRefunded(BigDecimal amount, long expectedVersion) {
        require(REFUND_REQUESTED, expectedVersion, "refund is not requested");
        if (!positive(amount) || amount.compareTo(refundAmount) > 0) {
            throw new IllegalArgumentException("refund amount exceeds request");
        }
        refundedAmount = amount;
        status = COMPLETED;
        version++;
        events.add(OmsEvent.of("ReverseAfterSaleCompleted", afterSaleNo, "REFUND"));
    }

    /**
     * 处理当前类型职责中的操作 {@code requestReship}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void requestReship(long expectedVersion) {
        require(RESHIP_PENDING, expectedVersion, "reship is not pending");
        status = RESHIP_REQUESTED;
        version++;
        events.add(OmsEvent.of("ReshipRequested", afterSaleNo, acceptedQty.signum() > 0 ? acceptedQty.toPlainString() : applyQty.toPlainString()));
    }

    /**
     * 处理当前类型职责中的操作 {@code markReshipCreated}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void markReshipCreated(long expectedVersion) {
        require(RESHIP_REQUESTED, expectedVersion, "reship is not requested");
        status = COMPLETED;
        version++;
        events.add(OmsEvent.of("ReverseAfterSaleCompleted", afterSaleNo, "RESHIP"));
    }

    /**
     * 查询并返回 {@code require}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expectedStatus 生命周期状态，类型为 {@code int}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    private void require(int expectedStatus, long expectedVersion, String message) {
        if (status != expectedStatus) {
            throw new IllegalStateException(message);
        }
        requireVersion(expectedVersion);
    }

    /**
     * 查询并返回 {@code requireVersion}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param expected 业务处理参数或成员，类型为 {@code long}
     */
    private void requireVersion(long expected) {
        if (version != expected) {
            throw new IllegalStateException("reverse after-sale version conflict");
        }
    }

    /**
     * 校验业务约束 {@code validateQuantities}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     */
    private void validateQuantities() {
        if (receivedQty.compareTo(applyQty) > 0 || acceptedQty.compareTo(receivedQty) > 0) {
            throw new IllegalArgumentException("reverse after-sale quantities are not conserved");
        }
    }

    /**
     * 查询并返回 {@code requiresReturn}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param type 业务处理参数或成员，类型为 {@code Type}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean requiresReturn(Type type) {
        return type == Type.RETURN_REFUND || type == Type.EXCHANGE;
    }

    /**
     * 处理当前类型职责中的操作 {@code positive}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean positive(BigDecimal v) {
        return v != null && v.signum() > 0;
    }

    /**
     * 处理当前类型职责中的操作 {@code zero}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code BigDecimal}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    private static BigDecimal zero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 处理当前类型职责中的操作 {@code blank}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param v 业务处理参数或成员，类型为 {@code String}
     * @return 条件成立或操作被接受时为 {@code true}，否则为 {@code false}
     */
    private static boolean blank(String v) {
        return v == null || v.isBlank();
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
     * 处理当前类型职责中的操作 {@code type}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Type}
     */
    public Type type() {
        return type;
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
     * 处理当前类型职责中的操作 {@code ownerId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long ownerId() {
        return ownerId;
    }

    /**
     * 处理当前类型职责中的操作 {@code sku}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String sku() {
        return sku;
    }

    /**
     * 执行命令 {@code applyQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal applyQty() {
        return applyQty;
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
     * 处理当前类型职责中的操作 {@code returnWarehouseId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long returnWarehouseId() {
        return returnWarehouseId;
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
     * 处理当前类型职责中的操作 {@code rmaNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String rmaNo() {
        return rmaNo;
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
     * 处理当前类型职责中的操作 {@code acceptedQty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal acceptedQty() {
        return acceptedQty;
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
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int status() {
        return status;
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
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<OmsEvent>}
     */
    public List<OmsEvent> pullEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }
}
