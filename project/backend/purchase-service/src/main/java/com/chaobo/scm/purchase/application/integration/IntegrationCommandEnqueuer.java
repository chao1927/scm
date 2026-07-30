package com.chaobo.scm.purchase.application.integration;

import com.chaobo.scm.purchase.domain.shared.IdentifierGenerator;
import com.chaobo.scm.purchase.infrastructure.persistence.integration.IntegrationCommandMapper;
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
     * mapper（类型：{@code IntegrationCommandMapper}）。
     *
     * <p>保存当前对象所需的持久化访问依赖；其具体生命周期由所属对象统一管理。
     */
    private final IntegrationCommandMapper mapper;

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
     * @param mapper 持久化访问依赖，类型为 {@code IntegrationCommandMapper}
     * @param ids 业务或技术标识，类型为 {@code IdentifierGenerator}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     */
    public IntegrationCommandEnqueuer(IntegrationCommandMapper mapper, IdentifierGenerator ids, ObjectMapper json) {
        this.mapper = mapper;
        this.ids = ids;
        this.json = json;
    }

    /**
     * 处理当前类型职责中的操作 {@code enqueue}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param commandType 用例输入命令，类型为 {@code String}
     * @param targetSystem 业务处理参数或成员，类型为 {@code String}
     * @param businessType 业务处理参数或成员，类型为 {@code String}
     * @param businessId 业务或技术标识，类型为 {@code String}
     * @param businessNo 可追踪业务编码，类型为 {@code String}
     * @param payload 业务处理参数或成员，类型为 {@code Object}
     */
    public void enqueue(String commandType, String targetSystem, String businessType, String businessId, String businessNo, Object payload) {
        mapper.insert(ids.nextId(), commandType, targetSystem, businessType, businessId, businessNo, write(payload));
    }

    /**
     * 处理当前类型职责中的操作 {@code write}。
     *
     * <p>该内部步骤用于收敛重复逻辑或保护局部规则，调用方应通过当前类型公开的业务入口使用该能力。
     * @param payload 业务处理参数或成员，类型为 {@code Object}
     * @return 处理当前类型职责中的操作的结果，类型为 {@code String}
     */
    private String write(Object payload) {
        try {
            return json.writeValueAsString(payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException("采购集成命令序列化失败", exception);
        }
    }
}
