package com.chaobo.scm.tms.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * WaybillAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class WaybillAggregate {

    /**
     * CREATED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CREATED = 1;

    /**
     * VOIDED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int VOIDED = 2;

    /** 承运商已揽收。 */
    public static final int PICKED_UP = 3;

    /** 运输途中。 */
    public static final int IN_TRANSIT = 4;

    /** 已到达目的地。 */
    public static final int ARRIVED = 5;

    /** 已签收终态。 */
    public static final int SIGNED = 6;

    /** 已拒收终态。 */
    public static final int REJECTED = 7;

    /** 部分签收终态。 */
    public static final int PARTIAL_SIGNED = 8;

    /**
     * waybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String waybillNo;

    /**
     * taskNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String taskNo;

    /**
     * carrierCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String carrierCode;

    /**
     * carrierName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String carrierName;

    /**
     * carrierWaybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String carrierWaybillNo;

    /**
     * logisticsProductCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String logisticsProductCode;

    /**
     * receiptPayload（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String receiptPayload;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * voidReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private String voidReason;

    /**
     * approvalNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String approvalNo;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<TmsEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<TmsEvent> events = new ArrayList<>();

    /**
     * 创建 WaybillAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param carrierName 业务处理参数或成员，类型为 {@code String}
     * @param carrierWaybillNo 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param receiptPayload 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param voidReason 业务或技术标识，类型为 {@code String}
     * @param approvalNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private WaybillAggregate(String waybillNo, String taskNo, String carrierCode, String carrierName, String carrierWaybillNo, String logisticsProductCode, String receiptPayload, int status, String voidReason, String approvalNo, long version) {
        if (blank(waybillNo) || blank(taskNo) || blank(carrierCode) || blank(carrierName) || blank(carrierWaybillNo) || blank(logisticsProductCode)) {
            throw new IllegalArgumentException("waybill references and carrier receipt are required");
        }
        this.waybillNo = waybillNo;
        this.taskNo = taskNo;
        this.carrierCode = carrierCode;
        this.carrierName = carrierName;
        this.carrierWaybillNo = carrierWaybillNo;
        this.logisticsProductCode = logisticsProductCode;
        this.receiptPayload = receiptPayload;
        this.status = status;
        this.voidReason = voidReason;
        this.approvalNo = approvalNo;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param carrierName 业务处理参数或成员，类型为 {@code String}
     * @param carrierWaybillNo 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param receiptPayload 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code WaybillAggregate}
     */
    public static WaybillAggregate create(String waybillNo, String taskNo, String carrierCode, String carrierName, String carrierWaybillNo, String logisticsProductCode, String receiptPayload) {
        WaybillAggregate aggregate = new WaybillAggregate(waybillNo, taskNo, carrierCode, carrierName, carrierWaybillNo, logisticsProductCode, receiptPayload, CREATED, null, null, 1);
        aggregate.events.add(TmsEvent.of("WaybillCreated", waybillNo, taskNo + "|" + carrierCode + "|" + carrierWaybillNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param taskNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param carrierName 业务处理参数或成员，类型为 {@code String}
     * @param carrierWaybillNo 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param receiptPayload 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param voidReason 业务或技术标识，类型为 {@code String}
     * @param approvalNo 可追踪业务编码，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code WaybillAggregate}
     */
    public static WaybillAggregate restore(String waybillNo, String taskNo, String carrierCode, String carrierName, String carrierWaybillNo, String logisticsProductCode, String receiptPayload, int status, String voidReason, String approvalNo, long version) {
        return new WaybillAggregate(waybillNo, taskNo, carrierCode, carrierName, carrierWaybillNo, logisticsProductCode, receiptPayload, status, voidReason, approvalNo, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code voidWaybill}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     * @param approvalNo 可追踪业务编码，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void voidWaybill(String reason, String approvalNo, long expectedVersion) {
        if (status != CREATED) {
            throw new IllegalStateException("waybill is not voidable");
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("waybill version conflict");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("void reason is required");
        }
        status = VOIDED;
        voidReason = reason;
        this.approvalNo = approvalNo;
        version++;
        events.add(TmsEvent.of("WaybillVoided", waybillNo, reason));
    }

    /**
     * 根据标准轨迹节点单向推进运单状态。
     *
     * <p>乱序轨迹只保存轨迹事实，不允许状态回退；终态和作废态不能被后续轨迹覆盖。
     *
     * @param nodeCode TMS 标准节点
     */
    public void advanceFromTrack(String nodeCode) {
        if (status == VOIDED || isTerminal(status)) {
            return;
        }
        int target = switch (nodeCode) {
            case "PICKED_UP" -> PICKED_UP;
            case "IN_TRANSIT" -> IN_TRANSIT;
            case "ARRIVED" -> ARRIVED;
            default -> status;
        };
        if (target > status) {
            status = target;
            version++;
        }
    }

    /**
     * 根据签收结果推进运单终态。
     *
     * @param receiptResult 签收聚合稳定结果值
     */
    public void advanceFromReceipt(int receiptResult) {
        if (status == VOIDED) {
            throw new IllegalStateException("voided waybill cannot receive receipt");
        }
        int target = switch (receiptResult) {
            case 1 -> SIGNED;
            case 2 -> REJECTED;
            case 3 -> PARTIAL_SIGNED;
            default -> throw new IllegalArgumentException("unsupported receipt result");
        };
        if (isTerminal(status)) {
            if (status != target) {
                throw new IllegalStateException("waybill receipt result conflicts with terminal state");
            }
            return;
        }
        status = target;
        version++;
    }

    private static boolean isTerminal(int current) {
        return current == SIGNED || current == REJECTED || current == PARTIAL_SIGNED;
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
     * 处理当前类型职责中的操作 {@code waybillNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String waybillNo() {
        return waybillNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code taskNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String taskNo() {
        return taskNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code carrierCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierCode() {
        return carrierCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code carrierName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierName() {
        return carrierName;
    }

    /**
     * 处理当前类型职责中的操作 {@code carrierWaybillNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierWaybillNo() {
        return carrierWaybillNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code logisticsProductCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String logisticsProductCode() {
        return logisticsProductCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code receiptPayload}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String receiptPayload() {
        return receiptPayload;
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
     * 处理当前类型职责中的操作 {@code voidReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String voidReason() {
        return voidReason;
    }

    /**
     * 处理当前类型职责中的操作 {@code approvalNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String approvalNo() {
        return approvalNo;
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
