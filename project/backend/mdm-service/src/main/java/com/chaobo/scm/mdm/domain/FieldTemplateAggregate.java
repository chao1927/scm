package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * FieldTemplateAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class FieldTemplateAggregate {

    /**
     * DRAFT（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DRAFT = 1;

    /**
     * PUBLISHED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int PUBLISHED = 2;

    /**
     * DISABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DISABLED = 9;

    /**
     * templateCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String templateCode;

    /**
     * typeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String typeCode;

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
     * fields（类型：{@code List<FieldDefinition>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<FieldDefinition> fields;

    /**
     * events（类型：{@code List<MdmEvent>}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final List<MdmEvent> events = new ArrayList<>();

    /**
     * 创建 FieldTemplateAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fields 业务处理参数或成员，类型为 {@code List<FieldDefinition>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private FieldTemplateAggregate(String templateCode, String typeCode, List<FieldDefinition> fields, int status, long version) {
        if (blank(templateCode) || blank(typeCode)) {
            throw new IllegalArgumentException("templateCode and typeCode are required");
        }
        ensureUniqueFields(fields);
        this.templateCode = templateCode;
        this.typeCode = typeCode;
        this.fields = new ArrayList<>(fields);
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fields 业务处理参数或成员，类型为 {@code List<FieldDefinition>}
     * @return 执行命令的结果，类型为 {@code FieldTemplateAggregate}
     */
    public static FieldTemplateAggregate create(String templateCode, String typeCode, List<FieldDefinition> fields) {
        FieldTemplateAggregate aggregate = new FieldTemplateAggregate(templateCode, typeCode, fields, DRAFT, 1);
        aggregate.events.add(MdmEvent.of("FieldTemplateCreated", templateCode, typeCode));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param templateCode 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param fields 业务处理参数或成员，类型为 {@code List<FieldDefinition>}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code FieldTemplateAggregate}
     */
    public static FieldTemplateAggregate restore(String templateCode, String typeCode, List<FieldDefinition> fields, int status, long version) {
        return new FieldTemplateAggregate(templateCode, typeCode, fields, status, version);
    }

    /**
     * 执行命令 {@code publish}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void publish() {
        if (fields.isEmpty()) {
            throw new IllegalStateException("field template must contain fields before publish");
        }
        if (status == DISABLED) {
            throw new IllegalStateException("disabled template cannot publish");
        }
        status = PUBLISHED;
        version++;
        events.add(MdmEvent.of("FieldTemplatePublished", templateCode, typeCode));
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
        events.add(MdmEvent.of("FieldTemplateDisabled", templateCode, reason));
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
     * 处理当前类型职责中的操作 {@code templateCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String templateCode() {
        return templateCode;
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
     * 处理当前类型职责中的操作 {@code fields}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code List<FieldDefinition>}
     */
    public List<FieldDefinition> fields() {
        return List.copyOf(fields);
    }

    /**
     * 校验业务约束 {@code ensureUniqueFields}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param fields 业务处理参数或成员，类型为 {@code List<FieldDefinition>}
     */
    private static void ensureUniqueFields(List<FieldDefinition> fields) {
        Set<String> codes = new HashSet<>();
        for (FieldDefinition field : fields) {
            if (blank(field.fieldCode()) || blank(field.fieldName()) || blank(field.fieldType())) {
                throw new IllegalArgumentException("field code, name and type are required");
            }
            if (!codes.add(field.fieldCode())) {
                throw new IllegalArgumentException("duplicate field code: " + field.fieldCode());
            }
        }
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
     * FieldDefinition。
     *
     * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为不可变数据载体集中表达一组相关业务参数或查询结果。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
     *
     * @author SCM Team
     * @since 0.1.0
     */
    public record FieldDefinition(String fieldCode, String fieldName, String fieldType, boolean required, boolean uniqueFlag, boolean keyField) {
    }
}
