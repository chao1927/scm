package com.chaobo.scm.common.error;

/**
 * BusinessException。
 *
 * <p>位于公共/base 模块，仅提供稳定的跨模块类型和技术约定，不拥有任何子系统业务状态。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public final class BusinessException extends RuntimeException {

    /**
     * code（类型：{@code ErrorCode}）。
     *
     * <p>保存当前对象所需的可追踪业务编码；其具体生命周期由所属对象统一管理。
     */
    private final ErrorCode code;

    /**
     * 创建 BusinessException。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param code 可追踪业务编码，类型为 {@code ErrorCode}
     * @param message 业务处理参数或成员，类型为 {@code String}
     */
    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 处理当前类型职责中的操作 {@code code}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code ErrorCode}
     */
    public ErrorCode code() {
        return code;
    }
}
