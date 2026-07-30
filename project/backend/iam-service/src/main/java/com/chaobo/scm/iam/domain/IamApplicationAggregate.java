package com.chaobo.scm.iam.domain;

/**
 * IamApplicationAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class IamApplicationAggregate {

    /**
     * ENABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int ENABLED = 1;

    /**
     * DISABLED（类型：{@code int}）。
     *
     * <p>定义当前类型使用的稳定常量，避免业务含义以魔法值散落。
     */
    public static final int DISABLED = 2;

    /**
     * appCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String appCode;

    /**
     * appName（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String appName;

    /**
     * homeUrl（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String homeUrl;

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
     * 创建 IamApplicationAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param appName 业务处理参数或成员，类型为 {@code String}
     * @param homeUrl 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private IamApplicationAggregate(String appCode, String appName, String homeUrl, int status, long version) {
        if (blank(appCode) || blank(appName)) {
            throw new IllegalArgumentException("application code and name are required");
        }
        this.appCode = appCode;
        this.appName = appName;
        this.homeUrl = homeUrl;
        this.status = status;
        this.version = version;
    }

    /**
     * 执行命令 {@code create}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param appName 业务处理参数或成员，类型为 {@code String}
     * @param homeUrl 业务处理参数或成员，类型为 {@code String}
     * @return 执行命令的结果，类型为 {@code IamApplicationAggregate}
     */
    public static IamApplicationAggregate create(String appCode, String appName, String homeUrl) {
        return new IamApplicationAggregate(appCode, appName, homeUrl, ENABLED, 1);
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param appName 业务处理参数或成员，类型为 {@code String}
     * @param homeUrl 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamApplicationAggregate}
     */
    public static IamApplicationAggregate restore(String appCode, String appName, String homeUrl, int status, long version) {
        return new IamApplicationAggregate(appCode, appName, homeUrl, status, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code change}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param appName 业务处理参数或成员，类型为 {@code String}
     * @param homeUrl 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void change(String appName, String homeUrl, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (blank(appName)) {
            throw new IllegalArgumentException("application name is required");
        }
        this.appName = appName;
        this.homeUrl = homeUrl;
        version++;
    }

    /**
     * 执行命令 {@code enable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void enable(long expectedVersion) {
        ensureVersion(expectedVersion);
        status = ENABLED;
        version++;
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void disable(long expectedVersion) {
        ensureVersion(expectedVersion);
        status = DISABLED;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code appCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String appCode() {
        return appCode;
    }

    /**
     * 处理当前类型职责中的操作 {@code appName}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String appName() {
        return appName;
    }

    /**
     * 处理当前类型职责中的操作 {@code homeUrl}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String homeUrl() {
        return homeUrl;
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
     * 校验业务约束 {@code ensureVersion}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    private void ensureVersion(long expectedVersion) {
        if (version != expectedVersion) {
            throw new IllegalStateException("application version conflict");
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
