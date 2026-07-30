package com.chaobo.scm.supplier.domain.quote;

/**
 * QuoteStatus。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。定义封闭的业务状态或类别集合，避免使用含义不明的数字和字符串。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public enum QuoteStatus {

    // 业务枚举值：draft
    DRAFT(1, "草稿"),
    // 业务枚举值：submitted
    SUBMITTED(2, "已提交"),
    // 业务枚举值：confirmed
    CONFIRMED(3, "已确认"),
    // 业务枚举值：adopted
    ADOPTED(4, "已采纳"),
    // 业务枚举值：rejected
    REJECTED(5, "已拒绝"),
    // 业务枚举值：voided
    VOIDED(6, "已作废"),
    // 业务枚举值：expired
    EXPIRED(7, "已过期");

    /**
     * code（类型：{@code int}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final int code;

    /**
     * label（类型：{@code String}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final String label;

    /**
     * 创建 QuoteStatus。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param code 可追踪业务编码，类型为 {@code int}
     * @param label 业务处理参数或成员，类型为 {@code String}
     */
    QuoteStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 处理当前类型职责中的操作 {@code code}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code int}
     */
    public int code() {
        return code;
    }

    /**
     * 处理当前类型职责中的操作 {@code label}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    public String label() {
        return label;
    }

    /**
     * 转换数据模型 {@code fromCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param code 可追踪业务编码，类型为 {@code int}
     * @return 转换数据模型的结果，类型为 {@code QuoteStatus}
     */
    public static QuoteStatus fromCode(int code) {
        for (var value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("未知报价状态: " + code);
    }
}
