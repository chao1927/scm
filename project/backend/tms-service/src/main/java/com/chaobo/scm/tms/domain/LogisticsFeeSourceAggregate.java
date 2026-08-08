package com.chaobo.scm.tms.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * LogisticsFeeSourceAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class LogisticsFeeSourceAggregate {

    /**
     * PENDING_PUSH（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PENDING_PUSH = 1;

    /**
     * PUSHED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PUSHED = 2;

    /**
     * PUSH_FAILED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PUSH_FAILED = 3;

    /** 已作废，不能再重算或推送。 */
    public static final int VOIDED = 4;

    /**
     * feeSourceNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String feeSourceNo;

    /**
     * waybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String waybillNo;

    /**
     * carrierCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String carrierCode;

    /**
     * logisticsProductCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String logisticsProductCode;

    /**
     * feeItemCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String feeItemCode;

    /**
     * amount（类型：{@code BigDecimal}）。
     *
     * <p>保存当前对象所需的金额或计费值；其具体生命周期由所属对象统一管理。
     */
    private BigDecimal amount;

    /**
     * currency（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String currency;

    /**
     * billingPeriod（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String billingPeriod;

    /**
     * responsibleParty（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String responsibleParty;

    /**
     * pushStatus（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int pushStatus;

    /**
     * bmsReceiveNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String bmsReceiveNo;

    /**
     * failureReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String failureReason;

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
     * 创建 LogisticsFeeSourceAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param feeSourceNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param feeItemCode 可追踪业务编码，类型为 {@code String}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @param pushStatus 生命周期状态，类型为 {@code int}
     * @param bmsReceiveNo 可追踪业务编码，类型为 {@code String}
     * @param failureReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private LogisticsFeeSourceAggregate(String feeSourceNo, String waybillNo, String carrierCode, String logisticsProductCode, String feeItemCode, BigDecimal amount, String currency, String billingPeriod, String responsibleParty, int pushStatus, String bmsReceiveNo, String failureReason, long version) {
        if (blank(feeSourceNo) || blank(waybillNo) || blank(carrierCode) || blank(logisticsProductCode) || blank(feeItemCode) || blank(currency) || blank(billingPeriod) || blank(responsibleParty)) {
            throw new IllegalArgumentException("fee source references are required");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("fee source amount cannot be negative");
        }
        this.feeSourceNo = feeSourceNo;
        this.waybillNo = waybillNo;
        this.carrierCode = carrierCode;
        this.logisticsProductCode = logisticsProductCode;
        this.feeItemCode = feeItemCode;
        this.amount = amount;
        this.currency = currency;
        this.billingPeriod = billingPeriod;
        this.responsibleParty = responsibleParty;
        this.pushStatus = pushStatus;
        this.bmsReceiveNo = bmsReceiveNo;
        this.failureReason = failureReason;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param feeSourceNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param feeItemCode 可追踪业务编码，类型为 {@code String}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LogisticsFeeSourceAggregate}
     */
    public static LogisticsFeeSourceAggregate generate(String feeSourceNo, String waybillNo, String carrierCode, String logisticsProductCode, String feeItemCode, BigDecimal amount, String currency, String billingPeriod, String responsibleParty) {
        LogisticsFeeSourceAggregate aggregate = new LogisticsFeeSourceAggregate(feeSourceNo, waybillNo, carrierCode, logisticsProductCode, feeItemCode, amount, currency, billingPeriod, responsibleParty, PENDING_PUSH, null, null, 1);
        aggregate.events.add(TmsEvent.of(
            "LogisticsFeeSourceGenerated", feeSourceNo, aggregate.feePayload(null)));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param feeSourceNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param carrierCode 可追踪业务编码，类型为 {@code String}
     * @param logisticsProductCode 可追踪业务编码，类型为 {@code String}
     * @param feeItemCode 可追踪业务编码，类型为 {@code String}
     * @param amount 金额或计费值，类型为 {@code BigDecimal}
     * @param currency 业务处理参数或成员，类型为 {@code String}
     * @param billingPeriod 业务处理参数或成员，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @param pushStatus 生命周期状态，类型为 {@code int}
     * @param bmsReceiveNo 可追踪业务编码，类型为 {@code String}
     * @param failureReason 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LogisticsFeeSourceAggregate}
     */
    public static LogisticsFeeSourceAggregate restore(String feeSourceNo, String waybillNo, String carrierCode, String logisticsProductCode, String feeItemCode, BigDecimal amount, String currency, String billingPeriod, String responsibleParty, int pushStatus, String bmsReceiveNo, String failureReason, long version) {
        return new LogisticsFeeSourceAggregate(feeSourceNo, waybillNo, carrierCode, logisticsProductCode, feeItemCode, amount, currency, billingPeriod, responsibleParty, pushStatus, bmsReceiveNo, failureReason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code pushToBms}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param bmsReceiveNo 可追踪业务编码，类型为 {@code String}
     */
    public void pushToBms(String bmsReceiveNo) {
        if (pushStatus == PUSHED) {
            return;
        }
        if (blank(bmsReceiveNo)) {
            throw new IllegalArgumentException("BMS receive number is required");
        }
        pushStatus = PUSHED;
        this.bmsReceiveNo = bmsReceiveNo;
        failureReason = null;
        version++;
        events.add(TmsEvent.of(
            "LogisticsFeeSourcePushed", feeSourceNo, feePayload(bmsReceiveNo)));
    }

    /**
     * 费用事件必须携带 BMS 可直接消费的稳定字段，避免消费者猜测管道分隔字符串。
     */
    private String feePayload(String receiveNo) {
        String receiveField = receiveNo == null ? "null" : '"' + json(receiveNo) + '"';
        return "{\"feeSourceNo\":\"" + json(feeSourceNo)
            + "\",\"sourceOrderNo\":\"" + json(waybillNo)
            + "\",\"billingObjectCode\":\"" + json(carrierCode)
            + "\",\"feeType\":\"" + json(feeItemCode)
            + "\",\"quantity\":1,\"billingPeriod\":\"" + json(billingPeriod)
            + "\",\"currency\":\"" + json(currency)
            + "\",\"amountSnapshot\":{\"amount\":" + amount.toPlainString()
            + "},\"bmsReceiveNo\":" + receiveField + '}';
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\r", "\\r").replace("\n", "\\n");
    }

    /**
     * 处理当前类型职责中的操作 {@code markPushFailed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void markPushFailed(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("push failure reason is required");
        }
        pushStatus = PUSH_FAILED;
        failureReason = reason;
        version++;
        events.add(TmsEvent.of("LogisticsFeeSourcePushFailed", feeSourceNo, reason));
    }

    /** 在推送 BMS 前修正来源金额。 */
    public void recalculate(BigDecimal newAmount, String reason) {
        if (pushStatus == PUSHED || pushStatus == VOIDED) {
            throw new IllegalStateException("pushed or voided fee source cannot be recalculated");
        }
        if (newAmount == null || newAmount.signum() < 0) {
            throw new IllegalArgumentException("recalculated amount must not be negative");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("recalculation reason is required");
        }
        amount = newAmount;
        pushStatus = PENDING_PUSH;
        failureReason = null;
        version++;
        events.add(TmsEvent.of(
            "LogisticsFeeSourceRecalculated", feeSourceNo, feePayload(null)));
    }

    /** 作废尚未推送的费用来源，保留原金额用于审计。 */
    public void voidSource(String reason) {
        if (pushStatus == VOIDED) {
            return;
        }
        if (pushStatus == PUSHED) {
            throw new IllegalStateException("pushed fee source cannot be voided");
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("fee source void reason is required");
        }
        pushStatus = VOIDED;
        failureReason = reason;
        version++;
        events.add(TmsEvent.of("LogisticsFeeSourceVoided", feeSourceNo, reason));
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
     * 处理当前类型职责中的操作 {@code feeSourceNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String feeSourceNo() {
        return feeSourceNo;
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
     * 处理当前类型职责中的操作 {@code carrierCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String carrierCode() {
        return carrierCode;
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
     * 处理当前类型职责中的操作 {@code feeItemCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String feeItemCode() {
        return feeItemCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code amount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code BigDecimal}
     */
    public BigDecimal amount() {
        return amount;
    }

    /**
     * 处理当前类型职责中的操作 {@code currency}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String currency() {
        return currency;
    }

    /**
     * 处理当前类型职责中的操作 {@code billingPeriod}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String billingPeriod() {
        return billingPeriod;
    }

    /**
     * 处理当前类型职责中的操作 {@code responsibleParty}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String responsibleParty() {
        return responsibleParty;
    }

    /**
     * 处理当前类型职责中的操作 {@code pushStatus}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int pushStatus() {
        return pushStatus;
    }

    /**
     * 处理当前类型职责中的操作 {@code bmsReceiveNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String bmsReceiveNo() {
        return bmsReceiveNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code failureReason}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String failureReason() {
        return failureReason;
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
