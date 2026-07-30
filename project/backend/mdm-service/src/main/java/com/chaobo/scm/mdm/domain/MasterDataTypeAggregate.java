package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * MasterDataTypeAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class MasterDataTypeAggregate {

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
     * typeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String typeCode;

    /**
     * typeName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String typeName;

    /**
     * domainCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private String domainCode;

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
     * events（类型：{@code List<MdmEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<MdmEvent> events = new ArrayList<>();

    /**
     * 创建 MasterDataTypeAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param typeName 业务处理参数或成员，类型为 {@code String}
     * @param domainCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private MasterDataTypeAggregate(String typeCode, String typeName, String domainCode, int status, long version) {
        if (blank(typeCode) || blank(typeName)) {
            throw new IllegalArgumentException("typeCode and typeName are required");
        }
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.domainCode = blank(domainCode) ? "SCM" : domainCode;
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param typeName 业务处理参数或成员，类型为 {@code String}
     * @param domainCode 可追踪业务编码，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code MasterDataTypeAggregate}
     */
    public static MasterDataTypeAggregate create(String typeCode, String typeName, String domainCode) {
        MasterDataTypeAggregate aggregate = new MasterDataTypeAggregate(typeCode, typeName, domainCode, DRAFT, 1);
        aggregate.events.add(MdmEvent.of("MasterDataTypeCreated", typeCode, typeName));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param typeName 业务处理参数或成员，类型为 {@code String}
     * @param domainCode 可追踪业务编码，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code MasterDataTypeAggregate}
     */
    public static MasterDataTypeAggregate restore(String typeCode, String typeName, String domainCode, int status, long version) {
        return new MasterDataTypeAggregate(typeCode, typeName, domainCode, status, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code rename}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param typeName 业务处理参数或成员，类型为 {@code String}
     */
    public void rename(String typeName) {
        if (status == DISABLED) {
            throw new IllegalStateException("disabled type cannot be changed");
        }
        if (blank(typeName)) {
            throw new IllegalArgumentException("typeName is required");
        }
        this.typeName = typeName;
        this.version++;
        events.add(MdmEvent.of("MasterDataTypeChanged", typeCode, typeName));
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
        events.add(MdmEvent.of("MasterDataTypeEnabled", typeCode, typeName));
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
        if (status == DISABLED) {
            return;
        }
        status = DISABLED;
        version++;
        events.add(MdmEvent.of("MasterDataTypeDisabled", typeCode, reason));
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
     * 处理当前类型职责中的操作 {@code typeCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String typeCode() {
        return typeCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code typeName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String typeName() {
        return typeName;
    }

    /**
     * 处理当前类型职责中的操作 {@code domainCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String domainCode() {
        return domainCode;
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
