package com.chaobo.scm.iam.domain;

import com.chaobo.scm.common.error.BusinessException;
import com.chaobo.scm.common.error.ErrorCode;

/**
 * UserAggregate。
 *
 * <p>位于当前子系统模块，负责其名称所表达的单一职责。作为聚合根保护状态迁移和业务不变量，并通过版本或幂等约束避免重复修改。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public class UserAggregate {

    /**
     * id（类型：{@code long}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final long id;

    /**
     * username（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String username;

    /**
     * passwordHash（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private String passwordHash;

    /**
     * status（类型：{@code int}）。
     *
     * <p>保存当前对象所需的生命周期状态；其具体生命周期由所属对象统一管理。
     */
    private int status;

    /**
     * failedAttempts（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private int failedAttempts;

    /**
     * version（类型：{@code int}）。
     *
     * <p>保存当前对象所需的乐观锁或契约版本；其具体生命周期由所属对象统一管理。
     */
    private int version;

    /**
     * 创建 UserAggregate。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param id 业务或技术标识，类型为 {@code long}
     * @param username 业务处理参数或成员，类型为 {@code String}
     * @param passwordHash 业务处理参数或成员，类型为 {@code String}
     * @param status 生命周期状态，类型为 {@code int}
     * @param failedAttempts 业务处理参数或成员，类型为 {@code int}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     */
    public UserAggregate(long id, String username, String passwordHash, int status, int failedAttempts, int version) {
        if (username == null || username.isBlank() || passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "用户名和密码不能为空");
        }
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.version = version;
    }

    /**
     * 处理当前类型职责中的操作 {@code authenticate}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param passwordHash 业务处理参数或成员，类型为 {@code String}
     */
    public void authenticate(String passwordHash) {
        if (status != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户不可登录");
        }
        if (!this.passwordHash.equals(passwordHash)) {
            failedAttempts++;
            if (failedAttempts >= AUTHENTICATE_VALUE_5) {
                status = 3;
            }
            version++;
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户名或密码错误");
        }
        failedAttempts = 0;
        version++;
    }

    /**
     * 执行命令 {@code disable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void disable() {
        if (status == DISABLE_VALUE_2) {
            throw new BusinessException(ErrorCode.STATE_CONFLICT, "用户已停用");
        }
        status = 2;
        version++;
    }

    /**
     * 执行命令 {@code enable}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     */
    public void enable() {
        status = 1;
        failedAttempts = 0;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code resetPassword}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param passwordHash 业务处理参数或成员，类型为 {@code String}
     */
    public void resetPassword(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "新密码不能为空");
        }
        this.passwordHash = passwordHash;
        failedAttempts = 0;
        version++;
    }

    /**
     * 处理当前类型职责中的操作 {@code id}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    public long id() {
        return id;
    }

    /**
     * 处理当前类型职责中的操作 {@code username}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String username() {
        return username;
    }

    /**
     * 处理当前类型职责中的操作 {@code passwordHash}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String passwordHash() {
        return passwordHash;
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
     * 处理当前类型职责中的操作 {@code failedAttempts}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int failedAttempts() {
        return failedAttempts;
    }

    /**
     * 处理当前类型职责中的操作 {@code version}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int version() {
        return version;
    }

    /**
     * 业务常量 {@code DISABLE_VALUE_2}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int DISABLE_VALUE_2 = 2;

    /**
     * 业务常量 {@code AUTHENTICATE_VALUE_5}。
     *
     * <p>集中表达当前用例使用的固定业务值，避免含义不明的字面量散落在判断或调用参数中。
     */
    private static final int AUTHENTICATE_VALUE_5 = 5;
}
