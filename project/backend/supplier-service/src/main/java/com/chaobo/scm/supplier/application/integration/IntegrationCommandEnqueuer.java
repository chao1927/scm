package com.chaobo.scm.supplier.application.integration;

import com.chaobo.scm.supplier.domain.shared.IdentifierGenerator;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * IntegrationCommandEnqueuer。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Service
public class IntegrationCommandEnqueuer {

    /**
     * repo（类型：{@code IntegrationCommandRepository}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandRepository repo;

    /**
     * ids（类型：{@code IdentifierGenerator}）。
     *
     * <p>保存当前对象所需的业务或技术标识；其具体生命周期由所属对象统一管理。
     */
    private final IdentifierGenerator ids;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * 创建 IntegrationCommandEnqueuer。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param repo 业务处理参数或成员，类型为 {@code IntegrationCommandRepository}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     */
    public IntegrationCommandEnqueuer(IntegrationCommandRepository repo, IdentifierGenerator ids, ObjectMapper json) {
        this.repo = repo;
        this.ids = ids;
        this.json = json;
    }

    /**
     * 处理当前类型职责中的操作 {@code enqueue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param type 业务处理参数或成员，类型为 {@code String}
     * @param aggregateType 业务处理参数或成员，类型为 {@code String}
     * @param aggregateId 业务或技术标识，类型为 {@code long}
     * @param version 乐观锁或契约版本，类型为 {@code int}
     * @param target 业务处理参数或成员，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code Object}
     */
    public void enqueue(String type, String aggregateType, long aggregateId, int version, String target, Object payload) {
        long id = ids.nextId();
        repo.save(new IntegrationCommand(id, "IC-" + id, type, aggregateType, aggregateId, version, target, write(payload), 1, 0, null, null, null));
    }

    /**
     * 处理当前类型职责中的操作 {@code write}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param value 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalStateException("集成命令序列化失败", e);
        }
    }
}
