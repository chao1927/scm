package com.chaobo.scm.mdm.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * DataQualityIssueAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class DataQualityIssueAggregate {

    /**
     * OPEN（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int OPEN = 1;

    /**
     * ASSIGNED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int ASSIGNED = 2;

    /**
     * FIXED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int FIXED = 3;

    /**
     * VERIFIED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int VERIFIED = 4;

    /**
     * CLOSED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int CLOSED = 5;

    /**
     * issueNo（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String issueNo;

    /**
     * typeCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String typeCode;

    /**
     * dataCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String dataCode;

    /**
     * issueType（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String issueType;

    /**
     * issueDescription（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String issueDescription;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * assigneeId（类型：{@code Long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private Long assigneeId;

    /**
     * resolution（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String resolution;

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
     * 创建 DataQualityIssueAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param issueDescription 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param assigneeId 业务或技术标识，类型为 {@code Long}
     * @param resolution 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private DataQualityIssueAggregate(String issueNo, String typeCode, String dataCode, String issueType, String issueDescription, int status, Long assigneeId, String resolution, long version) {
        if (blank(issueNo) || blank(typeCode) || blank(dataCode) || blank(issueType) || blank(issueDescription)) {
            throw new IllegalArgumentException("quality issue references are required");
        }
        this.issueNo = issueNo;
        this.typeCode = typeCode;
        this.dataCode = dataCode;
        this.issueType = issueType;
        this.issueDescription = issueDescription;
        this.status = status;
        this.assigneeId = assigneeId;
        this.resolution = resolution;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code raise}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param issueDescription 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code DataQualityIssueAggregate}
     */
    public static DataQualityIssueAggregate raise(String issueNo, String typeCode, String dataCode, String issueType, String issueDescription) {
        DataQualityIssueAggregate aggregate = new DataQualityIssueAggregate(issueNo, typeCode, dataCode, issueType, issueDescription, OPEN, null, null, 1);
        aggregate.events.add(MdmEvent.of("DataQualityIssueRaised", issueNo, typeCode + "|" + dataCode));
        return aggregate;
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param issueNo 可追踪业务编码，类型为 {@code String}
     * @param typeCode 可追踪业务编码，类型为 {@code String}
     * @param dataCode 可追踪业务编码，类型为 {@code String}
     * @param issueType 业务处理参数或成员，类型为 {@code String}
     * @param issueDescription 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param assigneeId 业务或技术标识，类型为 {@code Long}
     * @param resolution 业务处理参数或成员，类型为 {@code String}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code DataQualityIssueAggregate}
     */
    public static DataQualityIssueAggregate restore(String issueNo, String typeCode, String dataCode, String issueType, String issueDescription, int status, Long assigneeId, String resolution, long version) {
        return new DataQualityIssueAggregate(issueNo, typeCode, dataCode, issueType, issueDescription, status, assigneeId, resolution, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code assign}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param assigneeId 业务或技术标识，类型为 {@code Long}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void assign(Long assigneeId, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != OPEN) {
            throw new IllegalStateException("quality issue is not open");
        }
        if (assigneeId == null) {
            throw new IllegalArgumentException("assignee is required");
        }
        this.assigneeId = assigneeId;
        status = ASSIGNED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueAssigned", issueNo, String.valueOf(assigneeId)));
    }

    /**
     * 处理当前类型职责中的操作 {@code markFixed}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param resolution 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void markFixed(String resolution, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != ASSIGNED) {
            throw new IllegalStateException("quality issue is not assigned");
        }
        if (blank(resolution)) {
            throw new IllegalArgumentException("resolution is required");
        }
        this.resolution = resolution;
        status = FIXED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueFixed", issueNo, resolution));
    }

    /**
     * 处理当前类型职责中的操作 {@code verify}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void verify(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != FIXED) {
            throw new IllegalStateException("quality issue is not fixed");
        }
        status = VERIFIED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueVerified", issueNo, resolution));
    }

    /**
     * 执行命令 {@code close}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void close(long expectedVersion) {
        ensureVersion(expectedVersion);
        if (status != VERIFIED) {
            throw new IllegalStateException("quality issue is not verified");
        }
        status = CLOSED;
        version++;
        events.add(MdmEvent.of("DataQualityIssueClosed", issueNo, resolution));
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
     * 处理当前类型职责中的操作 {@code issueNo}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String issueNo() {
        return issueNo;
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
     * 处理当前类型职责中的操作 {@code dataCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String dataCode() {
        return dataCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code issueType}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String issueType() {
        return issueType;
    }

    /**
     * 处理当前类型职责中的操作 {@code issueDescription}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String issueDescription() {
        return issueDescription;
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
     * 处理当前类型职责中的操作 {@code assigneeId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code Long}
     */
    public Long assigneeId() {
        return assigneeId;
    }

    /**
     * 处理当前类型职责中的操作 {@code resolution}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String resolution() {
        return resolution;
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
     * 校验业务约束 {@code ensureVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("quality issue version conflict");
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
}
