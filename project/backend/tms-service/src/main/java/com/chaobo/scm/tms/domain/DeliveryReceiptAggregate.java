package com.chaobo.scm.tms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DeliveryReceiptAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class DeliveryReceiptAggregate {

    /**
     * SIGNED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int SIGNED = 1;

    /**
     * REJECTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int REJECTED = 2;

    /**
     * PARTIAL_SIGNED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PARTIAL_SIGNED = 3;

    /**
     * receiptNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String receiptNo;

    /**
     * waybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String waybillNo;

    /**
     * result（类型：{@code int}）。
     *
     * <p>保存当前对象所需的处理结果；其具体生命周期由所属对象统一管理。
     */
    private final int result;

    /**
     * signedBy（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String signedBy;

    /**
     * signedAt（类型：{@code LocalDateTime}）。
     *
     * <p>保存当前对象所需的业务时间；其具体生命周期由所属对象统一管理。
     */
    private final LocalDateTime signedAt;

    /**
     * rejectReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String rejectReason;

    /**
     * proofUrl（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String proofUrl;

    /**
     * events（类型：{@code List<TmsEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<TmsEvent> events = new ArrayList<>();

    /**
     * 创建 DeliveryReceiptAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param result 处理结果，类型为 {@code int}
     * @param signedBy 业务处理参数或成员，类型为 {@code String}
     * @param signedAt 业务时间，类型为 {@code LocalDateTime}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param proofUrl 业务处理参数或成员，类型为 {@code String}
     */
    private DeliveryReceiptAggregate(String receiptNo, String waybillNo, int result, String signedBy, LocalDateTime signedAt, String rejectReason, String proofUrl) {
        if (blank(receiptNo) || blank(waybillNo) || signedAt == null) {
            throw new IllegalArgumentException("delivery receipt references are required");
        }
        if (result == SIGNED && blank(signedBy)) {
            throw new IllegalArgumentException("signed by is required");
        }
        if (result == REJECTED && blank(rejectReason)) {
            throw new IllegalArgumentException("reject reason is required");
        }
        if (!List.of(SIGNED, REJECTED, PARTIAL_SIGNED).contains(result)) {
            throw new IllegalArgumentException("unsupported receipt result");
        }
        this.receiptNo = receiptNo;
        this.waybillNo = waybillNo;
        this.result = result;
        this.signedBy = signedBy;
        this.signedAt = signedAt;
        this.rejectReason = rejectReason;
        this.proofUrl = proofUrl;
    }

    /**
     * 处理当前类型职责中的操作 {@code record}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param receiptNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param result 处理结果，类型为 {@code int}
     * @param signedBy 业务处理参数或成员，类型为 {@code String}
     * @param signedAt 业务时间，类型为 {@code LocalDateTime}
     * @param rejectReason 业务处理参数或成员，类型为 {@code String}
     * @param proofUrl 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code DeliveryReceiptAggregate}
     */
    public static DeliveryReceiptAggregate record(String receiptNo, String waybillNo, int result, String signedBy, LocalDateTime signedAt, String rejectReason, String proofUrl) {
        DeliveryReceiptAggregate aggregate = new DeliveryReceiptAggregate(receiptNo, waybillNo, result, signedBy, signedAt, rejectReason, proofUrl);
        String eventType = switch(result) {
            case SIGNED ->
                "TransportSigned";
            case REJECTED ->
                "TransportRejected";
            case PARTIAL_SIGNED ->
                "PartialSigned";
            default ->
                throw new IllegalArgumentException("unsupported receipt result");
        };
        aggregate.events.add(TmsEvent.of(eventType, waybillNo, receiptNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<TmsEvent>}
     */
    public List<TmsEvent> pullEvents() {
        List<TmsEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
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
     * 处理当前类型职责中的操作 {@code waybillNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String waybillNo() {
        return waybillNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code result}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int result() {
        return result;
    }

    /**
     * 处理当前类型职责中的操作 {@code signedBy}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String signedBy() {
        return signedBy;
    }

    /**
     * 处理当前类型职责中的操作 {@code signedAt}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LocalDateTime}
     */
    public LocalDateTime signedAt() {
        return signedAt;
    }

    /**
     * 执行命令 {@code rejectReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String rejectReason() {
        return rejectReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code proofUrl}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String proofUrl() {
        return proofUrl;
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
