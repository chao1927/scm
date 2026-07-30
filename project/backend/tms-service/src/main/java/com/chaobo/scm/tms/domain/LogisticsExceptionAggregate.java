package com.chaobo.scm.tms.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * LogisticsExceptionAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class LogisticsExceptionAggregate {

    /**
     * OPEN（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int OPEN = 1;

    /**
     * CLOSED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CLOSED = 2;

    /**
     * exceptionNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String exceptionNo;

    /**
     * waybillNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String waybillNo;

    /**
     * exceptionType（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String exceptionType;

    /**
     * level（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String level;

    /**
     * description（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String description;

    /**
     * responsibleParty（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String responsibleParty;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * closeResult（类型：{@code String}）。
     *
     * <p>保存当前对象所需的处理结果；其具体生命周期由所属对象统一管理。
     */
    private String closeResult;

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
     * 创建 LogisticsExceptionAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param exceptionType 业务处理参数或成员，类型为 {@code String}
     * @param level 业务处理参数或成员，类型为 {@code String}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param closeResult 处理结果，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private LogisticsExceptionAggregate(String exceptionNo, String waybillNo, String exceptionType, String level, String description, String responsibleParty, int status, String closeResult, long version) {
        if (blank(exceptionNo) || blank(waybillNo) || blank(exceptionType) || blank(level) || blank(description)) {
            throw new IllegalArgumentException("logistics exception references are required");
        }
        this.exceptionNo = exceptionNo;
        this.waybillNo = waybillNo;
        this.exceptionType = exceptionType;
        this.level = level;
        this.description = description;
        this.responsibleParty = responsibleParty;
        this.status = status;
        this.closeResult = closeResult;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code register}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param exceptionType 业务处理参数或成员，类型为 {@code String}
     * @param level 业务处理参数或成员，类型为 {@code String}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LogisticsExceptionAggregate}
     */
    public static LogisticsExceptionAggregate register(String exceptionNo, String waybillNo, String exceptionType, String level, String description, String responsibleParty) {
        LogisticsExceptionAggregate aggregate = new LogisticsExceptionAggregate(exceptionNo, waybillNo, exceptionType, level, description, responsibleParty, OPEN, null, 1);
        aggregate.events.add(TmsEvent.of("LogisticsExceptionRegistered", exceptionNo, waybillNo + "|" + exceptionType));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param exceptionNo 可追踪业务编码，类型为 {@code String}
     * @param waybillNo 可追踪业务编码，类型为 {@code String}
     * @param exceptionType 业务处理参数或成员，类型为 {@code String}
     * @param level 业务处理参数或成员，类型为 {@code String}
     * @param description 业务处理参数或成员，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param closeResult 处理结果，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code LogisticsExceptionAggregate}
     */
    public static LogisticsExceptionAggregate restore(String exceptionNo, String waybillNo, String exceptionType, String level, String description, String responsibleParty, int status, String closeResult, long version) {
        return new LogisticsExceptionAggregate(exceptionNo, waybillNo, exceptionType, level, description, responsibleParty, status, closeResult, version);
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param closeResult 处理结果，类型为 {@code String}
     * @param responsibleParty 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void close(String closeResult, String responsibleParty, long expectedVersion) {
        if (status != OPEN) {
            throw new IllegalStateException("logistics exception is not open");
        }
        if (version != expectedVersion) {
            throw new IllegalStateException("logistics exception version conflict");
        }
        if (blank(closeResult) || blank(responsibleParty)) {
            throw new IllegalArgumentException("close result and responsible party are required");
        }
        this.closeResult = closeResult;
        this.responsibleParty = responsibleParty;
        status = CLOSED;
        version++;
        events.add(TmsEvent.of("LogisticsExceptionClosed", exceptionNo, closeResult));
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
     * 处理当前类型职责中的操作 {@code exceptionNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String exceptionNo() {
        return exceptionNo;
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
     * 处理当前类型职责中的操作 {@code exceptionType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String exceptionType() {
        return exceptionType;
    }

    /**
     * 处理当前类型职责中的操作 {@code level}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String level() {
        return level;
    }

    /**
     * 处理当前类型职责中的操作 {@code description}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String description() {
        return description;
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
     * 处理当前类型职责中的操作 {@code status}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int status() {
        return status;
    }

    /**
     * 执行命令 {@code closeResult}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 执行命令的结果，类型为 {@code String}
     */
    public String closeResult() {
        return closeResult;
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
