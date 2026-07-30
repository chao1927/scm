package com.chaobo.scm.mdm.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeRuleAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class CodeRuleAggregate {

    /**
     * DRAFT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DRAFT = 1;

    /**
     * ENABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int ENABLED = 2;

    /**
     * DISABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DISABLED = 9;

    /**
     * ruleCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String ruleCode;

    /**
     * typeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String typeCode;

    /**
     * prefix（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String prefix;

    /**
     * serialLength（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int serialLength;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * currentSerial（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private long currentSerial;

    /**
     * version（类型：{@code long}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private long version;

    /**
     * events（类型：{@code List<MdmEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<MdmEvent> events = new ArrayList<>();

    /**
     * 创建 CodeRuleAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param prefix 业务处理参数或成员，类型为 {@code String}
     * @param serialLength 业务处理参数或成员，类型为 {@code int}
     * @param status 生命周期状态，类型为 {@code int}
     * @param currentSerial 业务处理参数或成员，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private CodeRuleAggregate(String ruleCode, String typeCode, String prefix, int serialLength, int status, long currentSerial, long version) {
        if (blank(ruleCode) || blank(typeCode) || blank(prefix)) {
            throw new IllegalArgumentException("ruleCode, typeCode and prefix are required");
        }
        if (serialLength < BUSINESS_VALUE_3 || serialLength > BUSINESS_VALUE_12) {
            throw new IllegalArgumentException("serialLength must be between 3 and 12");
        }
        this.ruleCode = ruleCode;
        this.typeCode = typeCode;
        this.prefix = prefix;
        this.serialLength = serialLength;
        this.status = status;
        this.currentSerial = currentSerial;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param prefix 业务处理参数或成员，类型为 {@code String}
     * @param serialLength 业务处理参数或成员，类型为 {@code int}
     * @return 执行命令的结果，类型为 {@code CodeRuleAggregate}
     */
    public static CodeRuleAggregate create(String ruleCode, String typeCode, String prefix, int serialLength) {
        CodeRuleAggregate aggregate = new CodeRuleAggregate(ruleCode, typeCode, prefix, serialLength, DRAFT, 0, 1);
        aggregate.events.add(MdmEvent.of("CodeRuleCreated", ruleCode, typeCode));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ruleCode 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param prefix 业务处理参数或成员，类型为 {@code String}
     * @param serialLength 业务处理参数或成员，类型为 {@code int}
     * @param status 生命周期状态，类型为 {@code int}
     * @param currentSerial 业务处理参数或成员，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code CodeRuleAggregate}
     */
    public static CodeRuleAggregate restore(String ruleCode, String typeCode, String prefix, int serialLength, int status, long currentSerial, long version) {
        return new CodeRuleAggregate(ruleCode, typeCode, prefix, serialLength, status, currentSerial, version);
    }

    /**
     * 执行命令 {@code enable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void enable() {
        if (status == ENABLED) {
            return;
        }
        status = ENABLED;
        version++;
        events.add(MdmEvent.of("CodeRuleEnabled", ruleCode, typeCode));
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param reason 业务处理参数或成员，类型为 {@code String}
     */
    public void disable(String reason) {
        if (blank(reason)) {
            throw new IllegalArgumentException("disable reason is required");
        }
        status = DISABLED;
        version++;
        events.add(MdmEvent.of("CodeRuleDisabled", ruleCode, reason));
    }

    /**
     * 处理当前类型职责中的操作 {@code generateCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String generateCode() {
        if (status != ENABLED) {
            throw new IllegalStateException("code rule is not enabled");
        }
        currentSerial++;
        version++;
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String code = prefix + date + String.format("%0" + serialLength + "d", currentSerial);
        events.add(MdmEvent.of("MasterDataCodeGenerated", ruleCode, code));
        return code;
    }

    /**
     * 处理当前类型职责中的操作 {@code pullEvents}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<MdmEvent>}
     */
    public List<MdmEvent> pullEvents() {
        List<MdmEvent> copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    /**
     * 处理当前类型职责中的操作 {@code ruleCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String ruleCode() {
        return ruleCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code typeCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String typeCode() {
        return typeCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code prefix}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String prefix() {
        return prefix;
    }

    /**
     * 处理当前类型职责中的操作 {@code serialLength}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int serialLength() {
        return serialLength;
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
     * 处理当前类型职责中的操作 {@code currentSerial}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long currentSerial() {
        return currentSerial;
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

    /**
     * 业务常量 {@code BUSINESS_VALUE_12}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int BUSINESS_VALUE_12 = 12;

    /**
     * 业务常量 {@code BUSINESS_VALUE_3}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int BUSINESS_VALUE_3 = 3;
}
