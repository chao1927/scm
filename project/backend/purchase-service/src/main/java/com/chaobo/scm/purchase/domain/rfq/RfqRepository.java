package com.chaobo.scm.purchase.domain.rfq;

import java.util.Optional;

/**
 * RfqRepository。
 *
 * <p>位于领域层，使用通用语言表达业务状态、行为与不变量，不依赖 HTTP、数据库或消息中间件细节。声明或实现数据访问能力，使上层通过业务语义访问持久化数据。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
public interface RfqRepository {

    /**
     * 查询并返回 {@code findById}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param id 业务或技术标识，类型为 {@code long}
     * @return 查询并返回的结果，类型为 {@code Optional<RfqAggregate>}
     */
    Optional<RfqAggregate> findById(long id);

    /**
     * 查询并返回 {@code findByNo}。
     *
     * <p>该方法只读取或转换当前上下文数据，不应绕过数据权限，也不应产生业务状态副作用。
     * @param rfqNo 可追踪业务编码，类型为 {@code String}
     * @return 查询并返回的结果，类型为 {@code Optional<RfqAggregate>}
     */
    Optional<RfqAggregate> findByNo(String rfqNo);

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param aggregate 业务处理参数或成员，类型为 {@code RfqAggregate}
     * @param operatorId 业务或技术标识，类型为 {@code long}
     */
    void save(RfqAggregate aggregate, long operatorId);
}
