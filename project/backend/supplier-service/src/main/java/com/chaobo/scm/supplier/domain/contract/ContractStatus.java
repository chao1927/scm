package com.chaobo.scm.supplier.domain.contract;

/**
 * ContractStatus。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public enum ContractStatus {

    // 业务枚举值：draft
    DRAFT(1, "草稿"),
    // 业务枚举值：approving
    APPROVING(2, "审批中"),
    // 业务枚举值：active
    ACTIVE(3, "已生效"),
    // 业务枚举值：terminated
    TERMINATED(4, "已终止"),
    // 业务枚举值：expired
    EXPIRED(5, "已到期");

    /**
     * c（类型：{@code int}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final int c;

    /**
     * l（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String l;

    /**
     * 创建 ContractStatus。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param c 业务处理参数或成员，类型为 {@code int}
     * @param l 业务处理参数或成员，类型为 {@code String}
     */
    ContractStatus(int c, String l) {
        this.c = c;
        this.l = l;
    }

    /**
     * 处理当前类型职责中的操作 {@code code}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int code() {
        return c;
    }

    /**
     * 处理当前类型职责中的操作 {@code label}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String label() {
        return l;
    }

    /**
     * 转换数据模型 {@code from}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param c 业务处理参数或成员，类型为 {@code int}
     * @return 转换数据模型的结果，类型为 {@code ContractStatus}
     */
    public static ContractStatus from(int c) {
        for (var v : values()) {
            if (v.c == c) {
                return v;
            }
        }
        throw new IllegalArgumentException("未知合同状态");
    }
}
