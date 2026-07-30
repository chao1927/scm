package com.chaobo.scm.tms.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * ShippingLabelAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class ShippingLabelAggregate {

    /**
     * GENERATED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int GENERATED = 1;

    /**
     * PRINTED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PRINTED = 2;

    /**
     * VOIDED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int VOIDED = 3;

    /**
     * labelNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String labelNo;

    /**
     * waybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String waybillNo;

    /**
     * packageNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String packageNo;

    /**
     * templateVersion（类型：{@code String}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private final String templateVersion;

    /**
     * labelUrl（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String labelUrl;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * printCount（类型：{@code int}）。
     *
     * <p>保存当前对象所需的数量值；其具体生命周期由所属对象统一管理。
     */
    private int printCount;

    /**
     * lastPrintDevice（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String lastPrintDevice;

    /**
     * voidReason（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private String voidReason;

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
     * 创建 ShippingLabelAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param labelNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param packageNo 可追踪业务编码，类型为 {@code String}
     * @param templateVersion 乐观锁或契约版本，类型为 {@code String}
     * @param labelUrl 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param printCount 数量值，类型为 {@code int}
     * @param lastPrintDevice 业务处理参数或成员，类型为 {@code String}
     * @param voidReason 业务或技术标识，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private ShippingLabelAggregate(String labelNo, String waybillNo, String packageNo, String templateVersion, String labelUrl, int status, int printCount, String lastPrintDevice, String voidReason, long version) {
        if (blank(labelNo) || blank(waybillNo) || blank(packageNo) || blank(templateVersion) || blank(labelUrl)) {
            throw new IllegalArgumentException("shipping label references and file url are required");
        }
        this.labelNo = labelNo;
        this.waybillNo = waybillNo;
        this.packageNo = packageNo;
        this.templateVersion = templateVersion;
        this.labelUrl = labelUrl;
        this.status = status;
        this.printCount = printCount;
        this.lastPrintDevice = lastPrintDevice;
        this.voidReason = voidReason;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code generate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param labelNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param packageNo 可追踪业务编码，类型为 {@code String}
     * @param templateVersion 乐观锁或契约版本，类型为 {@code String}
     * @param labelUrl 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ShippingLabelAggregate}
     */
    public static ShippingLabelAggregate generate(String labelNo, String waybillNo, String packageNo, String templateVersion, String labelUrl) {
        ShippingLabelAggregate aggregate = new ShippingLabelAggregate(labelNo, waybillNo, packageNo, templateVersion, labelUrl, GENERATED, 0, null, null, 1);
        aggregate.events.add(TmsEvent.of("ShippingLabelGenerated", labelNo, waybillNo + "|" + packageNo));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param labelNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param packageNo 可追踪业务编码，类型为 {@code String}
     * @param templateVersion 乐观锁或契约版本，类型为 {@code String}
     * @param labelUrl 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param printCount 数量值，类型为 {@code int}
     * @param lastPrintDevice 业务处理参数或成员，类型为 {@code String}
     * @param voidReason 业务或技术标识，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ShippingLabelAggregate}
     */
    public static ShippingLabelAggregate restore(String labelNo, String waybillNo, String packageNo, String templateVersion, String labelUrl, int status, int printCount, String lastPrintDevice, String voidReason, long version) {
        return new ShippingLabelAggregate(labelNo, waybillNo, packageNo, templateVersion, labelUrl, status, printCount, lastPrintDevice, voidReason, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code print}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param deviceNo 可追踪业务编码，类型为 {@code String}
     */
    public void print(String deviceNo) {
        if (status == VOIDED) {
            throw new IllegalStateException("voided label cannot be printed");
        }
        if (blank(deviceNo)) {
            throw new IllegalArgumentException("print device is required");
        }
        status = PRINTED;
        printCount++;
        lastPrintDevice = deviceNo;
        version++;
        events.add(TmsEvent.of("ShippingLabelPrinted", labelNo, deviceNo + "|" + printCount));
    }

    /**
     * 处理当前类型职责中的操作 {@code voidLabel}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void voidLabel(String reason) {
        if (status == VOIDED) {
            return;
        }
        if (blank(reason)) {
            throw new IllegalArgumentException("label void reason is required");
        }
        status = VOIDED;
        voidReason = reason;
        version++;
        events.add(TmsEvent.of("ShippingLabelVoided", labelNo, reason));
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
     * 处理当前类型职责中的操作 {@code labelNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String labelNo() {
        return labelNo;
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
     * 处理当前类型职责中的操作 {@code packageNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String packageNo() {
        return packageNo;
    }

    /**
     * 处理当前类型职责中的操作 {@code templateVersion}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String templateVersion() {
        return templateVersion;
    }

    /**
     * 处理当前类型职责中的操作 {@code labelUrl}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String labelUrl() {
        return labelUrl;
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
     * 处理当前类型职责中的操作 {@code printCount}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int printCount() {
        return printCount;
    }

    /**
     * 处理当前类型职责中的操作 {@code lastPrintDevice}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String lastPrintDevice() {
        return lastPrintDevice;
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
