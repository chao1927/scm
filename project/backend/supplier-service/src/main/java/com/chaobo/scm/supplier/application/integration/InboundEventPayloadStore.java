package com.chaobo.scm.supplier.application.integration;

import com.chaobo.scm.supplier.application.masterdata.MasterDataEventConsumeLogPort;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * InboundEventPayloadStore。
 *
 * <p>位于应用层，负责用例编排、事务边界、幂等处理和跨端口协作，核心不变量仍由领域对象保护。封装与其名称一致的业务或技术职责，并保持内部实现细节不向调用方泄露。该类型只在所属限界上下文内表达该语义，跨上下文协作应通过已声明的接口或事件完成。
 *
 * @author SCM Team
 * @since 0.1.0
 */
@Component
public class InboundEventPayloadStore {

    /**
     * inbox（类型：{@code MasterDataEventConsumeLogPort}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final MasterDataEventConsumeLogPort inbox;

    /**
     * json（类型：{@code ObjectMapper}）。
     *
     * <p>保存当前对象所需的业务处理参数或成员；其具体生命周期由所属对象统一管理。
     */
    private final ObjectMapper json;

    /**
     * 创建 InboundEventPayloadStore。
     *
     * <p>构造阶段集中接收必需依赖或恢复对象状态，确保实例创建后即可安全参与所属用例。
     * @param inbox 业务处理参数或成员，类型为 {@code MasterDataEventConsumeLogPort}
     * @param json 业务处理参数或成员，类型为 {@code ObjectMapper}
     */
    public InboundEventPayloadStore(MasterDataEventConsumeLogPort inbox, ObjectMapper json) {
        this.inbox = inbox;
        this.json = json;
    }

    /**
     * 执行命令 {@code save}。
     *
     * <p>该方法完成当前用例中的一个明确业务动作；状态修改、权限、幂等和异常语义由所属层次共同约束。
     * @param source 业务处理参数或成员，类型为 {@code String}
     * @param eventCode 可追踪业务编码，类型为 {@code String}
     * @param consumer 业务处理参数或成员，类型为 {@code String}
     * @param event 业务处理参数或成员，类型为 {@code Object}
     */
    public void save(String source, String eventCode, String consumer, Object event) {
        try {
            inbox.savePayload(source, eventCode, consumer, json.writeValueAsString(event));
        } catch (JacksonException exception) {
            throw new IllegalStateException("入站事件载荷序列化失败", exception);
        }
    }
}
