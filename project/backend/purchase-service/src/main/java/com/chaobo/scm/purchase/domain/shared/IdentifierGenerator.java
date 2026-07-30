package com.chaobo.scm.purchase.domain.shared;

/**
 * IdentifierGenerator。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。以稳定接口声明调用方所需能力，具体实现可在不影响调用方的前提下替换。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface IdentifierGenerator {

    /**
     * 处理当前类型职责中的操作 {@code nextId}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @return 处理当前类型职责中的操作的结果，类型为 {@code long}
     */
    long nextId();

    /**
     * 处理当前类型职责中的操作 {@code nextCode}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param prefix 业务处理参数或成员，类型为 {@code String}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    String nextCode(String prefix);
}
