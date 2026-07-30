package com.chaobo.scm.iam.domain;

/**
 * IamSsoClientAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class IamSsoClientAggregate {

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
     * ssoCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String ssoCode;

    /**
     * appCode（类型：{@code String}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final String appCode;

    /**
     * redirectUrl（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String redirectUrl;

    /**
     * secretHash（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String secretHash;

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
     * 创建 IamSsoClientAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param redirectUrl 业务处理参数或成员，类型为 {@code String}
     * @param secretHash 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     */
    private IamSsoClientAggregate(String ssoCode, String appCode, String redirectUrl, String secretHash, int status, long version) {
        if (blank(ssoCode) || blank(appCode) || blank(redirectUrl) || blank(secretHash)) {
            throw new IllegalArgumentException("sso client references are required");
        }
        this.ssoCode = ssoCode;
        this.appCode = appCode;
        this.redirectUrl = redirectUrl;
        this.secretHash = secretHash;
        this.status = status;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code configure}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param redirectUrl 业务处理参数或成员，类型为 {@code String}
     * @param secretHash 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamSsoClientAggregate}
     */
    public static IamSsoClientAggregate configure(String ssoCode, String appCode, String redirectUrl, String secretHash) {
        return new IamSsoClientAggregate(ssoCode, appCode, redirectUrl, secretHash, ENABLED, 1);
    }

    /**
     * 处理当前类型职责中的操作 {@code restore}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param ssoCode 可追踪业务编码，类型为 {@code String}
     * @param appCode 可追踪业务编码，类型为 {@code String}
     * @param redirectUrl 业务处理参数或成员，类型为 {@code String}
     * @param secretHash 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code long}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code IamSsoClientAggregate}
     */
    public static IamSsoClientAggregate restore(String ssoCode, String appCode, String redirectUrl, String secretHash, int status, long version) {
        return new IamSsoClientAggregate(ssoCode, appCode, redirectUrl, secretHash, status, version);
    }

    /**
     * 处理当前类型职责中的操作 {@code resetSecret}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param secretHash 业务处理参数或成员，类型为 {@code String}
     * @param expectedVersion 乐观锁或契约版本，类型为 {@code long}
     */
    public void resetSecret(String secretHash, long expectedVersion) {
        ensureVersion(expectedVersion);
        if (blank(secretHash)) {
            throw new IllegalArgumentException("secret hash is required");
        }
        this.secretHash = secretHash;
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
     * 处理当前类型职责中的操作 {@code ssoCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String ssoCode() {
        return ssoCode;
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
     * 处理当前类型职责中的操作 {@code redirectUrl}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String redirectUrl() {
        return redirectUrl;
    }

    /**
     * 处理当前类型职责中的操作 {@code secretHash}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String secretHash() {
        return secretHash;
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
            throw new IllegalStateException("sso client version conflict");
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
